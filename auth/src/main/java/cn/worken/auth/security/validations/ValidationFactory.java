package cn.worken.auth.security.validations;

import cn.worken.auth.security.dto.LoginParam;
import cn.worken.auth.security.exception.ServiceException;
import cn.worken.auth.service.entity.SysUser;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 获得校验工具类
 *
 * @author shaoyijiong
 * @date 2020/8/7
 */
@Component
public class ValidationFactory  {


    private final List<Validation> validations;

    public ValidationFactory(List<Validation> validations) {
        this.validations = validations;
    }


    /**
     * 实际校验
     */
    public void doValidation(SysUser user, LoginParam loginParam) {
        getValidation(loginParam).doValidation(user, loginParam);
    }

    /**
     * 根据登录条件获取 validation
     */
    private Validation getValidation(LoginParam loginParam) {
        for (Validation validation : validations) {
            if (validation.match(loginParam)) {
                return validation;
            }
        }
        throw new ServiceException("登录参数有误!");
    }
}
