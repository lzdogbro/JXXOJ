package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 学生组成员回传实体
 */
@ApiModel(value = "学生组成员回传实体", description = "")
@Data
public class StudentGroupUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "学生uid")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "真实姓名")
    private String realname;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "学号")
    private String number;

    @ApiModelProperty(value = "加入时间")
    private Date gmtCreate;
}
