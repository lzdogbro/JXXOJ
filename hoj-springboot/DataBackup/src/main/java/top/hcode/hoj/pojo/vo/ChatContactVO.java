package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Description: 私聊联系人VO
 */
@ApiModel(value="私聊联系人", description="")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatContactVO {

    @ApiModelProperty(value = "联系人用户id")
    private String uid;

    @ApiModelProperty(value = "联系人用户名")
    private String username;

    @ApiModelProperty(value = "联系人头像")
    private String avatar;

    @ApiModelProperty(value = "联系人昵称")
    private String nickname;

    @ApiModelProperty(value = "最后一条消息内容")
    private String lastContent;

    @ApiModelProperty(value = "最后一条消息时间")
    private Date lastTime;

    @ApiModelProperty(value = "未读消息数")
    private Integer unreadCount;
}
