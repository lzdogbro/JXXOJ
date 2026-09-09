package top.hcode.hoj.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置（判题服务侧完成通知用，appid/secret/模板 id 从环境变量注入）
 */
@Data
@Component
@ConfigurationProperties(prefix = "hoj.wechat")
public class WechatProperties {

    private String appid;

    private String secret;

    private String templatePublish;

    private String templateDone;
}
