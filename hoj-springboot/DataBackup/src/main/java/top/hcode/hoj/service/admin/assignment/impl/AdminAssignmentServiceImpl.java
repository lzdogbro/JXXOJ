package top.hcode.hoj.service.admin.assignment.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.admin.assignment.AdminAssignmentManager;
import top.hcode.hoj.pojo.dto.AssignmentDTO;
import top.hcode.hoj.pojo.vo.AssignmentVO;
import top.hcode.hoj.service.admin.assignment.AdminAssignmentService;

import java.util.Date;
import java.util.HashMap;

/**
 * 后台管理作业 Service 实现
 */
@Service
public class AdminAssignmentServiceImpl implements AdminAssignmentService {

    @Autowired
    private AdminAssignmentManager adminAssignmentManager;

    @Override
    public CommonResult<IPage<AssignmentVO>> getAssignmentList(Integer limit, Integer currentPage, String keyword) {
        return CommonResult.successResponse(adminAssignmentManager.getAssignmentList(limit, currentPage, keyword));
    }

    @Override
    public CommonResult<HashMap<String, Object>> getAssignment(Long aid) {
        try {
            return CommonResult.successResponse(adminAssignmentManager.getAssignment(aid));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> addAssignment(AssignmentDTO assignmentDto) {
        try {
            adminAssignmentManager.addAssignment(assignmentDto);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> updateAssignment(AssignmentDTO assignmentDto) {
        try {
            adminAssignmentManager.updateAssignment(assignmentDto);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> publishAssignment(AssignmentDTO assignmentDto) {
        try {
            adminAssignmentManager.publishAssignment(assignmentDto);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> extendAssignment(Long aid, Date endTime) {
        try {
            adminAssignmentManager.extendAssignment(aid, endTime);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> deleteAssignment(Long aid) {
        try {
            adminAssignmentManager.deleteAssignment(aid);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }
}
