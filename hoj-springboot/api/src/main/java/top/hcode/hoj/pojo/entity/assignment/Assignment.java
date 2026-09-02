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
 * 作业实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "Assignment对象", description = "作业实体")
public class Assignment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "作业ID")
    private Long id;

    @ApiModelProperty(value = "作业标题")
    private String title;

    @ApiModelProperty(value = "作业说明")
    private String description;

    @ApiModelProperty(value = "创建者uid(管理员)")
    private String creatorUid;

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

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}
