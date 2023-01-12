package cn.worken.auth.security.userloader;

import cn.worken.auth.security.dto.LoginParam;
import cn.worken.auth.security.exception.ServiceException;
import cn.worken.auth.service.entity.SysUser;
import cn.worken.auth.util.wechart.WxClient;
import com.google.common.base.Strings;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author shaoyijiong
 * @date 2020/8/7
 */
@Order(2)
@Component
public class MiniAppUserLoader implements UserLoader {

    private final StringRedisTemplate stringRedisTemplate;
    private final WxClient wxClient;

    public MiniAppUserLoader(StringRedisTemplate stringRedisTemplate, WxClient wxClient) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.wxClient = wxClient;
    }


    @Override
    public SysUser loadUser(LoginParam loginParam) {
        String openId = wxClient.wechartUserAuth(loginParam.getCode(), loginParam.getLoginType());
        SysUser sysUser = SysUser.loadUserByWxId(loginParam.getLoginType(), openId);
        if (sysUser == null) {
            // sessionId 返回给前端 下次登录是确定登录人
            String sessionId = UUID.randomUUID().toString();
            String openidIdPrefix = "open:id:";
            stringRedisTemplate.opsForValue().set(openidIdPrefix + sessionId, openId, 30, TimeUnit.MINUTES);
            throw new ServiceException(404, sessionId);
        }
        // 直接表示是微信小程序登录的 , 不是通过账户密码登录的
        loginParam.setMiniAppLogin(true);
        return sysUser;
    }

    /**
     * 小程序登陆 并且账户不为空
     */
    @Override
    public boolean match(LoginParam loginParam) {
        return loginParam.getLoginType().name().startsWith("MINI_APP")
            && Strings.isNullOrEmpty(loginParam.getUsername());
    }
}
