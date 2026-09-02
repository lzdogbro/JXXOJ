package top.hcode.hoj.service.admin.assignment.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.admin.assignment.AdminStudentGroupManager;
import top.hcode.hoj.pojo.entity.assignment.StudentGroup;
import top.hcode.hoj.pojo.vo.StudentGroupUserVO;
import top.hcode.hoj.pojo.vo.StudentGroupVO;
import top.hcode.hoj.service.admin.assignment.AdminStudentGroupService;

import java.util.List;

/**
 * 后台管理学生组 Service 实现
 */
@Service
public class AdminStudentGroupServiceImpl implements AdminStudentGroupService {

    @Autowired
    private AdminStudentGroupManager adminStudentGroupManager;

    @Override
    public CommonResult<List<StudentGroupVO>> getGroupList() {
        return CommonResult.successResponse(adminStudentGroupManager.getGroupList());
    }

    @Override
    public CommonResult<List<StudentGroupUserVO>> getGroupUserList(Long gid) {
        try {
            return CommonResult.successResponse(adminStudentGroupManager.getGroupUserList(gid));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> addGroup(StudentGroup group) {
        try {
            adminStudentGroupManager.addGroup(group);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> updateGroup(StudentGroup group) {
        try {
            adminStudentGroupManager.updateGroup(group);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> deleteGroup(Long gid) {
        try {
            adminStudentGroupManager.deleteGroup(gid);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> addGroupUser(Long gid, List<String> uidList) {
        try {
            adminStudentGroupManager.addGroupUser(gid, uidList);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> removeGroupUser(Long gid, String uid) {
        try {
            adminStudentGroupManager.removeGroupUser(gid, uid);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }
}
