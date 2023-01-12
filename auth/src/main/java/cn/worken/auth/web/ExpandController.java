package cn.worken.auth.web;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author shaoyijiong
 * @date 2020/7/20
 */
@RestController
public class ExpandController {

    private final PasswordEncoder passwordEncoder;

    public ExpandController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 创建新密码  crm 模块调用 内部
     */
    @PostMapping("password/create")
    public String createPassword(@RequestBody String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * 生成随机账户
     */
    @PostMapping("random-app")
    public String openApp() {
        String appKey = RandomStringUtils.randomAlphabetic(8, 12);
        String appSecret = RandomStringUtils.randomAlphabetic(16, 20);
        String encodeSecret = passwordEncoder.encode(appSecret);
        return JSON
            .toJSONString(ImmutableMap.of("appKey", appKey, "appSecret", appSecret, "encodeSecret", encodeSecret));
    }
}
