package top.hcode.hoj.dao.wechat;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.assignment.WechatSubscribeQuota;

/**
 * 微信订阅消息推送配额 EntityService
 */
public interface WechatSubscribeQuotaEntityService extends IService<WechatSubscribeQuota> {

    /**
     * 授权上报：配额 +1
     */
    int addQuota(String openid, String templateId);

    /**
     * 推送前扣配额：受影响行=1 才允许发
     */
    int decrementQuota(String openid, String templateId);
}
