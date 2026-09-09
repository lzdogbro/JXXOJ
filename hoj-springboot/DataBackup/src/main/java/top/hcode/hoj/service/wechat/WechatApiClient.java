package top.hcode.hoj.service.wechat;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.config.WechatProperties;
import top.hcode.hoj.utils.RedisUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 微信小程序服务端接口客户端（code2session / access_token / 订阅消息发送）
 *
 * appid / secret / 模板 id 均来自配置中心（不入库不进前端不进 git 明文）
 */
@Component
@Slf4j(topic = "hoj")
public class WechatApiClient {

    private static final String ACCESS_TOKEN_KEY = "wechat:access_token";

    @Resource
    private WechatProperties wechatProperties;

    @Resource
    private RedisUtils redisUtils;

    /**
     * wx.login 的临时 code 换 openid
     */
    public String code2session(String code) throws StatusFailException {
        String appid = wechatProperties.getAppid();
        String secret = wechatProperties.getSecret();
        if (StrUtil.isBlank(appid) || StrUtil.isBlank(secret)) {
            throw new StatusFailException("微信小程序未配置 appid/secret！");
        }
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appid
                + "&secret=" + secret + "&js_code=" + code + "&grant_type=authorization_code";
        String resp = HttpUtil.get(url, 5000);
        JSONObject json = JSONUtil.parseObj(resp);
        String openid = json.getStr("openid");
        if (StrUtil.isBlank(openid)) {
            log.warn("[wechat] code2session 失败: {}", resp);
            throw new StatusFailException("code 无效或已过期！");
        }
        return openid;
    }

    /**
     * 获取全局 access_token（Redis 缓存，提前 300s 过期兜底）
     */
    public String getAccessToken() throws StatusFailException {
        Object cached = redisUtils.get(ACCESS_TOKEN_KEY);
        if (cached != null && StrUtil.isNotBlank(cached.toString())) {
            return cached.toString();
        }
        String appid = wechatProperties.getAppid();
        String secret = wechatProperties.getSecret();
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appid
                + "&secret=" + secret;
        String resp = HttpUtil.get(url, 5000);
        JSONObject json = JSONUtil.parseObj(resp);
        String token = json.getStr("access_token");
        if (StrUtil.isBlank(token)) {
            log.warn("[wechat] 获取 access_token 失败: {}", resp);
            throw new StatusFailException("获取微信 access_token 失败！");
        }
        int expiresIn = json.getInt("expires_in", 7200);
        redisUtils.set(ACCESS_TOKEN_KEY, token, Math.max(expiresIn - 300, 60));
        return token;
    }

    /**
     * 发送一次性订阅消息，返回是否成功（errcode == 0）
     */
    public boolean sendSubscribeMessage(String openid, String templateId, Map<String, Object> data, String page) {
        String accessToken;
        try {
            accessToken = getAccessToken();
        } catch (StatusFailException e) {
            log.warn("[wechat] 获取 access_token 失败，跳过推送: {}", e.getMessage());
            return false;
        }
        Map<String, Object> body = new java.util.HashMap<>();
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

    /**
     * 生成小程序码（扫码绑定）：scene=绑定码，page=pages/bind/bind
     * 成功返回 PNG 二进制；失败（微信返回 JSON 错误）抛 StatusFailException
     */
    public byte[] getWxaCodeUnlimited(String scene, String page) throws StatusFailException {
        String accessToken = getAccessToken();
        String url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" + accessToken;
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("scene", scene);
        body.put("page", page);
        body.put("check_path", false);
        body.put("env_version", "release");
        body.put("width", 430);
        byte[] bytes;
        try {
            HttpResponse response = HttpRequest.post(url)
                    .body(JSONUtil.toJsonStr(body), "application/json")
                    .timeout(5000)
                    .execute();
            bytes = response.bodyBytes();
        } catch (Exception e) {
            log.warn("[wechat] getwxacodeunlimit 请求异常: {}", e.getMessage());
            throw new StatusFailException("生成小程序码失败！");
        }
        // 出错时微信返回 JSON（首字节 '{'），成功时返回图片二进制
        if (bytes == null || bytes.length == 0 || bytes[0] == '{') {
            String respStr = bytes == null ? "" : new String(bytes, StandardCharsets.UTF_8);
            log.warn("[wechat] getwxacodeunlimit 失败: {}", respStr);
            throw new StatusFailException("生成小程序码失败！");
        }
        return bytes;
    }
}
