package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 作业未完成统计回传实体（navbar 角标 + 闪烁用）
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "作业未完成统计回传实体", description = "")
public class AssignmentUnfinishedVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "未完成的必做题目数（角标数字，仅统计截止前）")
    private Integer badgeCount;

    @ApiModelProperty(value = "是否继续闪烁（必做未完成任意截止，或选做未完成且未截止）")
    private Boolean flash;
}
