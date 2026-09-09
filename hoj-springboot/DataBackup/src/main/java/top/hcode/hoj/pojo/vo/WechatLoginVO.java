package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 微信登录返回
 */
@Data
public class WechatLoginVO {

    @ApiModelProperty(value = "微信 JWT")
    private String token;

    @ApiModelProperty(value = "当前 openid")
    private String openid;

    @ApiModelProperty(value = "是否已绑定孩子")
    private Boolean hasChild;
}
