package cn.worken.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author shaoyijiong
 * @date 2020/8/7
 */
@Configuration
public class BeanConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
