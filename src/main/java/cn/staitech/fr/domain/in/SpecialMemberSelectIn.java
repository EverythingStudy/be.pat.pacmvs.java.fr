package cn.staitech.fr.domain.in;

import cn.staitech.common.core.domain.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author wudi
 * @Date 2024/4/1 13:42
 * @desc
 */
@Data
public class SpecialMemberSelectIn extends PageRequest {
    @NotNull(message = "专题id不能为空")
    @ApiModelProperty(value = "专题ID", required = true)
    private Long specialId;
    @ApiModelProperty(value = "用户名", required = false)
    private String userName;
    @ApiModelProperty(value = "姓名", required = false)
    private String nickName;
    @ApiModelProperty(value = "用户性别（0男 1女）", required = false)
    private String sex;
    @ApiModelProperty(value = "系统角色ID", required = false)
    private Long roleId;
}
