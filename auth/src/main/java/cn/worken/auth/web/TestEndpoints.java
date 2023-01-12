package cn.worken.auth.web;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 以下两个接口只用来测试权限 业务上并无使用
 *
 * @author shaoyijiong
 */
@Profile("dev")
@RestController
public class TestEndpoints extends BaseUserInfoController {

    @Permission("A")
    @GetMapping("/product/1")
    public String getProduct(String id, HttpServletRequest request) {
        return "product id : " + id;
    }

    @GetMapping("/product/2")
    public String getProduct() {
        return "hello world";
    }

    @GetMapping("/order/1")
    public String getOrder(String id) {
        return "order id : " + id;
    }

}
