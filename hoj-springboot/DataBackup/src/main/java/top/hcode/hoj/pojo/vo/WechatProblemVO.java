package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 家长侧作业题目状态
 */
@Data
public class WechatProblemVO {

    @ApiModelProperty(value = "题目 id")
    private Long pid;

    @ApiModelProperty(value = "作业内展示编号 A/B/C")
    private String displayId;

    @ApiModelProperty(value = "题目标题")
    private String title;

    @ApiModelProperty(value = "状态：AC / 已提交未AC / 未做")
    private String status;

    @ApiModelProperty(value = "分值")
    private Integer score;

    @ApiModelProperty(value = "最近提交时间")
    private Date submitTime;
}
