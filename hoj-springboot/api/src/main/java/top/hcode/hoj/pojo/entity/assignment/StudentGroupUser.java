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
 * 学生组成员实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "StudentGroupUser对象", description = "学生组成员实体")
public class StudentGroupUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键id")
    private Long id;

    @ApiModelProperty(value = "学生组id")
    private Long gid;

    @ApiModelProperty(value = "学生uid")
    private String uid;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

}
