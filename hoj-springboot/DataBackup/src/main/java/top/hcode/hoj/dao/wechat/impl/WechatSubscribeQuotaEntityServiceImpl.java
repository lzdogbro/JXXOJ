package top.hcode.hoj.dao.wechat.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.wechat.WechatSubscribeQuotaEntityService;
import top.hcode.hoj.mapper.WechatSubscribeQuotaMapper;
import top.hcode.hoj.pojo.entity.assignment.WechatSubscribeQuota;

import javax.annotation.Resource;

/**
 * 微信订阅消息推送配额 EntityService 实现
 */
@Service
public class WechatSubscribeQuotaEntityServiceImpl extends ServiceImpl<WechatSubscribeQuotaMapper, WechatSubscribeQuota> implements WechatSubscribeQuotaEntityService {

    @Resource
    private WechatSubscribeQuotaMapper wechatSubscribeQuotaMapper;

    @Override
    public int addQuota(String openid, String templateId) {
        return wechatSubscribeQuotaMapper.addQuota(openid, templateId);
    }

    @Override
    public int decrementQuota(String openid, String templateId) {
        return wechatSubscribeQuotaMapper.decrementQuota(openid, templateId);
    }
}
