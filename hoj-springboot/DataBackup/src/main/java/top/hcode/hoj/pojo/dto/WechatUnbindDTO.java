package top.hcode.hoj.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 家长解绑孩子请求
 */
@Data
public class WechatUnbindDTO {

    @ApiModelProperty(value = "要解绑的孩子 uid")
    private String studentUid;
}
