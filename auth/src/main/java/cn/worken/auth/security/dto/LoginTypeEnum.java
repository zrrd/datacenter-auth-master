package cn.worken.auth.security.dto;

/**
 * 登录来源校验
 *
 * @author shaoyijiong
 * @date 2020/7/28
 */
public enum LoginTypeEnum {

    /**
     * DMK 营销服务
     */
    DMK(ServerEnum.DMK),
    MINI_APP_DMK(ServerEnum.DMK),
    /**
     * 数据中台
     */
    DATA_CENTER(ServerEnum.DATA_CENTER),
    /**
     * 数据中台审核小程序
     */
    MINI_APP_ORDER(ServerEnum.DATA_CENTER),
    ;

    ServerEnum belongServe;

    public ServerEnum getBelongServe() {
        return belongServe;
    }

    LoginTypeEnum(ServerEnum belongServe) {
        this.belongServe = belongServe;
    }
}
