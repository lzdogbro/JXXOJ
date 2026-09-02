package top.hcode.hoj.pojo.entity.assignment;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 作业题目实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "AssignmentProblem对象", description = "作业题目实体")
public class AssignmentProblem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键id")
    private Long id;

    @ApiModelProperty(value = "作业id")
    private Long aid;

    @ApiModelProperty(value = "题目id")
    private Long pid;

    @ApiModelProperty(value = "作业内展示编号 A/B/C")
    private String displayId;

    @ApiModelProperty(value = "分值(预留,AC制暂用)")
    private Integer score;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

}
