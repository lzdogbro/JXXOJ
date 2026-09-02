package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.assignment.AssignmentStudent;
import top.hcode.hoj.pojo.vo.AssignmentStudentVO;

import java.util.List;

/**
 * 作业下发快照 Mapper
 */
@Mapper
@Repository
public interface AssignmentStudentMapper extends BaseMapper<AssignmentStudent> {

    List<AssignmentStudentVO> getAssignmentStudentList(@Param("aid") Long aid);
}
