package top.hcode.hoj.shiro;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.utils.JwtUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 微信家长侧 JWT 过滤器（挂在 /api/wechat/**，Authorization 带 Bearer 前缀）
 */
@Component
@Slf4j(topic = "hoj")
public class WechatJwtFilter extends AuthenticatingFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        // 微信侧接口除 login（走 anon）外均需登录，直接交给 onAccessDenied 校验
        return false;
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String jwt = stripBearer(request.getHeader("Authorization"));
        if (StrUtil.isBlank(jwt)) {
            return null;
        }
        return new WechatJwtToken(jwt);
    }

    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String token = stripBearer(request.getHeader("Authorization"));
        if (StrUtil.isBlank(token)) {
            return this.onLoginFailure(null, new AuthenticationException("请先登录！"), servletRequest, servletResponse);
        }
        Claims claim = jwtUtils.getClaimByToken(token);
        if (claim == null || jwtUtils.isTokenExpired(claim.getExpiration())) {
            return this.onLoginFailure(null, new AuthenticationException("登录状态已失效，请重新登录！"), servletRequest, servletResponse);
        }
        String openid = claim.getSubject();
        if (!jwtUtils.hasWechatToken(openid)) {
            return this.onLoginFailure(null, new AuthenticationException("登录状态已失效，请重新登录！"), servletRequest, servletResponse);
        }
        return executeLogin(servletRequest, servletResponse);
    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        returnErrorResponse(request, response, e);
        return false;
    }

    private void returnErrorResponse(ServletRequest request, ServletResponse response, Exception e) {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        try {
            Throwable throwable = e.getCause() == null ? e : e.getCause();
            CommonResult<Void> result = CommonResult.errorResponse(throwable.getMessage(), ResultStatus.ACCESS_DENIED);
            String json = JSONUtil.toJsonStr(result);
            httpResponse.setContentType("application/json;charset=utf-8");
            httpResponse.setHeader("Access-Control-Expose-Headers", "Authorization,Url-Type");
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
            // HTTP 永远 200，业务状态放 body.status（前端 request.ts 只认 body.status，非 2xx 会吞 msg）
            httpResponse.setStatus(org.springframework.http.HttpStatus.OK.value());
            httpResponse.getWriter().print(json);
        } catch (IOException e1) {
        }
    }

    /**
     * 对跨域提供支持
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        httpServletResponse.setHeader("Access-Control-Expose-Headers", "Authorization,Url-Type,Content-Type");
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(org.springframework.http.HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }

    private String stripBearer(String authorization) {
        if (StrUtil.isBlank(authorization)) {
            return null;
        }
        String token = authorization.trim();
        if (token.startsWith("Bearer ") || token.startsWith("bearer ")) {
            return token.substring(7).trim();
        }
        return token;
    }
}
