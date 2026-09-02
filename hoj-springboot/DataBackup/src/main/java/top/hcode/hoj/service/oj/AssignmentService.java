package top.hcode.hoj.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.AssignmentProblemVO;
import top.hcode.hoj.pojo.vo.AssignmentUnfinishedVO;
import top.hcode.hoj.pojo.vo.AssignmentVO;
import top.hcode.hoj.pojo.vo.ProblemInfoVO;

import java.util.HashMap;
import java.util.List;

/**
 * 学生视图作业 Service
 */
public interface AssignmentService {

    CommonResult<IPage<AssignmentVO>> getMyAssignmentList(Integer limit, Integer currentPage);

    CommonResult<HashMap<String, Object>> getAssignmentDetail(Long aid);

    CommonResult<List<AssignmentProblemVO>> getAssignmentProblemList(Long aid);

    CommonResult<ProblemInfoVO> getAssignmentProblemDetails(Long aid, String displayId);

    CommonResult<AssignmentUnfinishedVO> getUnfinishedCount();
}
