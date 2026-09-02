package top.hcode.hoj.validator;

import org.springframework.stereotype.Component;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.pojo.dto.AssignmentDTO;
import top.hcode.hoj.pojo.entity.assignment.AssignmentProblem;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 作业校验器
 */
@Component
public class AssignmentValidator {

    @Resource
    private CommonValidator commonValidator;

    public void validateAssignment(AssignmentDTO assignmentDto) throws StatusFailException {
        commonValidator.validateContent(assignmentDto.getTitle(), "作业标题", 200);
        commonValidator.validateContentLength(assignmentDto.getDescription(), "作业说明", 65535);

        // 时间窗合法
        if (assignmentDto.getStartTime() != null && assignmentDto.getEndTime() != null
                && assignmentDto.getStartTime().after(assignmentDto.getEndTime())) {
            throw new StatusFailException("作业开始时间不能晚于截止时间！");
        }
    }

    public void validatePublish(AssignmentDTO assignmentDto) throws StatusFailException {
        if (assignmentDto.getProblemList() == null || assignmentDto.getProblemList().isEmpty()) {
            throw new StatusFailException("发布作业前必须至少选择一道题目！");
        }
    }

    /**
     * 校验题目列表字段合法性：题目 id 非空、展示编号非空、同一作业内题目不重复
     */
    public void validateProblemList(List<AssignmentProblem> problemList) throws StatusFailException {
        if (problemList == null || problemList.isEmpty()) {
            return;
        }
        Set<Long> pidSet = new HashSet<>();
        for (AssignmentProblem problem : problemList) {
            if (problem.getPid() == null) {
                throw new StatusFailException("题目 id 不能为空！");
            }
            if (problem.getDisplayId() == null || problem.getDisplayId().trim().isEmpty()) {
                throw new StatusFailException("作业内展示编号不能为空！");
            }
            if (!pidSet.add(problem.getPid())) {
                throw new StatusFailException("作业内存在重复的题目！");
            }
        }
    }
}
