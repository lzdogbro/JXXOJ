package top.hcode.hoj.service.oj.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.oj.AssignmentManager;
import top.hcode.hoj.pojo.vo.AssignmentProblemVO;
import top.hcode.hoj.pojo.vo.AssignmentVO;
import top.hcode.hoj.pojo.vo.ProblemInfoVO;
import top.hcode.hoj.service.oj.AssignmentService;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * 学生视图作业 Service 实现
 */
@Service
public class AssignmentServiceImpl implements AssignmentService {

    @Resource
    private AssignmentManager assignmentManager;

    @Override
    public CommonResult<IPage<AssignmentVO>> getMyAssignmentList(Integer limit, Integer currentPage) {
        return CommonResult.successResponse(assignmentManager.getMyAssignmentList(limit, currentPage));
    }

    @Override
    public CommonResult<HashMap<String, Object>> getAssignmentDetail(Long aid) {
        try {
            return CommonResult.successResponse(assignmentManager.getAssignmentDetail(aid));
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<List<AssignmentProblemVO>> getAssignmentProblemList(Long aid) {
        try {
            return CommonResult.successResponse(assignmentManager.getAssignmentProblemList(aid));
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<ProblemInfoVO> getAssignmentProblemDetails(Long aid, String displayId) {
        try {
            return CommonResult.successResponse(assignmentManager.getAssignmentProblemDetails(aid, displayId));
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }
}
