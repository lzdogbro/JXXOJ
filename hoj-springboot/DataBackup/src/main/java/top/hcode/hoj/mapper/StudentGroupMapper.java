package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.assignment.StudentGroup;
import top.hcode.hoj.pojo.vo.StudentGroupVO;

import java.util.List;

/**
 * 学生组 Mapper
 */
@Mapper
@Repository
public interface StudentGroupMapper extends BaseMapper<StudentGroup> {

    List<StudentGroupVO> getGroupList(@Param("ownerUid") String ownerUid);
}
