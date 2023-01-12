package cn.worken.auth.security.userloader;

import cn.worken.auth.security.dto.LoginParam;
import cn.worken.auth.security.exception.ServiceException;
import cn.worken.auth.service.entity.SysUser;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * @author shaoyijiong
 * @date 2020/8/7
 */
@Component
public class UserLoaderFactory {

    private final List<UserLoader> userLoaders;

    public UserLoaderFactory(List<UserLoader> userLoaders) {
        this.userLoaders = userLoaders;
    }

    /**
     * 查找用户
     */
    public SysUser loadUser(LoginParam loginParam) {
        for (UserLoader userLoader : userLoaders) {
            if (userLoader.match(loginParam)) {
                return userLoader.loadUser(loginParam);
            }
        }
        throw new ServiceException("登录参数有误!");
    }
}
