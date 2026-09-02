package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.assignment.AssignmentStudent;

/**
 * 作业下发快照 Mapper（判题服务侧）
 */
@Mapper
@Repository
public interface AssignmentStudentMapper extends BaseMapper<AssignmentStudent> {

    /**
     * 统计某学生在某作业内已 AC 的题目数（按 pid 去重）
     */
    @Select("SELECT COUNT(DISTINCT pid) FROM judge WHERE aid = #{aid} AND uid = #{uid} AND status = #{acceptedStatus}")
    int countAcceptedByAidUid(@Param("aid") Long aid, @Param("uid") String uid, @Param("acceptedStatus") int acceptedStatus);

    /**
     * 统计某作业的题目总数
     */
    @Select("SELECT COUNT(*) FROM assignment_problem WHERE aid = #{aid}")
    int countProblemByAid(@Param("aid") Long aid);
}
