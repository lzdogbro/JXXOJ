package top.hcode.hoj.config;

import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.hcode.hoj.shiro.AccountRealm;
import top.hcode.hoj.shiro.JwtFilter;
import top.hcode.hoj.shiro.ShiroCacheManager;
import top.hcode.hoj.shiro.ShiroConstant;
import top.hcode.hoj.shiro.WechatJwtFilter;
import top.hcode.hoj.shiro.WechatRealm;
import top.hcode.hoj.utils.RedisUtils;

import javax.servlet.Filter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author: Himit_ZH
 * @Date: 2020/7/19 22:53
 * @Description: shiro启用注解拦截控制器
 */
@Configuration
public class ShiroConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private WechatJwtFilter wechatJwtFilter;

    @Autowired
    private RedisUtils redisUtils;

    @Value("${hoj.jwt.expire:86400}")
    private long expire;

    @Bean
    public DefaultWebSecurityManager securityManager(AccountRealm accountRealm, WechatRealm wechatRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealms(Arrays.asList(accountRealm, wechatRealm));

        ShiroCacheManager shiroCacheManager = new ShiroCacheManager();
        shiroCacheManager.setCacheLive(expire);
        shiroCacheManager.setCacheKeyPrefix(ShiroConstant.SHIRO_AUTHORIZATION_CACHE);
        shiroCacheManager.setRedisUtils(redisUtils);
        securityManager.setCacheManager(shiroCacheManager);
        /*
         * 关闭shiro自带的session，详情见文档
         */
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        securityManager.setSubjectDAO(subjectDAO);
        return securityManager;
    }

    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chainDefinition = new DefaultShiroFilterChainDefinition();
        Map<String, String> filterMap = new LinkedHashMap<>();
        // 微信侧：登录匿名；小程序码图片接口匿名；生成绑定码走平台 JWT（排在 /api/wechat/** 之前）；其余走微信 JWT
        filterMap.put("/api/wechat/login", "anon");
        filterMap.put("/api/wechat/qrcode/**", "anon");
        filterMap.put("/api/wechat/bind-code", "jwt");
        filterMap.put("/api/wechat/**", "wechatJwt");
        filterMap.put("/**", "jwt"); // 主要通过注解方式校验权限
        chainDefinition.addPathDefinitions(filterMap);
        return chainDefinition;
    }

    @Bean("shiroFilterFactoryBean")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager,
                                                         ShiroFilterChainDefinition shiroFilterChainDefinition) {
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager);
        Map<String, Filter> filters = new HashMap<>();
        filters.put("jwt", jwtFilter);
        filters.put("wechatJwt", wechatJwtFilter);
        shiroFilter.setFilters(filters);
        Map<String, String> filterMap = shiroFilterChainDefinition.getFilterChainMap();
        shiroFilter.setFilterChainDefinitionMap(filterMap);
        return shiroFilter;
    }


    //开启注解代理
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager){
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }
    @Bean
    public static DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }
}
