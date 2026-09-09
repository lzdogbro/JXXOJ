package top.hcode.hoj.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置（appid/secret/订阅模板 id），不入库不进前端不进 git 明文
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
