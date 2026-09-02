package top.hcode.hoj.dao.assignment.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.assignment.AssignmentEntityService;
import top.hcode.hoj.mapper.AssignmentMapper;
import top.hcode.hoj.pojo.entity.assignment.Assignment;
import top.hcode.hoj.pojo.vo.AssignmentVO;

import javax.annotation.Resource;

/**
 * 作业 EntityService 实现
 */
@Service
public class AssignmentEntityServiceImpl extends ServiceImpl<AssignmentMapper, Assignment> implements AssignmentEntityService {

    @Resource
    private AssignmentMapper assignmentMapper;

    @Override
    public IPage<AssignmentVO> getAssignmentList(int limit, int currentPage, String keyword, String creatorUid, Integer status) {
        Page<AssignmentVO> page = new Page<>(currentPage, limit);
        return assignmentMapper.getAssignmentList(page, keyword, creatorUid, status);
    }
}
