package top.hcode.hoj.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 微信订阅通知 Mapper（判题服务侧完成通知用，直连共享表）
 */
@Mapper
@Repository
public interface WechatNotifyMapper {

    @Select("SELECT title FROM assignment WHERE id = #{aid}")
    String getAssignmentTitle(@Param("aid") Long aid);

    @Select("SELECT COALESCE(realname, username) FROM user_info WHERE uuid = #{uid}")
    String getStudentName(@Param("uid") String uid);

    @Select("SELECT parent_openid FROM parent_binding WHERE student_uid = #{uid} AND status = 1 AND parent_openid IS NOT NULL")
    List<String> getBoundParentOpenids(@Param("uid") String uid);

    @Update("UPDATE wechat_subscribe_quota SET quota = quota - 1 WHERE openid = #{openid} AND template_id = #{templateId} AND quota > 0")
    int decrementQuota(@Param("openid") String openid, @Param("templateId") String templateId);
}
