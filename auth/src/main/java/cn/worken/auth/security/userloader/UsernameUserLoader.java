package cn.worken.auth.security.userloader;

import cn.worken.auth.security.dto.LoginParam;
import cn.worken.auth.service.entity.SysUser;
import com.google.common.base.Strings;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author shaoyijiong
 * @date 2020/8/7
 */
@Order(1)
@Component
public class UsernameUserLoader implements UserLoader {

    @Override
    public SysUser loadUser(LoginParam loginParam) {
        return SysUser.loadUserByUsername(loginParam.getUsername());
    }

    @Override
    public boolean match(LoginParam loginParam) {
        return !Strings.isNullOrEmpty(loginParam.getUsername());
    }
}
