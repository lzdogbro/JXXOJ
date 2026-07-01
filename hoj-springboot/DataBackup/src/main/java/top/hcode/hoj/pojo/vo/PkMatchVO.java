package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Description: PK对战详情VO
 */
@ApiModel(value="PK对战详情", description="")
@Data
public class PkMatchVO {

    @ApiModelProperty(value = "对战ID")
    private Long id;

    @ApiModelProperty(value = "题目ID（内部）")
    private Long problemId;

    @ApiModelProperty(value = "题目展示ID")
    private String problemDisplayId;

    @ApiModelProperty(value = "题目名称")
    private String problemTitle;

    @ApiModelProperty(value = "发起者用户ID")
    private String initiatorUid;

    @ApiModelProperty(value = "发起者用户名")
    private String initiatorUsername;

    @ApiModelProperty(value = "发起者昵称")
    private String initiatorNickname;

    @ApiModelProperty(value = "发起者头像")
    private String initiatorAvatar;

    @ApiModelProperty(value = "发起者PK积分")
    private Integer initiatorPkScore;

    @ApiModelProperty(value = "对手用户ID")
    private String opponentUid;

    @ApiModelProperty(value = "对手用户名")
    private String opponentUsername;

    @ApiModelProperty(value = "对手昵称")
    private String opponentNickname;

    @ApiModelProperty(value = "对手头像")
    private String opponentAvatar;

    @ApiModelProperty(value = "对手PK积分")
    private Integer opponentPkScore;

    @ApiModelProperty(value = "对战状态: 0=等待接受, 1=进行中, 2=发起者胜, 3=对手胜, 4=平局, 5=已拒绝, 6=已取消")
    private Integer status;

    @ApiModelProperty(value = "胜者用户ID")
    private String winnerUid;

    @ApiModelProperty(value = "胜者用户名")
    private String winnerUsername;

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

    @ApiModelProperty(value = "剩余秒数（进行中时计算）")
    private Long remainingSeconds;

    @ApiModelProperty(value = "发起者分数变化")
    private Integer initiatorScoreChange;

    @ApiModelProperty(value = "对手分数变化")
    private Integer opponentScoreChange;

    @ApiModelProperty(value = "创建时间")
    private Date gmtCreate;
}
