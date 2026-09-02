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
 * 微信身份映射实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "WechatUser对象", description = "微信身份映射实体")
public class WechatUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "openid", type = IdType.INPUT)
    @ApiModelProperty(value = "微信openid")
    private String openid;

    @ApiModelProperty(value = "平台账号uid,可空")
    private String uid;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "头像")
    private String avatar;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

}
