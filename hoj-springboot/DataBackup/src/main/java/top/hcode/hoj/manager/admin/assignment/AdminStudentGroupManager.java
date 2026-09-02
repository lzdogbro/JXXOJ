package top.hcode.hoj.manager.admin.assignment;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.dao.assignment.StudentGroupEntityService;
import top.hcode.hoj.dao.assignment.StudentGroupUserEntityService;
import top.hcode.hoj.pojo.entity.assignment.StudentGroup;
import top.hcode.hoj.pojo.entity.assignment.StudentGroupUser;
import top.hcode.hoj.pojo.vo.StudentGroupUserVO;
import top.hcode.hoj.pojo.vo.StudentGroupVO;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.validator.CommonValidator;

import javax.annotation.Resource;
import java.util.List;

/**
 * 后台管理学生组 Manager
 */
@Component
@Slf4j(topic = "hoj")
public class AdminStudentGroupManager {

    @Resource
    private StudentGroupEntityService studentGroupEntityService;

    @Resource
    private StudentGroupUserEntityService studentGroupUserEntityService;

    @Resource
    private CommonValidator commonValidator;

    public List<StudentGroupVO> getGroupList() {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        String ownerUid = isRoot ? null : userRolesVo.getUid();
        return studentGroupEntityService.getGroupList(ownerUid);
    }

    public List<StudentGroupUserVO> getGroupUserList(Long gid) throws StatusFailException, StatusForbiddenException {
        StudentGroup group = getGroupChecked(gid);
        checkOwnerOrRoot(group.getOwnerUid());
        return studentGroupUserEntityService.getGroupUserList(gid);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addGroup(StudentGroup group) throws StatusFailException {
        commonValidator.validateContent(group.getName(), "组名", 100);
        commonValidator.validateContentLength(group.getDescription(), "组描述", 500);

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        group.setId(null);
        group.setOwnerUid(userRolesVo.getUid());
        group.setIsDeleted(0);

        boolean isOk = studentGroupEntityService.save(group);
        if (!isOk) {
            throw new StatusFailException("新建学生组失败！");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateGroup(StudentGroup group) throws StatusFailException, StatusForbiddenException {
        commonValidator.validateContent(group.getName(), "组名", 100);
        commonValidator.validateContentLength(group.getDescription(), "组描述", 500);

        StudentGroup oldGroup = getGroupChecked(group.getId());
        checkOwnerOrRoot(oldGroup.getOwnerUid());

        boolean isOk = studentGroupEntityService.updateById(new StudentGroup()
                .setId(oldGroup.getId())
                .setName(group.getName())
                .setDescription(group.getDescription()));
        if (!isOk) {
            throw new StatusFailException("修改学生组失败！");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(Long gid) throws StatusFailException, StatusForbiddenException {
        StudentGroup group = getGroupChecked(gid);
        checkOwnerOrRoot(group.getOwnerUid());

        boolean isOk = studentGroupEntityService.updateById(new StudentGroup().setId(gid).setIsDeleted(1));
        if (!isOk) {
            throw new StatusFailException("删除学生组失败！");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void addGroupUser(Long gid, List<String> uidList) throws StatusFailException, StatusForbiddenException {
        StudentGroup group = getGroupChecked(gid);
        checkOwnerOrRoot(group.getOwnerUid());

        if (uidList == null || uidList.isEmpty()) {
            throw new StatusFailException("请至少选择一个学生！");
        }

        for (String uid : uidList) {
            QueryWrapper<StudentGroupUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("gid", gid).eq("uid", uid);
            StudentGroupUser exists = studentGroupUserEntityService.getOne(queryWrapper, false);
            if (exists == null) {
                studentGroupUserEntityService.save(new StudentGroupUser().setGid(gid).setUid(uid));
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeGroupUser(Long gid, String uid) throws StatusFailException, StatusForbiddenException {
        StudentGroup group = getGroupChecked(gid);
        checkOwnerOrRoot(group.getOwnerUid());

        QueryWrapper<StudentGroupUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("gid", gid).eq("uid", uid);
        boolean isOk = studentGroupUserEntityService.remove(queryWrapper);
        if (!isOk) {
            throw new StatusFailException("移出成员失败！");
        }
    }

    private StudentGroup getGroupChecked(Long gid) throws StatusFailException {
        StudentGroup group = studentGroupEntityService.getById(gid);
        if (group == null || group.getIsDeleted() == 1) {
            throw new StatusFailException("该学生组不存在！");
        }
        return group;
    }

    private void checkOwnerOrRoot(String ownerUid) throws StatusForbiddenException {
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        if (isRoot) {
            return;
        }
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        if (!userRolesVo.getUid().equals(ownerUid)) {
            throw new StatusForbiddenException("对不起，你无权限操作！");
        }
    }
}
