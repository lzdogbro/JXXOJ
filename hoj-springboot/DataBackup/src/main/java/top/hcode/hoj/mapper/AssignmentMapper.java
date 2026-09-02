package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.assignment.Assignment;
import top.hcode.hoj.pojo.vo.AssignmentVO;

import java.util.List;

/**
 * 作业 Mapper
 */
@Mapper
@Repository
public interface AssignmentMapper extends BaseMapper<Assignment> {

    IPage<AssignmentVO> getAssignmentList(Page<AssignmentVO> page,
                                          @Param("keyword") String keyword,
                                          @Param("creatorUid") String creatorUid,
                                          @Param("status") Integer status);

    IPage<AssignmentVO> getMyAssignmentList(Page<AssignmentVO> page, @Param("uid") String uid);

    List<AssignmentVO> getMyAssignmentUnfinishedList(@Param("uid") String uid);
}
