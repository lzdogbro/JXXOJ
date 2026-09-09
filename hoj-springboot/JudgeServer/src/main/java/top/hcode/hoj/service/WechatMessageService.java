package top.hcode.hoj.service;

import cn.hutool.core.util.StrUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import top.hcode.hoj.config.WechatProperties;
import top.hcode.hoj.mapper.WechatNotifyMapper;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信订阅消息推送服务（判题服务侧：作业完成通知）
 *
 * 配额跨服务共享：直连同一张 wechat_subscribe_quota 表，推送前 -1（受影响行=1 才允许发）。
 */
@Component
public class WechatMessageService {

    @Resource
    private WechatApiClient wechatApiClient;

    @Resource
    private WechatProperties wechatProperties;

    @Resource
    private WechatNotifyMapper wechatNotifyMapper;

    /**
     * 作业完成通知（异步）
     */
    @Async
    public void notifyAssignmentDone(Long aid, String uid) {
        String templateId = wechatProperties.getTemplateDone();
        if (StrUtil.isBlank(templateId)) {
            return;
        }
        String title = wechatNotifyMapper.getAssignmentTitle(aid);
        String studentName = wechatNotifyMapper.getStudentName(uid);
        List<String> openids = wechatNotifyMapper.getBoundParentOpenids(uid);
        if (openids == null || openids.isEmpty()) {
            return;
        }
        Map<String, Object> data = buildData(studentName, title, new Date());
        for (String openid : openids) {
            int affected = wechatNotifyMapper.decrementQuota(openid, templateId);
            if (affected > 0) {
                wechatApiClient.sendSubscribeMessage(openid, templateId, data, null);
            }
        }
    }

    /**
     * 字段 key（thing1/thing2/time3）需与微信后台模板关键词一致，上线时按实际模板调整。
     */
    private Map<String, Object> buildData(String studentName, String title, Date doneTime) {
        Map<String, Object> data = new HashMap<>();
        data.put("thing1", wrap(studentName));
        data.put("thing2", wrap(title));
        data.put("time3", wrap(doneTime == null ? "" : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(doneTime)));
        return data;
    }

    private Map<String, Object> wrap(String value) {
        Map<String, Object> m = new HashMap<>();
        m.put("value", value == null ? "" : value);
        return m;
    }
}
