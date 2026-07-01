package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Description: PK积分VO
 */
@ApiModel(value="PK积分", description="")
@Data
public class PkScoreVO {

    @ApiModelProperty(value = "用户ID")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "头像")
    private String avatar;

    @ApiModelProperty(value = "PK积分")
    private Integer pkScore;

    @ApiModelProperty(value = "总对战场次")
    private Integer totalMatches;

    @ApiModelProperty(value = "胜场")
    private Integer wins;

    @ApiModelProperty(value = "负场")
    private Integer losses;

    @ApiModelProperty(value = "平局")
    private Integer draws;
}
