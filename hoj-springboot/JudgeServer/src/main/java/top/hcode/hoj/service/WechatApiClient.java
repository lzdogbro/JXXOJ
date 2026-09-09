package top.hcode.hoj.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.hcode.hoj.config.WechatProperties;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信小程序服务端接口客户端（判题服务侧：access_token + 订阅消息发送）
 *
 * 判题服务无 Redis，access_token 用进程内缓存（提前 300s 过期兜底）。
 */
@Component
@Slf4j(topic = "hoj")
public class WechatApiClient {

    private static volatile String cachedToken;

    private static volatile long tokenExpireAt = 0L;

    @Resource
    private WechatProperties wechatProperties;

    public synchronized String getAccessToken() {
        long now = System.currentTimeMillis();
        if (cachedToken != null && now < tokenExpireAt) {
            return cachedToken;
        }
        String appid = wechatProperties.getAppid();
        String secret = wechatProperties.getSecret();
        if (StrUtil.isBlank(appid) || StrUtil.isBlank(secret)) {
            log.warn("[wechat] 微信未配置 appid/secret，跳过推送");
            return null;
        }
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appid
                + "&secret=" + secret;
        String resp = HttpUtil.get(url, 5000);
        JSONObject json = JSONUtil.parseObj(resp);
        String token = json.getStr("access_token");
        if (StrUtil.isBlank(token)) {
            log.warn("[wechat] 获取 access_token 失败: {}", resp);
            return null;
        }
        int expiresIn = json.getInt("expires_in", 7200);
        cachedToken = token;
        tokenExpireAt = now + Math.max(expiresIn - 300, 60) * 1000L;
        return token;
    }

    public boolean sendSubscribeMessage(String openid, String templateId, Map<String, Object> data, String page) {
        String accessToken = getAccessToken();
        if (accessToken == null) {
            return false;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("touser", openid);
        body.put("template_id", templateId);
        body.put("page", StrUtil.isBlank(page) ? "pages/index/index" : page);
        body.put("data", data);
        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;
        String resp = HttpUtil.post(url, JSONUtil.toJsonStr(body), 5000);
        JSONObject json = JSONUtil.parseObj(resp);
        int errcode = json.getInt("errcode", -1);
        if (errcode != 0) {
            log.warn("[wechat] 订阅消息发送失败 openid={} template={} resp={}", openid, templateId, resp);
            return false;
        }
        return true;
    }
}
