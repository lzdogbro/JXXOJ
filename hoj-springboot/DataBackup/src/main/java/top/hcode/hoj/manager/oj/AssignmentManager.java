package top.hcode.hoj.manager.oj;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Component;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.dao.assignment.AssignmentEntityService;
import top.hcode.hoj.dao.assignment.AssignmentProblemEntityService;
import top.hcode.hoj.dao.assignment.AssignmentStudentEntityService;
import top.hcode.hoj.dao.judge.JudgeEntityService;
import top.hcode.hoj.dao.problem.CodeTemplateEntityService;
import top.hcode.hoj.dao.problem.LanguageEntityService;
import top.hcode.hoj.dao.problem.ProblemEntityService;
import top.hcode.hoj.dao.problem.ProblemLanguageEntityService;
import top.hcode.hoj.dao.problem.ProblemTagEntityService;
import top.hcode.hoj.dao.problem.TagEntityService;
import top.hcode.hoj.dao.user.UserInfoEntityService;
import top.hcode.hoj.pojo.entity.assignment.Assignment;
import top.hcode.hoj.pojo.entity.assignment.AssignmentProblem;
import top.hcode.hoj.pojo.entity.assignment.AssignmentStudent;
import top.hcode.hoj.pojo.entity.problem.CodeTemplate;
import top.hcode.hoj.pojo.entity.problem.Language;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.problem.ProblemLanguage;
import top.hcode.hoj.pojo.entity.problem.ProblemTag;
import top.hcode.hoj.pojo.entity.problem.Tag;
import top.hcode.hoj.pojo.entity.user.UserInfo;
import top.hcode.hoj.pojo.vo.AssignmentProblemVO;
import top.hcode.hoj.pojo.vo.AssignmentUnfinishedVO;
import top.hcode.hoj.pojo.vo.AssignmentVO;
import top.hcode.hoj.pojo.vo.ProblemCountVO;
import top.hcode.hoj.pojo.vo.ProblemInfoVO;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.utils.Constants;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 学生视图作业 Manager
 */
@Component
@Slf4j(topic = "hoj")
public class AssignmentManager {

    @Resource
    private AssignmentEntityService assignmentEntityService;

    @Resource
    private AssignmentProblemEntityService assignmentProblemEntityService;

    @Resource
    private AssignmentStudentEntityService assignmentStudentEntityService;

    @Resource
    private UserInfoEntityService userInfoEntityService;

    @Resource
    private ProblemEntityService problemEntityService;

    @Resource
    private ProblemTagEntityService problemTagEntityService;

    @Resource
    private TagEntityService tagEntityService;

    @Resource
    private LanguageEntityService languageEntityService;

    @Resource
    private ProblemLanguageEntityService problemLanguageEntityService;

    @Resource
    private CodeTemplateEntityService codeTemplateEntityService;

    @Resource
    private JudgeEntityService judgeEntityService;

    /**
     * 布置给我的作业（分页：未完成在前、必做未完成置顶）
     */
    public IPage<AssignmentVO> getMyAssignmentList(Integer limit, Integer currentPage) {
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        String uid = userRolesVo.getUid();

        Page<AssignmentVO> page = new Page<>(currentPage, limit);
        IPage<AssignmentVO> result = assignmentEntityService.getMyAssignmentList(page, uid);
        for (AssignmentVO vo : result.getRecords()) {
            computeTimeStatus(vo);
            vo.setCompleted(isCompleted(vo));
        }
        return result;
    }

    /**
     * 单份作业 + 我的完成进度（每题状态实时查 judge）
     */
    public HashMap<String, Object> getAssignmentDetail(Long aid)
            throws StatusNotFoundException, StatusForbiddenException {
        Assignment assignment = checkAndGetAssignment(aid);
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        String uid = userRolesVo.getUid();
        checkAssigned(aid, uid);

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

        List<AssignmentProblemVO> problemList = getProblemListWithStatus(aid, uid);
        assignmentVo.setProblemCount(problemList.size());
        fillProgress(assignmentVo, problemList);

        HashMap<String, Object> result = new HashMap<>(2);
        result.put("assignment", assignmentVo);
        result.put("problemList", problemList);
        return result;
    }

    /**
     * 作业题目列表（提交入口用，校验我是被下发学生）
     */
    public List<AssignmentProblemVO> getAssignmentProblemList(Long aid)
            throws StatusNotFoundException, StatusForbiddenException {
        checkAndGetAssignment(aid);
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        String uid = userRolesVo.getUid();
        checkAssigned(aid, uid);
        return getProblemListWithStatus(aid, uid);
    }

    /**
     * 作业题目详情（提交页用，校验我是被下发学生，屏蔽内部评测参数）
     */
    public ProblemInfoVO getAssignmentProblemDetails(Long aid, String displayId)
            throws StatusNotFoundException, StatusForbiddenException {
        checkAndGetAssignment(aid);
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        String uid = userRolesVo.getUid();
        checkAssigned(aid, uid);

        QueryWrapper<AssignmentProblem> problemQueryWrapper = new QueryWrapper<>();
        problemQueryWrapper.eq("aid", aid).eq("display_id", displayId);
        AssignmentProblem assignmentProblem = assignmentProblemEntityService.getOne(problemQueryWrapper, false);
        if (assignmentProblem == null) {
            throw new StatusNotFoundException("该作业题目不存在！");
        }

        Problem problem = problemEntityService.getById(assignmentProblem.getPid());
        if (problem == null || (problem.getAuth() != null && problem.getAuth() == 2)) {
            throw new StatusNotFoundException("该题目不存在或已隐藏！");
        }

        // 题目标签
        List<Tag> tags = new ArrayList<>();
        List<Long> tidList = problemTagEntityService.list(
                new QueryWrapper<ProblemTag>().eq("pid", problem.getId()))
                .stream().map(ProblemTag::getTid).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(tidList)) {
            tags = (List<Tag>) tagEntityService.listByIds(tidList);
        }

