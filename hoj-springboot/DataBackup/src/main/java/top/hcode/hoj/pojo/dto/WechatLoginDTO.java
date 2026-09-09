package top.hcode.hoj.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 微信登录请求
 */
@Data
public class WechatLoginDTO {

    @ApiModelProperty(value = "wx.login() 返回的临时 code")
    private String code;

    @ApiModelProperty(value = "微信昵称，可选")
    private String nickname;

    @ApiModelProperty(value = "头像 URL，可选")
    private String avatar;
}
