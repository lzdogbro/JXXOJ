package top.hcode.hoj.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 订阅消息授权上报请求
 */
@Data
public class WechatSubscribeDTO {

    @ApiModelProperty(value = "微信订阅消息模板 id")
    private String templateId;

    @ApiModelProperty(value = "事件类型：assignment_publish / assignment_done")
    private String eventType;
}
