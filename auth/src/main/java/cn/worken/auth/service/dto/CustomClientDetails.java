package cn.worken.auth.service.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

/**
 * @author shaoyijiong
 * @date 2020/6/28
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CustomClientDetails extends BaseClientDetails {
    /**
     * 组织ID
     */
    private String comId;
    /**
     * 产品Code
     */
    private String confProductCode;
}
