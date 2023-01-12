package cn.worken.auth.security.advice;

import cn.worken.auth.security.OAuthServerConfig;
import java.util.Optional;
import javax.servlet.http.Cookie;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * token 颁布完处理
 *
 * @author shaoyijiong
 * @date 2020/7/7
 */
@Component
@Aspect
public class TokenCreateAdvice {

    /**
     * token 生成后 set cookie , 具体如何生成 token
     *
     * @see OAuthServerConfig
     */
    @AfterReturning(pointcut = "execution(* org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter.enhance(..))", returning = "accessToken")
    public void afterTokenPublish(OAuth2AccessToken accessToken) {
        setCookie(accessToken);

    }

    /**
     * 在token生成时设置cookie
     */
    private void setCookie(OAuth2AccessToken token) {
        // 设置 token
        setCookie("token", token.getValue(), token.getExpiresIn());
        // 设置 refreshToken
        OAuth2RefreshToken refreshToken = token.getRefreshToken();
        if (refreshToken != null) {
            setCookie("refresh_token", refreshToken.getValue(), token.getExpiresIn());
        }

    }

    private void setCookie(String key, String value, int maxAge) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
            .getRequestAttributes();
        if (requestAttributes != null) {
            Cookie cookie = new Cookie(key, value);
            cookie.setMaxAge(maxAge);
            cookie.setHttpOnly(false);
            //设置根路径
            cookie.setPath("/");
            Optional.ofNullable(requestAttributes.getResponse()).ifPresent(r -> r.addCookie(cookie));
        }
    }
}
