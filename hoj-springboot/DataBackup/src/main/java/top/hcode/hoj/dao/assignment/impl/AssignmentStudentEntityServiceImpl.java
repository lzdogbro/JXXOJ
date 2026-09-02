package top.hcode.hoj.dao.assignment.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.assignment.AssignmentStudentEntityService;
import top.hcode.hoj.mapper.AssignmentStudentMapper;
import top.hcode.hoj.pojo.entity.assignment.AssignmentStudent;
import top.hcode.hoj.pojo.vo.AssignmentStudentVO;

import javax.annotation.Resource;
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
}
