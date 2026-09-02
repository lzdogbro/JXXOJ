package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 管理详情：作业下发学生完成统计回传实体
 */
@ApiModel(value = "作业下发学生完成统计回传实体", description = "")
@Data
public class AssignmentStudentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "学生uid")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "真实姓名")
    private String realname;

    @ApiModelProperty(value = "已AC题数")
    private Integer acceptedCount;

    @ApiModelProperty(value = "0未完成 1已完成")
    private Integer status;

    @ApiModelProperty(value = "得分(预留)")
    private Integer score;

    @ApiModelProperty(value = "完成时间")
    private Date gmtFinish;
}
