package cn.staitech.fr.vo.project;

import cn.staitech.common.core.domain.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
public class ProjectMemberPageReq extends PageRequest {
    @NotNull(message = "项目id不能为空")
    @ApiModelProperty(value = "项目ID", required = true)
    private Long projectId;
    @ApiModelProperty(value = "用户名", required = false)
    private String userName;
    @ApiModelProperty(value = "姓名", required = false)
    private String nickName;
    @ApiModelProperty(value = "用户性别（0男 1女）", required = false)
    private String sex;
    @ApiModelProperty(value = "系统角色ID", required = false)
    private Long roleId;
}
