package cn.worken.auth.util.wechart;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序相关配置
 *
 * @author shaoyijiong
 * @date 2019/2/26
 */
@ConfigurationProperties(prefix = "miniprogram")
@Component
@Getter
@Setter
public class MiniProgramProperties {

    private List<MiniApp> apps;

    private Map<String, MiniApp> appMap;

    /**
     * 将对应
     */
    @PostConstruct
    public void init() {
        appMap = apps.stream().collect(Collectors.toMap(MiniApp::getKey, miniApp -> miniApp));
    }

    /**
     * 小程序配置
     */
    @Data
    static class MiniApp {

        private String key;
        private String appId;
        private String appSecret;
    }
}
