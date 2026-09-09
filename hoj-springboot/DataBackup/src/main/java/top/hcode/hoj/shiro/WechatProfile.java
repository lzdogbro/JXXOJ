package top.hcode.hoj.shiro;

import lombok.Data;

import java.io.Serializable;

/**
 * 微信登录态的当前主体（家长 openid）
 */
@Data
public class WechatProfile implements Serializable {

    private String openid;

    public String getId() { // shiro 登录用户实体默认主键获取方法
        return openid;
    }
}
