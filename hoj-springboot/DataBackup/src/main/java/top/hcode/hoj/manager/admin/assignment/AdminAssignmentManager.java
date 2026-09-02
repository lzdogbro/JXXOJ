package top.hcode.hoj.manager.admin.assignment;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.dao.assignment.AssignmentEntityService;
import top.hcode.hoj.dao.assignment.AssignmentProblemEntityService;
import top.hcode.hoj.dao.assignment.AssignmentStudentEntityService;
import top.hcode.hoj.dao.assignment.StudentGroupEntityService;
import top.hcode.hoj.dao.assignment.StudentGroupUserEntityService;
import top.hcode.hoj.dao.user.UserInfoEntityService;
import top.hcode.hoj.pojo.dto.AssignmentDTO;
import top.hcode.hoj.pojo.entity.assignment.Assignment;
import top.hcode.hoj.pojo.entity.assignment.AssignmentProblem;
import top.hcode.hoj.pojo.entity.assignment.AssignmentStudent;
import top.hcode.hoj.pojo.entity.assignment.StudentGroup;
import top.hcode.hoj.pojo.entity.assignment.StudentGroupUser;
import top.hcode.hoj.pojo.entity.user.UserInfo;
import top.hcode.hoj.pojo.vo.AssignmentStudentVO;
import top.hcode.hoj.pojo.vo.AssignmentVO;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.validator.AssignmentValidator;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 后台管理作业 Manager
 */
@Component
@Slf4j(topic = "hoj")
public class AdminAssignmentManager {

    @Resource
    private AssignmentEntityService assignmentEntityService;

    @Resource
    private AssignmentProblemEntityService assignmentProblemEntityService;

    @Resource
    private AssignmentStudentEntityService assignmentStudentEntityService;

    @Resource
    private StudentGroupEntityService studentGroupEntityService;

    @Resource
    private StudentGroupUserEntityService studentGroupUserEntityService;

    @Resource
    private UserInfoEntityService userInfoEntityService;

    @Resource
    private AssignmentValidator assignmentValidator;

    public IPage<AssignmentVO> getAssignmentList(Integer limit, Integer currentPage, String keyword) {
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        boolean isAdmin = SecurityUtils.getSubject().hasRole("admin");
        boolean isProblemAdmin = SecurityUtils.getSubject().hasRole("problem_admin");

        String creatorUid = null;
        Integer status = null;
        if (isRoot) {
            // 全局，不过滤
        } else if (isAdmin) {
            // 普通 admin 仅自己的
            creatorUid = userRolesVo.getUid();
        } else if (isProblemAdmin) {
            // 题目管理员只读全部已发布
            status = 1;
        }

        IPage<AssignmentVO> page = assignmentEntityService.getAssignmentList(limit, currentPage, keyword, creatorUid, status);
        for (AssignmentVO vo : page.getRecords()) {
            computeTimeStatus(vo);
        }
        return page;
    }

