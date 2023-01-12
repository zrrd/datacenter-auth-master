package cn.worken.auth.security.validations;

import cn.worken.auth.security.dto.LoginParam;
import cn.worken.auth.service.entity.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 通用校验 只校验密码
 *
 * @author shaoyijiong
 * @date 2020/8/7
 */
@Slf4j
@Component
@Order
public class CommonValidation implements Validation {

    private final PasswordValidation passwordValidation;

    public CommonValidation(PasswordValidation passwordValidation) {
        this.passwordValidation = passwordValidation;
    }

    /**
     * 通用校验 , 如果前面的 Validation 都不满足的话进入该 validation
     */
    @Override
    public boolean match(LoginParam loginParam) {
        return true;
    }

    /**
     * 只做密码校验
     *
     * @param sysUser 用户信息
     * @param loginParam 登录参数
     */
    @Override
    public void doValidation(SysUser sysUser, LoginParam loginParam) {
        log.info("用户[{}],通用校验", sysUser.getLoginName());
        passwordValidation.doValidation(sysUser, loginParam);
    }
}
