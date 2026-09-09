package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 孩子信息（家长视角）
 */
@Data
public class ChildVO {

    @ApiModelProperty(value = "孩子平台 uid")
    private String studentUid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "真实姓名")
    private String realname;

    @ApiModelProperty(value = "学号")
    private String number;

    @ApiModelProperty(value = "学校")
    private String school;

    @ApiModelProperty(value = "绑定时间")
    private Date boundAt;
}
