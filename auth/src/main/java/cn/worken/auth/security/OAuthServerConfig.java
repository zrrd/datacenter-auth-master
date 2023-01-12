package cn.worken.auth.security;


import cn.worken.auth.security.dto.ClientConstants;
import cn.worken.auth.security.dto.LoginUserInfo;
import cn.worken.auth.security.dto.UserConstants;
import cn.worken.auth.service.dto.CustomClientDetails;
import cn.worken.auth.util.RSAUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

/**
 * @author shaoyijiong
 */
@Configuration
public class OAuthServerConfig {

    private static final String DEMO_RESOURCE_ID = "order";

    /**
     * 资源服务器配置 (用于测试)
     */
    @Configuration
    @EnableResourceServer
    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) {
            resources.resourceId(DEMO_RESOURCE_ID).stateless(true);
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.exceptionHandling(h -> {
                h.accessDeniedHandler((httpServletRequest, httpServletResponse, e) -> {
                    throw e;
                });
                h.authenticationEntryPoint((httpServletRequest, httpServletResponse, e) -> {
                    throw e;
                });
            })
                .authorizeRequests()
                .antMatchers("/order/**")
                .authenticated();//配置order访问控制，必须认证过后才可以访问
        }
    }

    /**
     * 密码加密方式
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /**
     * 认证服务器配置
     */
    @Configuration
    @EnableAuthorizationServer
    protected static class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

        private final AuthenticationManager authenticationManager;
        private final ClientDetailsServiceAdaptImpl clientDetailsServiceAdapt;
        private final UserDetailsServiceImpl userDetailsService;

        public AuthorizationServerConfiguration(AuthenticationManager authenticationManager,
            ClientDetailsServiceAdaptImpl clientDetailsServiceAdapt,
            UserDetailsServiceImpl userDetailsService) {
            this.authenticationManager = authenticationManager;
            this.clientDetailsServiceAdapt = clientDetailsServiceAdapt;
            this.userDetailsService = userDetailsService;
        }


        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            // 手动配置
            clients.withClientDetails(clientDetailsServiceAdapt);
        }

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
            endpoints.accessTokenConverter(jwtAccessTokenConverter())
                // password 登录方式鉴权
                .authenticationManager(authenticationManager)
                .userDetailsService(userDetailsService)
                .exceptionTranslator(e -> {
                    throw e;
                })
                .allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST);
        }


        @Override
        public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
            //允许表单认证
            oauthServer.allowFormAuthenticationForClients();
        }


        @Bean
        public JwtAccessTokenConverter jwtAccessTokenConverter() {
            JwtAccessTokenConverter accessTokenConverter = new JwtAccessTokenConverter();
            // 自定义生成签名的key
            accessTokenConverter.setKeyPair(keyPair());
            // 自定义生成的签名
            accessTokenConverter.setAccessTokenConverter(new DefaultAccessTokenConverter() {
                @SneakyThrows
                @Override
                public Map<String, ?> convertAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
                    Map<String, Object> map = (Map<String, Object>) super.convertAccessToken(token, authentication);
                    map.remove("user_name");
                    if (token instanceof DefaultOAuth2AccessToken && authentication.getPrincipal() != null
                        && authentication.getPrincipal() instanceof LoginUserInfo) {
                        // password 自定义 jwt token
                        DefaultOAuth2AccessToken oAuth2AccessToken = (DefaultOAuth2AccessToken) token;
                        LoginUserInfo userInfo = (LoginUserInfo) authentication.getPrincipal();
                        map.put(UserConstants.USER_ID, userInfo.getUserId());
                        map.put(UserConstants.USER_TYPE, userInfo.getUserType());
                        map.put(UserConstants.NAME, userInfo.getUserName());
                        map.put(UserConstants.COM_ID, userInfo.getComId());
                        map.put(UserConstants.USER_NAME, userInfo.getLoginName());
                        map.put(UserConstants.SERVER, userInfo.getLoginType().getBelongServe().name());
                        map.put(UserConstants.PRODUCT_ID, userInfo.getProductId());
                        map.put(UserConstants.EXPIRES_IN, token.getExpiresIn());
                        // 自定义 额外参数
                        Map<String, Object> additionalInformation = oAuth2AccessToken.getAdditionalInformation();
                        additionalInformation.put("code", userInfo.getCode());
                        additionalInformation.put("message", userInfo.getMessage());
                    } else if (authentication.getOAuth2Request().getRefreshTokenRequest() != null) {
                        // refresh_token 自定义 jwt token
                        TokenRequest refreshTokenRequest = authentication.getOAuth2Request().getRefreshTokenRequest();
                        // 获取请求中的 refresh_token 再次转化为access token
                        String refreshToken = refreshTokenRequest.getRequestParameters().get("refresh_token");
                        JWTClaimsSet jwtClaimsSet = JWTParser.parse(refreshToken).getJWTClaimsSet();
                        map.put(UserConstants.USER_ID, jwtClaimsSet.getClaim(UserConstants.USER_ID));
                        map.put(UserConstants.USER_TYPE, jwtClaimsSet.getClaim(UserConstants.USER_TYPE));
                        map.put(UserConstants.NAME, jwtClaimsSet.getClaim(UserConstants.NAME));
                        map.put(UserConstants.COM_ID, jwtClaimsSet.getClaim(UserConstants.COM_ID));
                        map.put(UserConstants.USER_NAME, jwtClaimsSet.getClaim(UserConstants.USER_NAME));
                        map.put(UserConstants.SERVER, jwtClaimsSet.getClaim(UserConstants.SERVER));
                        map.put(UserConstants.PRODUCT_ID, jwtClaimsSet.getClaim(UserConstants.PRODUCT_ID));
                        map.put(UserConstants.EXPIRES_IN, token.getExpiresIn());
                    } else {
                        // client_credentials 自定义 jwt token
                        String clientId = authentication.getOAuth2Request().getClientId();
                        CustomClientDetails clientDetails =
                            (CustomClientDetails) clientDetailsServiceAdapt.loadClientByClientId(clientId);
                        map.put(ClientConstants.COM_ID, clientDetails.getComId());
                        map.put(ClientConstants.PRODUCT_CODE, clientDetails.getConfProductCode());
                    }
                    return map;
                }
            });
            return accessTokenConverter;
        }

        /**
         * rsa 密钥对
         */
        @SneakyThrows
        @Bean
        public KeyPair keyPair() {
            RSAPublicKey publicKey = RSAUtils.getPublicKey(PubKey.VALUE);
            RSAPrivateKey privateKey = RSAUtils.getPrivateKey(PriKey.VALUE);
            return new KeyPair(publicKey, privateKey);
        }


        /**
         * Rsa 公钥
         */
        public interface PubKey {

            String VALUE =
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCUHHjftKwmmTvhu1nheSfti5w5edO6SROcdBwHsqEFHdEwc-H4xauApJoe9AubUpDmCFBiuojsL9oS33LIfvjvddXzzIXYA3qJorLcSNu2Rid3Wev1wgXp3RC8qkoln26hXuw9ktyO7Rbmlvq9sIJWDHXDNzF_OePa1fTf3ErBKQIDAQAB";
        }

        /**
         * Rsa 密钥
         */
        public interface PriKey {

            String VALUE =
                "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAJQceN-0rCaZO-G7WeF5J-2LnDl507pJE5x0HAeyoQUd0TBz4fjFq4Ckmh70C5tSkOYIUGK6iOwv2hLfcsh--O911fPMhdgDeomistxI27ZGJ3dZ6_XCBendELyqSiWfbqFe7D2S3I7tFuaW-r2wglYMdcM3MX8549rV9N_cSsEpAgMBAAECgYAZdDc1wvge_hCzf543yLma8nZbzlsb1blAFxhGHmXb2j78Q74fIeGSjzdJgImKHQt37Q_bl9E2PhcNTUeNu3yOb7ZnSPogDuFNnAHE7JyGARKdB8ytdtmMtDopkfLg0YU94SWcIL7_tY-E_cjwAcpVQEQ_ZvgyrG2ddAkBWCXKsQJBAM6xGrdGBB_HBHPyPLPrGdaG9qO-PXlGanLP-q5pTJX-VsuWfh1qrY-HpLgOCJsZ8J6D2d6uNpOwpP96dmQabrUCQQC3cclkIiR29MPRR-6xnTQMse8Njn7EX1FHmAUlfEams4P1SX2UySCzBDt_6vZBMwy0E4NPHKtNhsnhsi5Y3V0lAkEAxXRLoWox5mmpx1pSw06FkHu-gw7qi0DJ0IT_Zj8R-vjO0g8iy9dtlwBuS6accg-F3uj26dRkLxNBjQ7gqyDVQQJACLdlZFai-OwEaetLEjFwCbJis3gz2czVpds5U2CmUsFw4bTOPMWZwifPU_KsJMjyL5RFxvqLwTFIYmr-_MX36QJAcs3sQnCZtq6TnRVcmGOU7LhyHH5QWPDf0iSZ7Pj2c5S8ZUqQfNjnMms2p6TGSqxmHx2DXT9E7H2fMJUS_-qlTA";
        }


    }


}
