package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 作业题目列表回传实体（学生视图）
 */
@ApiModel(value = "作业题目列表回传实体", description = "")
@Data
public class AssignmentProblemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "作业内展示编号 A/B/C")
    private String displayId;

    @ApiModelProperty(value = "题目id(problem.id)")
    private Long pid;

    @ApiModelProperty(value = "题目展示id(problem.problem_id)")
    private String problemId;

    @ApiModelProperty(value = "题目标题")
    private String title;

    @ApiModelProperty(value = "题目难度")
    private Integer difficulty;

    @ApiModelProperty(value = "题目类型")
    private Integer type;

    @ApiModelProperty(value = "分值(预留,AC制暂用)")
    private Integer score;

    @ApiModelProperty(value = "学生此题状态 0未AC 1已AC")
    private Integer status;
}
