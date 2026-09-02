package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.assignment.AssignmentProblem;
import top.hcode.hoj.pojo.vo.AssignmentProblemVO;

import java.util.List;

/**
 * 作业题目 Mapper
 */
@Mapper
@Repository
public interface AssignmentProblemMapper extends BaseMapper<AssignmentProblem> {

    List<AssignmentProblemVO> getAssignmentProblemList(@Param("aid") Long aid);
}
