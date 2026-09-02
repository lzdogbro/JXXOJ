package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 学生组回传实体
 */
@ApiModel(value = "学生组回传实体", description = "")
@Data
public class StudentGroupVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "学生组id")
    private Long id;

    @ApiModelProperty(value = "组名")
    private String name;

    @ApiModelProperty(value = "组主uid")
    private String ownerUid;

    @ApiModelProperty(value = "组描述")
    private String description;

    @ApiModelProperty(value = "成员数量")
    private Integer memberCount;

    private Date gmtCreate;

    private Date gmtModified;
}
