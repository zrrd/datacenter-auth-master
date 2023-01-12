package cn.worken.auth.security.validations;

import cn.worken.auth.security.dto.LoginParam;
import cn.worken.auth.service.entity.SysUser;

/**
 * 检验通用类
 *
 * @author shaoyijiong
 * @date 2020/8/7
 */
public interface Validation {

    /**
     * 是否 match 到这个 validation
     *
     * @param loginParam 登录参数
     * @return true 匹配成功
     */
    boolean match(LoginParam loginParam);

    /**
     * 实际做校验
     *
     * @param sysUser 用户信息
     * @param loginParam 登录参数
     */
    void doValidation(SysUser sysUser, LoginParam loginParam);
}
