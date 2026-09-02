package top.hcode.hoj.service.admin.assignment;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.AssignmentDTO;
import top.hcode.hoj.pojo.vo.AssignmentVO;

import java.util.Date;
import java.util.HashMap;

/**
 * 后台管理作业 Service
 */
public interface AdminAssignmentService {

    CommonResult<IPage<AssignmentVO>> getAssignmentList(Integer limit, Integer currentPage, String keyword);

    CommonResult<HashMap<String, Object>> getAssignment(Long aid);

    CommonResult<Void> addAssignment(AssignmentDTO assignmentDto);

    CommonResult<Void> updateAssignment(AssignmentDTO assignmentDto);

    CommonResult<Void> publishAssignment(AssignmentDTO assignmentDto);

    CommonResult<Void> extendAssignment(Long aid, Date endTime);

    CommonResult<Void> deleteAssignment(Long aid);
}
