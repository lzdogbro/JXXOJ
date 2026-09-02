package top.hcode.hoj.dao.assignment.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.assignment.StudentGroupEntityService;
import top.hcode.hoj.mapper.StudentGroupMapper;
import top.hcode.hoj.pojo.entity.assignment.StudentGroup;
import top.hcode.hoj.pojo.vo.StudentGroupVO;

import javax.annotation.Resource;
import java.util.List;

/**
 * 学生组 EntityService 实现
 */
@Service
public class StudentGroupEntityServiceImpl extends ServiceImpl<StudentGroupMapper, StudentGroup> implements StudentGroupEntityService {

    @Resource
    private StudentGroupMapper studentGroupMapper;

    @Override
    public List<StudentGroupVO> getGroupList(String ownerUid) {
        return studentGroupMapper.getGroupList(ownerUid);
    }
}
