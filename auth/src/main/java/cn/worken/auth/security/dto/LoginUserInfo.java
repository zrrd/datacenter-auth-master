package cn.worken.auth.security.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录后返回前台基础信息
 *
 * @author shaoyijiong
 * @date 2020/7/1
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserInfo {

    private String loginName;
    /**
     * 用户id
     */
    private Integer userId;
    /**
     * 用户姓名
     */
    private String userName;
    /**
     * 用户类型
     */
    private String userType;
    /**
     * 组织id
     */
    private String comId;
    /**
     * 组织名
     */
    private String companyName;
    /**
     * 用户权限列表
     */
    private ResTypeDto res;
    /**
     * 登录来源
     */
    private LoginTypeEnum loginType;

    private Integer productId;
    /**
     * 自定义code
     */
    @JsonIgnore
    private Integer code = 200;

    /**
     * 自定义响应
     */
    @JsonIgnore
    private String message = "登录成功";

    public void checkSuccess() {
        this.code = 200;
        this.message = "登录成功";
    }
}
