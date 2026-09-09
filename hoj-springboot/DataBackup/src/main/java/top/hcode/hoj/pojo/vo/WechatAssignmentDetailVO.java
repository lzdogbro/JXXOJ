package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 家长侧作业详情（含题目明细）
 */
@Data
public class WechatAssignmentDetailVO {

    @ApiModelProperty(value = "作业 id")
    private Long id;

    @ApiModelProperty(value = "作业标题")
    private String title;

    @ApiModelProperty(value = "作业说明")
    private String description;

    @ApiModelProperty(value = "是否必做 1必做 0选做")
    private Integer isRequired;

    @ApiModelProperty(value = "开始时间")
    private Date startTime;

    @ApiModelProperty(value = "截止时间")
    private Date endTime;

    @ApiModelProperty(value = "题目总数")
    private Integer problemCount;

    @ApiModelProperty(value = "孩子已 AC 题数")
    private Integer acceptedCount;

    @ApiModelProperty(value = "是否完成")
    private Boolean completed;

    @ApiModelProperty(value = "是否进行中")
    private Boolean isRunning;

    @ApiModelProperty(value = "是否已结束")
    private Boolean isEnded;

    @ApiModelProperty(value = "题目明细")
    private List<WechatProblemVO> problems;
}
