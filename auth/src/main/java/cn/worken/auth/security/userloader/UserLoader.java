package cn.worken.auth.security.userloader;

import cn.worken.auth.security.dto.LoginParam;
import cn.worken.auth.service.entity.SysUser;

/**
 * @author shaoyijiong
 * @date 2020/8/7
 */
public interface UserLoader {

    /**
     * 查找用户
     *
     * @param loginParam 登录参数
     * @return 用户
     */
    SysUser loadUser(LoginParam loginParam);

    /**
     * 匹配
     *
     * @param loginParam 登录参数
     * @return true 表示匹配
     */
    boolean match(LoginParam loginParam);
}
