package cn.worken.auth.security.dto;

import cn.worken.auth.security.exception.ServiceException;
import cn.worken.auth.util.RSAUtils;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

/**
 * 登录参数
 *
 * @author shaoyijiong
 * @date 2020/7/1
 */
@Data
public class LoginParam {

    private String username;
    private String password;
    /**
     * 登录类型 方便区分是哪个服务登录的 , 也可以在业务端做来源校验
     */
    private LoginTypeEnum loginType;
    /**
     * MD5加密码/微信小程序code
     */
    private String code;
    /**
     * 小程序使用 用来标识小程序两次登录是同一个人 用户绑定微信openId
     */
    private String sessionId;

    private boolean miniAppLogin;

    /**
     * web端公钥设置
     */
    private final static String webPrivateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALE-BHZq2kjfSmliy8zsHthTlRpVKab6IVmxhY0c38EkeRLmmUPQvSgA8RwByx0ruC1fSaO32Hnyky-qwkF_ckl8lHdIWtyDXdIzYXKAKkKygJJX_B0JBHl62ON8jwwX-_mP8gUIkU-OBchmcOO6gdCdLb9GAqjZGX5NEGf0hTITAgMBAAECgYBFq2wTpCLTnDnivPwBPBtPy2MDq9Aom4-6cwZdFql5gT26STybpJSGAgDEkovllobaBueqXBzSqqBij0u2dUNVqadOfiRGYaH1pKpTLW6wTlQsrJbuDO8JVmP8etjz99gi6_noig4MD_YgNfUWvz4daP8zm7KXwVk1QOUTBQx8WQJBAOTVjbBXjSLmfQ31rtOyvGQbQuzAI3rjzfEDo4qn-w61K9p6j9baw67m-w3KqWJgi7CoNLIiDMS1DZK07uWCOXcCQQDGSIuhWLe7QHR5ELLaF184y_l7USzS-Uj5WTODx-T5sYtqJujB_WZMO3NDEnC4DtvGKq_I30DxgxdwQAa_DzNFAkB-shIBqWe2BAEHApiqExKlJkbWh8bdGlWCdG5OrZSd3wcJrsQGxBaFttMXHrbLhgKxa1_JpHqHSmraaBzAq2ofAkEAklRFX3wmk3Ucv5-YLiTja_n0EpK0frnjpcizoMQrci4ZkexHB6qnDQkupcf9EYg9nsAv3BEOT22MJZXKpMNl9QJBANdIAVsSVtiVzsjwDo_lIfmeyopS9JsL7yoSGKAzONYQziU00_Cq8w3e55r0DXvpCXD9OJtCgUv-CdD_UDvkzFc";

    /**
     * 封装参数
     */
    public static LoginParam getLoginParamWithRequest(Authentication authentication) {
        LoginParam loginParam = new LoginParam();
        loginParam.setUsername((String) authentication.getPrincipal());
        // RSA解密已加密密码
        try {
            RSAPrivateKey rsaPrivateKey = RSAUtils.getPrivateKey(webPrivateKey);
            String decPassword = RSAUtils.privateDecryptCommon((String) authentication.getCredentials(), rsaPrivateKey);
            loginParam.setPassword(decPassword);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | RuntimeException ne) {
            new ServiceException("无效登录");
        }
        // 额外参数封装
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        // 登录类型
        loginParam.setLoginType(LoginTypeEnum.valueOf(Optional.ofNullable(request.getParameter("loginType")).orElseThrow(() -> new ServiceException("登录类型为空"))));
        loginParam.setCode(request.getParameter("code"));
        loginParam.setSessionId(request.getParameter("sessionId"));
        return loginParam;
    }
}
