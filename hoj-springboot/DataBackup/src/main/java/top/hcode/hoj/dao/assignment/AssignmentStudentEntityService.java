package top.hcode.hoj.dao.assignment;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.assignment.AssignmentStudent;
import top.hcode.hoj.pojo.vo.AssignmentStudentVO;

import java.util.List;

/**
 * 作业下发快照 EntityService
 */
public interface AssignmentStudentEntityService extends IService<AssignmentStudent> {

    List<AssignmentStudentVO> getAssignmentStudentList(Long aid);

    /**
     * 查询某学生在某作业中已 AC 的题目 pid 集合（用于题目列表状态标注）
     *
     * @param aid            作业id
     * @param uid            学生uid
     * @param acceptedStatus AC 状态码（Constants.Judge.STATUS_ACCEPTED）
     */
    List<Long> getAcceptedPidsByAidUid(Long aid, String uid, int acceptedStatus);

    /**
     * 重算某学生在某作业的完成情况（AC 制：accepted_count >= 题目总数 即完成）
     *
     * @param aid            作业id
     * @param uid            学生uid
     * @param acceptedStatus AC 状态码（Constants.Judge.STATUS_ACCEPTED）
     */
    void updateCompletion(Long aid, String uid, int acceptedStatus);
}
