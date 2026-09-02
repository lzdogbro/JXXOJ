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
 * 家长-孩子绑定实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "ParentBinding对象", description = "家长-孩子绑定实体")
public class ParentBinding implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键id")
    private Long id;

    @ApiModelProperty(value = "家长openid")
    private String parentOpenid;

    @ApiModelProperty(value = "孩子平台uid")
    private String studentUid;

    @ApiModelProperty(value = "一次性绑定码")
    private String bindCode;

    @ApiModelProperty(value = "0待确认 1已绑定 2已解绑")
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}
