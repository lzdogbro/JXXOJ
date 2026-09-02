package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.assignment.AssignmentProblem;

/**
 * 作业题目 Mapper
 */
@Mapper
@Repository
public interface AssignmentProblemMapper extends BaseMapper<AssignmentProblem> {
}
