package top.hcode.hoj.dao.assignment.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.assignment.StudentGroupUserEntityService;
import top.hcode.hoj.mapper.StudentGroupUserMapper;
import top.hcode.hoj.pojo.entity.assignment.StudentGroupUser;
import top.hcode.hoj.pojo.vo.StudentGroupUserVO;

import javax.annotation.Resource;
import java.util.List;

/**
 * 学生组成员 EntityService 实现
 */
@Service
public class StudentGroupUserEntityServiceImpl extends ServiceImpl<StudentGroupUserMapper, StudentGroupUser> implements StudentGroupUserEntityService {

    @Resource
    private StudentGroupUserMapper studentGroupUserMapper;

    @Override
    public List<StudentGroupUserVO> getGroupUserList(Long gid) {
        return studentGroupUserMapper.getGroupUserList(gid);
    }
}
