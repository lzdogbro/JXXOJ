package top.hcode.hoj.dao.assignment.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.assignment.AssignmentStudentEntityService;
import top.hcode.hoj.mapper.AssignmentStudentMapper;
import top.hcode.hoj.pojo.entity.assignment.AssignmentStudent;
import top.hcode.hoj.pojo.vo.AssignmentStudentVO;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 作业下发快照 EntityService 实现
 */
@Service
public class AssignmentStudentEntityServiceImpl extends ServiceImpl<AssignmentStudentMapper, AssignmentStudent> implements AssignmentStudentEntityService {

    @Resource
    private AssignmentStudentMapper assignmentStudentMapper;

    @Override
    public List<AssignmentStudentVO> getAssignmentStudentList(Long aid) {
        return assignmentStudentMapper.getAssignmentStudentList(aid);
    }

    @Override
    public List<Long> getAcceptedPidsByAidUid(Long aid, String uid, int acceptedStatus) {
        return assignmentStudentMapper.getAcceptedPidsByAidUid(aid, uid, acceptedStatus);
    }

    @Override
    public void updateCompletion(Long aid, String uid, int acceptedStatus) {
        int acceptedCount = assignmentStudentMapper.countAcceptedByAidUid(aid, uid, acceptedStatus);
        int total = assignmentStudentMapper.countProblemByAid(aid);
        boolean completed = total > 0 && acceptedCount >= total;

        QueryWrapper<AssignmentStudent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("aid", aid).eq("uid", uid);
        AssignmentStudent existing = getOne(queryWrapper, false);
        if (existing == null) {
            // 理论上发布时已写快照，此处兜底补插
            save(new AssignmentStudent()
                    .setAid(aid)
                    .setUid(uid)
                    .setIsRequired(0)
                    .setAcceptedCount(acceptedCount)
                    .setStatus(completed ? 1 : 0)
                    .setScore(0)
                    .setGmtFinish(completed ? new Date() : null));
        } else {
            UpdateWrapper<AssignmentStudent> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("aid", aid).eq("uid", uid)
                    .set("accepted_count", acceptedCount)
                    .set("status", completed ? 1 : 0);
            if (completed) {
                updateWrapper.set("gmt_finish", new Date());
            } else {
                updateWrapper.set("gmt_finish", null);
            }
            update(updateWrapper);
        }
    }
}
