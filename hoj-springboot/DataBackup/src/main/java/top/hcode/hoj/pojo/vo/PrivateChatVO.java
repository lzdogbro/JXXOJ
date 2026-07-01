package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Description: 私聊消息VO
 */
@ApiModel(value="私聊消息", description="")
@Data
public class PrivateChatVO {

    @ApiModelProperty(value = "消息id")
    private Long id;

    @ApiModelProperty(value = "发送者id")
    private String senderId;

    @ApiModelProperty(value = "发送者用户名")
    private String senderUsername;

    @ApiModelProperty(value = "发送者头像")
    private String senderAvatar;

    @ApiModelProperty(value = "发送者昵称")
    private String senderNickname;

    @ApiModelProperty(value = "消息内容")
    private String content;

    @ApiModelProperty(value = "是否已读")
    private Boolean state;

    @ApiModelProperty(value = "发送时间")
    private Date gmtCreate;
}
