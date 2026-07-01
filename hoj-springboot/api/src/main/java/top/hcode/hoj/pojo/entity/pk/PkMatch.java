package top.hcode.hoj.pojo.entity.pk;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Description: PK对战实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="PkMatch", description="")
public class PkMatch {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "题目ID")
    private Long problemId;

    @ApiModelProperty(value = "发起者用户ID")
    private String initiatorUid;

    @ApiModelProperty(value = "对手用户ID")
    private String opponentUid;

    @ApiModelProperty(value = "对战状态: 0=等待接受, 1=进行中, 2=发起者胜, 3=对手胜, 4=平局, 5=已拒绝, 6=已取消")
    private Integer status;

    @ApiModelProperty(value = "胜者用户ID")
    private String winnerUid;

    @ApiModelProperty(value = "发起者AC的提交ID")
    private Long initiatorSubmitId;

    @ApiModelProperty(value = "对手AC的提交ID")
    private Long opponentSubmitId;

    @ApiModelProperty(value = "对战开始时间")
    private Date startTime;

    @ApiModelProperty(value = "对战结束时间")
    private Date endTime;

    @ApiModelProperty(value = "投降者用户ID")
    private String surrenderUid;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;
}
