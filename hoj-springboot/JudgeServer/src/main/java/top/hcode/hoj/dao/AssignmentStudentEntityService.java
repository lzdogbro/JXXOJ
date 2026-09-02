package top.hcode.hoj.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.assignment.AssignmentStudent;

/**
 * 作业下发快照 EntityService（判题服务侧）
 */
public interface AssignmentStudentEntityService extends IService<AssignmentStudent> {

    /**
     * 重算某学生在某作业的完成情况（AC 制：accepted_count >= 题目总数 即完成）
     *
     * @param aid            作业id
     * @param uid            学生uid
     * @param acceptedStatus AC 状态码（Constants.Judge.STATUS_ACCEPTED）
     */
    void updateCompletion(Long aid, String uid, int acceptedStatus);
}
