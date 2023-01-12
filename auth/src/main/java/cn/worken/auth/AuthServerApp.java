package cn.worken.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


/**
 * @author syj
 */
@EnableConfigurationProperties
@EnableDiscoveryClient
@SpringBootApplication
@EnableAspectJAutoProxy
@MapperScan("cn.worken.auth.service.mapper")
public class AuthServerApp {

    public static void main(String[] args) {
        SpringApplication.run(AuthServerApp.class, args);
    }

}
