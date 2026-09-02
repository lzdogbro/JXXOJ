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
 * 作业下发快照与完成情况实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "AssignmentStudent对象", description = "作业下发快照与完成情况实体")
public class AssignmentStudent implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键id")
    private Long id;

    @ApiModelProperty(value = "作业id")
    private Long aid;

    @ApiModelProperty(value = "学生uid")
    private String uid;

    @ApiModelProperty(value = "冗余:是否必做,便于查\"必做未完成\"")
    private Integer isRequired;

    @ApiModelProperty(value = "0未完成 1已完成")
    private Integer status;

    @ApiModelProperty(value = "已AC题数")
    private Integer acceptedCount;

    @ApiModelProperty(value = "得分(预留)")
    private Integer score;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "下发时间")
    private Date gmtCreate;

    @ApiModelProperty(value = "完成时间")
    private Date gmtFinish;

}
