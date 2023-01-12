package cn.worken.auth.web;

import cn.worken.auth.service.entity.SysUser;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author shaoyijiong
 * @date 2020/7/20
 */
@RestController
@RequestMapping("open")
public class OpenController extends BaseUserInfoController {

    private final StringRedisTemplate stringRedisTemplate;
    private final String redisUserInfo;

    public OpenController(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisUserInfo = "sys:user-info:";
    }

    /**
     * 登出接口
     */
    @RequestMapping("/logout")
    public void logout(@RequestParam(value = "loginType", required = false) String loginType, HttpServletResponse response) {
        // 删除 token 与 refresh_token
        deleteCookie("token", response);
        deleteCookie("refresh_token", response);
        // 删除用户缓存
        stringRedisTemplate.delete(redisUserInfo + getUserId());
        // 如果是小程序登陆的话删除用户绑定的小程序openid
        if (!Strings.isNullOrEmpty(loginType) && loginType.startsWith("MINI_APP")) {
            deleteWxOpenId(getUserId(), loginType);
        }
    }

    private void deleteWxOpenId(Integer userId, String loginType) {
        SysUser sysUser = new SysUser(userId).selectById();
        if (!Strings.isNullOrEmpty(sysUser.getWxId())) {
            JSONObject object = JSON.parseObject(sysUser.getWxId());
            object.remove(loginType);
            sysUser.setWxId(object.toJSONString());
            sysUser.updateById();
        }
    }

    private void deleteCookie(String name, HttpServletResponse response) {
        // 清空cookie
        Cookie cookie = new Cookie(name, null);
        cookie.setMaxAge(0);
        cookie.setHttpOnly(false);
        //设置根路径
        cookie.setPath("/");
        cookie.setSecure(false);
        response.addCookie(cookie);
    }
}
