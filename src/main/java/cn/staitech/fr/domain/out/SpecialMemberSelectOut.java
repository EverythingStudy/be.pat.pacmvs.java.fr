package cn.staitech.fr.domain.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author wudi
 * @Date 2024/4/1 14:26
 * @desc
 */
@Data
public class SpecialMemberSelectOut {

    @ApiModelProperty(value = "成员id")
    private Long MemberId;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "系统姓名")
    private String userName;

    @ApiModelProperty(value = "注册姓名")
    private String nickName;

    @ApiModelProperty(value = "性别0：男1女")
    private String sex;

    @ApiModelProperty(value = "手机号")
    private String phonenumber;

}
