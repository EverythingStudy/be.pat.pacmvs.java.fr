package cn.staitech.fr.vo.project;

import cn.staitech.sft.logaudit.annotation.IgnoreLogField;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberInfo {
    @IgnoreLogField
    @ApiModelProperty(value = "项目成员id", required = true)
    private Long memberId;

    @ApiModelProperty(value = "系统姓名")
    private String nickName;

    @ApiModelProperty(value = "注册姓名")
    private String userName;

    @ApiModelProperty(value = "性别")
    private String sex;

    @ApiModelProperty(value = "手机")
    private String phonenumber;

    @ApiModelProperty(value = "系统角色")
    private String roleName;
}
