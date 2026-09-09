package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.assignment.WechatSubscribeQuota;

/**
 * 微信订阅消息推送配额 Mapper
 */
@Mapper
@Repository
public interface WechatSubscribeQuotaMapper extends BaseMapper<WechatSubscribeQuota> {

    /**
     * 授权上报：配额 +1（不存在则插入）
     */
    @Insert("INSERT INTO wechat_subscribe_quota(openid, template_id, quota) VALUES(#{openid}, #{templateId}, 1) " +
            "ON DUPLICATE KEY UPDATE quota = quota + 1")
    int addQuota(@Param("openid") String openid, @Param("templateId") String templateId);

    /**
     * 推送前扣配额：扣成功（受影响行=1）才允许发，配额为 0 时不受影响
     */
    @Update("UPDATE wechat_subscribe_quota SET quota = quota - 1 WHERE openid = #{openid} AND template_id = #{templateId} AND quota > 0")
    int decrementQuota(@Param("openid") String openid, @Param("templateId") String templateId);
}
