package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 作业列表项与详情回传实体
 */
@ApiModel(value = "作业列表项与详情回传实体", description = "")
@Data
public class AssignmentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "作业id")
    private Long id;

    @ApiModelProperty(value = "作业标题")
    private String title;

    @ApiModelProperty(value = "作业说明")
    private String description;

    @ApiModelProperty(value = "创建者uid")
    private String creatorUid;

    @ApiModelProperty(value = "创建者用户名")
    private String creatorUsername;

    @ApiModelProperty(value = "是否必做 1必做 0选做")
    private Integer isRequired;

    @ApiModelProperty(value = "0草稿 1已发布")
    private Integer status;

    @ApiModelProperty(value = "开始时间")
    private Date startTime;

    @ApiModelProperty(value = "截止时间")
    private Date endTime;

    @ApiModelProperty(value = "软删除 0正常 1已删除")
    private Integer isDeleted;

    @ApiModelProperty(value = "题目总数")
    private Integer problemCount;

    @ApiModelProperty(value = "我的已AC题数(学生视图)")
    private Integer acceptedCount;

    @ApiModelProperty(value = "是否已完成(学生视图)")
    private Boolean completed;

    @ApiModelProperty(value = "是否进行中")
    private Boolean isRunning;

    @ApiModelProperty(value = "是否已结束")
    private Boolean isEnded;

    private Date gmtCreate;

    private Date gmtModified;
}