    public HashMap<String, Object> getAssignment(Long aid) throws StatusFailException, StatusForbiddenException {
        Assignment assignment = assignmentEntityService.getById(aid);
        if (assignment == null || assignment.getIsDeleted() == 1) {
            throw new StatusFailException("查询失败：该作业不存在！");
        }

        checkReadAccess(assignment);

        AssignmentVO assignmentVo = new AssignmentVO();
        assignmentVo.setId(assignment.getId());
        assignmentVo.setTitle(assignment.getTitle());
        assignmentVo.setDescription(assignment.getDescription());
        assignmentVo.setCreatorUid(assignment.getCreatorUid());
        assignmentVo.setIsRequired(assignment.getIsRequired());
        assignmentVo.setStatus(assignment.getStatus());
        assignmentVo.setStartTime(assignment.getStartTime());
        assignmentVo.setEndTime(assignment.getEndTime());
        assignmentVo.setIsDeleted(assignment.getIsDeleted());
        assignmentVo.setGmtCreate(assignment.getGmtCreate());
        assignmentVo.setGmtModified(assignment.getGmtModified());
        UserInfo creator = userInfoEntityService.getById(assignment.getCreatorUid());
        assignmentVo.setCreatorUsername(creator == null ? null : creator.getUsername());
        computeTimeStatus(assignmentVo);

        QueryWrapper<AssignmentProblem> problemQueryWrapper = new QueryWrapper<>();
        problemQueryWrapper.eq("aid", aid).orderByAsc("sort", "id");
        List<AssignmentProblem> problemList = assignmentProblemEntityService.list(problemQueryWrapper);
        assignmentVo.setProblemCount(problemList.size());

        List<AssignmentStudentVO> studentList = assignmentStudentEntityService.getAssignmentStudentList(aid);

        HashMap<String, Object> result = new HashMap<>(3);
        result.put("assignment", assignmentVo);
        result.put("problemList", problemList);
        result.put("studentList", studentList);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addAssignment(AssignmentDTO assignmentDto) throws StatusFailException, StatusForbiddenException {
        assignmentValidator.validateAssignment(assignmentDto);
        assignmentValidator.validateProblemList(assignmentDto.getProblemList());

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        String uid = userRolesVo.getUid();

        boolean directPublish = assignmentDto.getStatus() != null && assignmentDto.getStatus() == 1;
        if (directPublish) {
            assignmentValidator.validatePublish(assignmentDto);
        }

        Assignment assignment = new Assignment()
                .setTitle(assignmentDto.getTitle())
                .setDescription(assignmentDto.getDescription())
                .setIsRequired(assignmentDto.getIsRequired() == null ? 0 : assignmentDto.getIsRequired())
                .setStatus(assignmentDto.getStatus() == null ? 0 : assignmentDto.getStatus())
                .setStartTime(assignmentDto.getStartTime())
                .setEndTime(assignmentDto.getEndTime())
                .setCreatorUid(uid)
                .setIsDeleted(0);

        boolean isOk = assignmentEntityService.save(assignment);
        if (!isOk) {
            throw new StatusFailException("新建作业失败！");
        }

        // 保存题目（草稿或直接发布均可保存题目集）
        replaceProblems(assignment.getId(), assignmentDto.getProblemList());

        // 直接发布（status=1）时展开成员写快照
        if (directPublish) {
            publishMembers(assignment, assignmentDto.getGroupIdList(), assignmentDto.getExtraUidList());
        }

        log.info("[{}],[{}],aid:[{}],operatorUid:[{}],operatorUsername:[{}]",
                "Admin_Assignment", "Add", assignment.getId(), uid, userRolesVo.getUsername());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateAssignment(AssignmentDTO assignmentDto) throws StatusFailException, StatusForbiddenException {
        assignmentValidator.validateAssignment(assignmentDto);
        assignmentValidator.validateProblemList(assignmentDto.getProblemList());

        Assignment oldAssignment = assignmentEntityService.getById(assignmentDto.getId());
        if (oldAssignment == null || oldAssignment.getIsDeleted() == 1) {
            throw new StatusFailException("修改失败：该作业不存在！");
        }
        AssignmentAuthHelper.checkOwnerOrRoot(oldAssignment.getCreatorUid());

        boolean isPublished = oldAssignment.getStatus() != null && oldAssignment.getStatus() == 1;

        // 发布后仅可改标题/说明（题目集锁定，时间与必做状态不可改，延期走 extend）
        Assignment update = new Assignment().setId(oldAssignment.getId());
        update.setTitle(assignmentDto.getTitle());
        update.setDescription(assignmentDto.getDescription());
        if (!isPublished) {
            // 未传字段保留原值，避免局部修改时静默清空必做状态/时间窗
            update.setIsRequired(assignmentDto.getIsRequired() != null
                    ? assignmentDto.getIsRequired() : oldAssignment.getIsRequired());
            update.setStartTime(assignmentDto.getStartTime() != null
                    ? assignmentDto.getStartTime() : oldAssignment.getStartTime());
            update.setEndTime(assignmentDto.getEndTime() != null
                    ? assignmentDto.getEndTime() : oldAssignment.getEndTime());
        }

        boolean isOk = assignmentEntityService.updateById(update);
        if (!isOk) {
            throw new StatusFailException("修改失败！");
        }

        // 草稿阶段可改题目集
        if (!isPublished) {
            replaceProblems(oldAssignment.getId(), assignmentDto.getProblemList());
        }

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        log.info("[{}],[{}],aid:[{}],operatorUid:[{}],operatorUsername:[{}]",
                "Admin_Assignment", "Update", oldAssignment.getId(), userRolesVo.getUid(), userRolesVo.getUsername());
    }

    @Transactional(rollbackFor = Exception.class)
    public void publishAssignment(AssignmentDTO assignmentDto) throws StatusFailException, StatusForbiddenException {
        Assignment assignment = assignmentEntityService.getById(assignmentDto.getId());
        if (assignment == null || assignment.getIsDeleted() == 1) {
            throw new StatusFailException("发布失败：该作业不存在！");
        }
        AssignmentAuthHelper.checkOwnerOrRoot(assignment.getCreatorUid());

        if (assignment.getStatus() != null && assignment.getStatus() == 1) {
            throw new StatusFailException("该作业已发布，请勿重复发布！");
        }

        // 发布前校验题目非空
        QueryWrapper<AssignmentProblem> problemQueryWrapper = new QueryWrapper<>();
        problemQueryWrapper.eq("aid", assignment.getId());
        long problemCount = assignmentProblemEntityService.count(problemQueryWrapper);
        if (problemCount == 0) {
            throw new StatusFailException("发布作业前必须至少选择一道题目！");
        }

        // 展开成员写快照
        publishMembers(assignment, assignmentDto.getGroupIdList(), assignmentDto.getExtraUidList());

        boolean isOk = assignmentEntityService.updateById(new Assignment()
                .setId(assignment.getId())
                .setStatus(1));
        if (!isOk) {
            throw new StatusFailException("发布失败！");
        }

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        log.info("[{}],[{}],aid:[{}],operatorUid:[{}],operatorUsername:[{}]",
                "Admin_Assignment", "Publish", assignment.getId(), userRolesVo.getUid(), userRolesVo.getUsername());
    }

    @Transactional(rollbackFor = Exception.class)
    public void extendAssignment(Long aid, Date endTime) throws StatusFailException, StatusForbiddenException {
        Assignment assignment = assignmentEntityService.getById(aid);
        if (assignment == null || assignment.getIsDeleted() == 1) {
            throw new StatusFailException("延期失败：该作业不存在！");
        }
        AssignmentAuthHelper.checkOwnerOrRoot(assignment.getCreatorUid());

        if (endTime == null) {
            throw new StatusFailException("延期失败：截止时间不能为空！");
        }
        if (endTime.before(new Date())) {
            throw new StatusFailException("延期失败：截止时间不能早于当前时间！");
        }
        if (assignment.getStartTime() != null && endTime.before(assignment.getStartTime())) {
            throw new StatusFailException("延期失败：截止时间不能早于开始时间！");
        }

        boolean isOk = assignmentEntityService.updateById(new Assignment().setId(aid).setEndTime(endTime));
        if (!isOk) {
            throw new StatusFailException("延期失败！");
        }

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        log.info("[{}],[{}],aid:[{}],operatorUid:[{}],operatorUsername:[{}]",
                "Admin_Assignment", "Extend", aid, userRolesVo.getUid(), userRolesVo.getUsername());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAssignment(Long aid) throws StatusFailException, StatusForbiddenException {
        Assignment assignment = assignmentEntityService.getById(aid);
        if (assignment == null || assignment.getIsDeleted() == 1) {
            throw new StatusFailException("删除失败：该作业不存在！");
        }
        AssignmentAuthHelper.checkOwnerOrRoot(assignment.getCreatorUid());

        boolean isOk = assignmentEntityService.updateById(new Assignment().setId(aid).setIsDeleted(1));
        if (!isOk) {
            throw new StatusFailException("删除失败！");
        }

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        log.info("[{}],[{}],aid:[{}],operatorUid:[{}],operatorUsername:[{}]",
                "Admin_Assignment", "Delete", aid, userRolesVo.getUid(), userRolesVo.getUsername());
    }

    /**
     * 校验管理侧读权限：root/创建者可读；problem_admin 只读已发布
     */
    private void checkReadAccess(Assignment assignment) throws StatusForbiddenException {
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        if (isRoot) {
            return;
        }
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        if (userRolesVo.getUid().equals(assignment.getCreatorUid())) {
            return;
        }
        boolean isProblemAdmin = SecurityUtils.getSubject().hasRole("problem_admin");
        if (isProblemAdmin && assignment.getStatus() != null && assignment.getStatus() == 1) {
            return;
        }
        throw new StatusForbiddenException("对不起，你无权限操作！");
    }

    /**
     * 展开学生组成员 + 手动追加的学生 -> 写入 assignment_student 快照
     */
    private void publishMembers(Assignment assignment, List<Long> groupIdList, List<String> extraUidList)
            throws StatusForbiddenException, StatusFailException {

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        String uid = userRolesVo.getUid();

        Set<String> uidSet = new LinkedHashSet<>();

        if (groupIdList != null) {
            for (Long gid : groupIdList) {
                StudentGroup group = studentGroupEntityService.getById(gid);
                if (group == null || group.getIsDeleted() == 1) {
                    throw new StatusFailException("发布失败：学生组不存在或已删除（gid=" + gid + "）！");
                }
                // 成员归属校验：普通 admin 只能下发自己创建的学生组
                if (!isRoot && !group.getOwnerUid().equals(uid)) {
                    throw new StatusForbiddenException("对不起，你无权限使用该学生组！");
                }
                List<StudentGroupUser> members = studentGroupUserEntityService.list(
                        new QueryWrapper<StudentGroupUser>().eq("gid", gid));
                for (StudentGroupUser member : members) {
                    uidSet.add(member.getUid());
                }
            }
        }

        if (extraUidList != null) {
            uidSet.addAll(extraUidList);
        }

        // 校验快照学生账号均存在，避免外键异常
        if (!uidSet.isEmpty()) {
            long userCount = userInfoEntityService.count(new QueryWrapper<UserInfo>().in("uuid", uidSet));
            if (userCount != uidSet.size()) {
                throw new StatusFailException("发布失败：存在无效的学生账号！");
            }
        }

        // 一次性查出已存在快照，仅补插缺失项
        Integer isRequired = assignment.getIsRequired() == null ? 0 : assignment.getIsRequired();
        Set<String> existingUids = new HashSet<>();
        if (!uidSet.isEmpty()) {
            List<AssignmentStudent> existing = assignmentStudentEntityService.list(
                    new QueryWrapper<AssignmentStudent>().eq("aid", assignment.getId()).in("uid", uidSet));
            existingUids = existing.stream().map(AssignmentStudent::getUid).collect(Collectors.toSet());
        }
        List<AssignmentStudent> toInsert = new ArrayList<>();
        for (String studentUid : uidSet) {
            if (!existingUids.contains(studentUid)) {
                toInsert.add(new AssignmentStudent()
                        .setAid(assignment.getId())
                        .setUid(studentUid)
                        .setIsRequired(isRequired)
                        .setStatus(0)
                        .setAcceptedCount(0)
                        .setScore(0));
            }
        }
        if (!toInsert.isEmpty()) {
            assignmentStudentEntityService.saveBatch(toInsert);
        }
    }

    /**
     * 替换作业题目集（草稿阶段）
     */
    private void replaceProblems(Long aid, List<AssignmentProblem> problemList) {
        assignmentProblemEntityService.remove(new QueryWrapper<AssignmentProblem>().eq("aid", aid));
        if (problemList == null || problemList.isEmpty()) {
            return;
        }
        List<AssignmentProblem> toInsert = new ArrayList<>(problemList.size());
        int sort = 0;
        for (AssignmentProblem problem : problemList) {
            problem.setId(null);
            problem.setAid(aid);
            if (problem.getScore() == null) {
                problem.setScore(0);
            }
            problem.setSort(sort++);
            toInsert.add(problem);
        }
        assignmentProblemEntityService.saveBatch(toInsert);
    }

    private void computeTimeStatus(AssignmentVO vo) {
        if (vo == null) {
            return;
        }
        Date now = new Date();
        boolean ended = vo.getEndTime() != null && now.after(vo.getEndTime());
        boolean running = !ended && vo.getStartTime() != null && now.after(vo.getStartTime());
        vo.setIsEnded(ended);
        vo.setIsRunning(running);
    }
}
