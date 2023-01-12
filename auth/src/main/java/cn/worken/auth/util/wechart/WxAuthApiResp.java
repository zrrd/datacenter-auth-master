package cn.worken.auth.util.wechart;

import lombok.Data;

/**
 * 微信校验接口返回
 *
 * @author shaoyijiong
 * @date 2019/2/26
 */
@Data
public class WxAuthApiResp {

    /**
     * 用户唯一id
     */
    private String openid;
    /**
     * 用于从微信服务器获取微信信息
     */
    private String sessionKey;
    /**
     * 唯一id
     */
    private String unionid;

    /**
     * 微信接口返回异常
     */
    private Integer errcode;

    /**
     * 微信返回消息
     */
    private String errmsg;

}
