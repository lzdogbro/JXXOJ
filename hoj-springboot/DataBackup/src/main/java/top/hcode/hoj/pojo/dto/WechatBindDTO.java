package top.hcode.hoj.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 家长绑定孩子请求
 */
@Data
public class WechatBindDTO {

    @ApiModelProperty(value = "6 位一次性绑定码")
    private String bindCode;

    @ApiModelProperty(value = "孩子学号")
    private String number;

    @ApiModelProperty(value = "孩子真实姓名")
    private String realname;
}
