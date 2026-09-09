package top.hcode.hoj.shiro;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.hcode.hoj.dao.wechat.WechatUserEntityService;
import top.hcode.hoj.pojo.entity.assignment.WechatUser;
import top.hcode.hoj.utils.JwtUtils;

/**
 * 微信家长侧认证 Realm（家长无平台账号，principal 为 openid）
 */
@Slf4j
@Component
public class WechatRealm extends AuthorizingRealm {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private WechatUserEntityService wechatUserEntityService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof WechatJwtToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        // 家长无平台角色/权限
        return new SimpleAuthorizationInfo();
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        WechatJwtToken jwt = (WechatJwtToken) token;

        String openid = jwtUtils.getClaimByToken((String) jwt.getPrincipal()).getSubject();

        WechatUser wechatUser = wechatUserEntityService.getById(openid);
        if (wechatUser == null) {
            throw new UnknownAccountException("微信用户不存在！");
        }

        WechatProfile profile = new WechatProfile();
        profile.setOpenid(openid);
        return new SimpleAuthenticationInfo(profile, jwt.getCredentials(), getName());
    }
}
