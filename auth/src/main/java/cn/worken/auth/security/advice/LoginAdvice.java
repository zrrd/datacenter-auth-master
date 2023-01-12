package cn.worken.auth.security.advice;

import cn.worken.auth.security.dto.LoginParam;
import cn.worken.auth.security.exception.ServiceException;
import cn.worken.auth.service.entity.SysUserLoginLog;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 登录日志记录
 *
 * @author shaoyijiong
 * @date 2020/7/28
 */
@Component
@Aspect
@RequiredArgsConstructor
public class LoginAdvice {

    /**
     * 登录成功
     */
    @AfterReturning(pointcut = "execution(* cn.worken.auth.security.UserDetailsServiceImpl.login(..))")
    public void afterReturning(JoinPoint joinPoint) {
        LoginParam loginParam = (LoginParam) joinPoint.getArgs()[0];
        SysUserLoginLog.loginSuccess(loginParam.getUsername(), getIpAddress(), loginParam.getLoginType().name());
    }


    /**
     * 登录失败
     */
    @AfterThrowing(pointcut = "execution(* cn.worken.auth.security.UserDetailsServiceImpl.login(..))", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, Exception ex) {
        LoginParam loginParam = (LoginParam) joinPoint.getArgs()[0];
        String errorMsg;
        if (ex instanceof ServiceException) {
            errorMsg = ex.getMessage();
        } else {
            errorMsg = "未知异常";
        }
        SysUserLoginLog.loginFail(loginParam.getUsername(), getIpAddress(), loginParam.getLoginType().name(), errorMsg);
    }

    private static String getIpAddress() {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        assert ra != null;
        HttpServletRequest request = ((ServletRequestAttributes) ra).getRequest();
        //未知ip
        String unknown = "unknown";
        String ip = request.getHeader("X-Real-IP");
        if (!StringUtils.isBlank(ip) && !unknown.equalsIgnoreCase(ip)) {
            return ip;
        }
        ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.isBlank(ip) && !unknown.equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个IP值，第一个为真实IP。
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        } else {
            return request.getRemoteAddr();
        }
    }
}
