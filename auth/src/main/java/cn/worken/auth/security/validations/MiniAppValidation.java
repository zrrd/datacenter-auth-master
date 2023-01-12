package cn.worken.auth.security.validations;

import cn.worken.auth.security.dto.LoginParam;
import cn.worken.auth.security.dto.LoginTypeEnum;
import cn.worken.auth.service.entity.SysUser;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 小程序校验
 *
 * @author shaoyijiong
 * @date 2020/8/7
 */
@Component
@Slf4j
@Order(1)
public class MiniAppValidation implements Validation {

    private final StringRedisTemplate stringRedisTemplate;
    private final PasswordValidation passwordValidation;

    public MiniAppValidation(StringRedisTemplate stringRedisTemplate,
        PasswordValidation passwordValidation) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.passwordValidation = passwordValidation;
    }

    @Override
    public boolean match(LoginParam loginParam) {
        return loginParam.getLoginType().name().startsWith("MINI_APP");
    }

    @Override
    public void doValidation(SysUser sysUser, LoginParam loginParam) {
        log.info("用户[{}]小程序[{}]登录", sysUser.getLoginName(), loginParam.getLoginType().name());
        // 通过微信账户登陆直接放行
        if (loginParam.isMiniAppLogin()) {
            return;
        }
        // 否则通过账户密码校验
        passwordValidation.doValidation(sysUser, loginParam);
        //将微信openId 与用户绑定 方便下次登陆 不需要输入密码
        String wxId = stringRedisTemplate.opsForValue().get("open:id:" + loginParam.getSessionId());
        if (wxId != null) {
            Map<String, String> map = Optional.ofNullable(sysUser.getWxId())
                .map(w -> JSON.parseObject(w, new TypeReference<HashMap<String, String>>() {
                })).orElse(Maps.newHashMap());
            // 增加一个类
            map.put(loginParam.getLoginType().name(), wxId);
            sysUser.setWxId(JSON.toJSONString(map));
            sysUser.updateById();
        }
    }
}
