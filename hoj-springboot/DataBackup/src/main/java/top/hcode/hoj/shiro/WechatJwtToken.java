package top.hcode.hoj.shiro;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * 微信侧 JWT 认证 token（与平台 JwtToken 区分，避免 Realm 冲突）
 */
public class WechatJwtToken implements AuthenticationToken {

    private String token;

    public WechatJwtToken(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
