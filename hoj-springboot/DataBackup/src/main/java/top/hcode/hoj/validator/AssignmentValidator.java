package top.hcode.hoj.validator;

import org.springframework.stereotype.Component;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.pojo.dto.AssignmentDTO;

import javax.annotation.Resource;

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
}
