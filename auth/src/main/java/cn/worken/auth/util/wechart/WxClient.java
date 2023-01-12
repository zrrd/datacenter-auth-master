package cn.worken.auth.util.wechart;

import cn.worken.auth.security.dto.LoginTypeEnum;
import cn.worken.auth.util.wechart.MiniProgramProperties.MiniApp;
import com.alibaba.fastjson.JSON;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

/**
 * @author shaoyijiong
 * @date 2020/8/7
 */
@Component
public class WxClient {

    private final RestTemplate restTemplate;
    private final MiniProgramProperties programProperties;

    /**
     * 调用路径
     */
    private static final String WX_AUTH_URL =
        "https://api.weixin.qq.com/sns/jscode2session?appid={AppId}&secret={AppSecret}&js_code={code}&grant_type=authorization_code";


    /**
     * 注入
     */
    public WxClient(MiniProgramProperties programProperties, RestTemplate restTemplate) {
        this.programProperties = programProperties;
        this.restTemplate = restTemplate;
    }


    /**
     * 从微信服务器校验用户
     *
     * @param code 微信端传过来的用户码
     */
    public String wechartUserAuth(String code, LoginTypeEnum loginType) {
        //根据登录类型 获取是哪个小程序
        MiniApp miniApp = programProperties.getAppMap().get(loginType.name());
        String result = restTemplate
            .getForObject(WX_AUTH_URL, String.class, miniApp.getAppId(), miniApp.getAppSecret(), code);
        try {
            WxAuthApiResp resp = JSON.parseObject(result, WxAuthApiResp.class);
            Assert.notNull(resp, "json转换异常");
            if (resp.getErrcode() != null) {
                throw new BadCredentialsException(resp.getErrmsg());
            }
            return resp.getOpenid();
        } catch (Exception e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

}
