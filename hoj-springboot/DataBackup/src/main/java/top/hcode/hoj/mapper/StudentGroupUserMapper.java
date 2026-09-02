package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.assignment.StudentGroupUser;
import top.hcode.hoj.pojo.vo.StudentGroupUserVO;

import java.util.List;

/**
 * 学生组成员 Mapper
 */
@Mapper
@Repository
public interface StudentGroupUserMapper extends BaseMapper<StudentGroupUser> {

    List<StudentGroupUserVO> getGroupUserList(@Param("gid") Long gid);
}
