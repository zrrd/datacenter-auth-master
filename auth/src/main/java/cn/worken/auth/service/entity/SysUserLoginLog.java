package cn.worken.auth.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户登录日志
 * </p>
 *
 * @author jobob
 * @since 2019-12-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysUserLoginLog extends Model<SysUserLoginLog> {

    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 登录名
     */
    private String loginName;
    /**
     * 登录ip
     */
    private String ip;
    /**
     * 登录类型
     */
    private String loginType;
    /**
     * 登录时间
     */
    private LocalDateTime loginTime;
    /**
     * 1-成功 -1-失败
     */
    private Integer status;
    /**
     * 接口返回结果
     */
    private String result;

    /**
     * 登录成功 日志
     */
    public static void loginSuccess(String loginName, String ip, String loginType) {
        SysUserLoginLog sysUserLoginLog = new SysUserLoginLog();
        sysUserLoginLog
            .setLoginName(loginName)
            .setIp(ip)
            .setLoginType(loginType)
            .setLoginTime(LocalDateTime.now())
            .setStatus(1)
            .setResult("登录成功");
        sysUserLoginLog.insert();
    }

    /**
     * 登录失败 日志
     */
    public static void loginFail(String loginName, String ip, String loginType, String errorMsg) {
        SysUserLoginLog sysUserLoginLog = new SysUserLoginLog();
        sysUserLoginLog
            .setLoginName(loginName)
            .setIp(ip)
            .setLoginType(loginType)
            .setLoginTime(LocalDateTime.now())
            .setStatus(-1)
            .setResult(errorMsg);
        sysUserLoginLog.insert();
    }
}
