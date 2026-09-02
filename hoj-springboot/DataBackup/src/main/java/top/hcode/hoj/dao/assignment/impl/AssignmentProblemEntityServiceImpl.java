package top.hcode.hoj.dao.assignment.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.assignment.AssignmentProblemEntityService;
import top.hcode.hoj.mapper.AssignmentProblemMapper;
import top.hcode.hoj.pojo.entity.assignment.AssignmentProblem;
import top.hcode.hoj.pojo.vo.AssignmentProblemVO;

import javax.annotation.Resource;
import java.util.List;

/**
 * 作业题目 EntityService 实现
 */
@Service
public class AssignmentProblemEntityServiceImpl extends ServiceImpl<AssignmentProblemMapper, AssignmentProblem> implements AssignmentProblemEntityService {

    @Resource
    private AssignmentProblemMapper assignmentProblemMapper;

    @Override
    public List<AssignmentProblemVO> getAssignmentProblemList(Long aid) {
        return assignmentProblemMapper.getAssignmentProblemList(aid);
    }
}