        // 题目可用语言
        HashMap<Long, String> tmpMap = new HashMap<>();
        List<String> languagesStr = new LinkedList<>();
        List<Long> lidList = problemLanguageEntityService.list(
                new QueryWrapper<ProblemLanguage>().eq("pid", problem.getId()).select("lid"))
                .stream().map(ProblemLanguage::getLid).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(lidList)) {
            Collection<Language> languages = languageEntityService.listByIds(lidList);
            languages = languages.stream().sorted(Comparator.comparing(Language::getSeq, Comparator.reverseOrder())
                            .thenComparing(Language::getId))
                    .collect(Collectors.toList());
            languages.forEach(language -> {
                languagesStr.add(language.getName());
                tmpMap.put(language.getId(), language.getName());
            });
        }

        // 提交统计
        ProblemCountVO problemCount = judgeEntityService.getProblemCount(problem.getId(), null);

        // 代码模板
        List<CodeTemplate> codeTemplates = codeTemplateEntityService.list(
                new QueryWrapper<CodeTemplate>().eq("pid", problem.getId()).eq("status", true));
        HashMap<String, String> langNameAndCode = new HashMap<>();
        if (CollectionUtil.isNotEmpty(codeTemplates)) {
            for (CodeTemplate codeTemplate : codeTemplates) {
                langNameAndCode.put(tmpMap.get(codeTemplate.getLid()), codeTemplate.getCode());
            }
        }

        // 屏蔽一些题目参数
        problem.setJudgeExtraFile(null).setSpjCode(null).setSpjLanguage(null);

        return new ProblemInfoVO(problem, tags, languagesStr, problemCount, langNameAndCode);
    }

    /**
     * 当前学生的未完成统计（navbar 角标 + 闪烁用）
     *
     * badgeCount：截止前未完成的必做题目数（角标数字）
     * flash：必做未完成（任意截止）或选做未完成（未截止）时继续闪烁
     */
    public AssignmentUnfinishedVO getUnfinishedCount() {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        String uid = userRolesVo.getUid();
        List<AssignmentVO> list = assignmentEntityService.getMyAssignmentUnfinishedList(uid);

        Date now = new Date();
        int badgeCount = 0;
        boolean flash = false;
        for (AssignmentVO vo : list) {
            int total = vo.getProblemCount() == null ? 0 : vo.getProblemCount();
            int accepted = vo.getAcceptedCount() == null ? 0 : vo.getAcceptedCount();
            if (accepted >= total) {
                continue; // 已全部完成
            }
            boolean required = vo.getIsRequired() != null && vo.getIsRequired() == 1;
            boolean beforeDeadline = vo.getEndTime() == null || now.before(vo.getEndTime());
            if (required) {
                flash = true;
                if (beforeDeadline) {
                    badgeCount += (total - accepted);
                }
            } else if (beforeDeadline) {
                flash = true;
            }
        }
        return new AssignmentUnfinishedVO()
                .setBadgeCount(badgeCount)
                .setFlash(flash);
    }

    private Assignment checkAndGetAssignment(Long aid) throws StatusNotFoundException, StatusForbiddenException {
        Assignment assignment = assignmentEntityService.getById(aid);
        if (assignment == null || assignment.getIsDeleted() == 1) {
            throw new StatusNotFoundException("该作业不存在！");
        }
        if (assignment.getStatus() == null || assignment.getStatus() != 1) {
            throw new StatusForbiddenException("该作业尚未发布！");
        }
        return assignment;
    }

    private void checkAssigned(Long aid, String uid) throws StatusForbiddenException {
        long count = assignmentStudentEntityService.count(
                new QueryWrapper<AssignmentStudent>().eq("aid", aid).eq("uid", uid));
        if (count == 0) {
            throw new StatusForbiddenException("你未被布置该作业，无权查看！");
        }
    }

    private List<AssignmentProblemVO> getProblemListWithStatus(Long aid, String uid) {
        List<AssignmentProblemVO> problemList = assignmentProblemEntityService.getAssignmentProblemList(aid);
        List<Long> acceptedPids = assignmentStudentEntityService.getAcceptedPidsByAidUid(
                aid, uid, Constants.Judge.STATUS_ACCEPTED.getStatus());
        Set<Long> acceptedSet = new HashSet<>(acceptedPids);
        for (AssignmentProblemVO vo : problemList) {
            vo.setStatus(acceptedSet.contains(vo.getPid()) ? 1 : 0);
        }
        return problemList;
    }

    private void fillProgress(AssignmentVO vo, List<AssignmentProblemVO> problemList) {
        int acceptedCount = 0;
        if (problemList != null) {
            for (AssignmentProblemVO p : problemList) {
                if (p.getStatus() != null && p.getStatus() == 1) {
                    acceptedCount++;
                }
            }
        }
        vo.setAcceptedCount(acceptedCount);
        vo.setCompleted(isCompleted(vo));
    }

    private boolean isCompleted(AssignmentVO vo) {
        Integer problemCount = vo.getProblemCount();
        Integer acceptedCount = vo.getAcceptedCount();
        return problemCount != null && problemCount > 0
                && acceptedCount != null && acceptedCount >= problemCount;
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
