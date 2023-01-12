package cn.worken.auth.security.validations;

import cn.worken.auth.security.dto.LoginParam;
import cn.worken.auth.security.exception.ServiceException;
import cn.worken.auth.service.entity.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码校验工具
 *
 * @author shaoyijiong
 * @date 2020/8/7
 */
@Slf4j
@Component
public class PasswordValidation {

    private final PasswordEncoder passwordEncoder;

    public PasswordValidation(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 密码校验功能
     */
    public void doValidation(SysUser sysUser, LoginParam loginParam) {
        if (!passwordEncoder.matches(loginParam.getPassword(), sysUser.getLoginPwd())) {
            throw new ServiceException("账户密码错误，请重新输入!");
        }
    }
}
