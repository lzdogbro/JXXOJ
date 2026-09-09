package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.assignment.ParentBinding;

import java.util.List;

/**
 * 家长-孩子绑定 Mapper
 */
@Mapper
@Repository
public interface ParentBindingMapper extends BaseMapper<ParentBinding> {

    /**
     * 查询某孩子绑定的家长 openid 列表（status=1）
     */
    @Select("SELECT parent_openid FROM parent_binding WHERE student_uid = #{studentUid} AND status = 1 AND parent_openid IS NOT NULL")
    List<String> getBoundParentOpenids(@Param("studentUid") String studentUid);
}
