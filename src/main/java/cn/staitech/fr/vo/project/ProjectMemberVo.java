package cn.staitech.fr.vo.project;

import cn.staitech.sft.logaudit.annotation.IgnoreLogField;
import cn.staitech.sft.logaudit.req.LogAuditBaseReq;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ProjectMemberVo extends LogAuditBaseReq {
    @IgnoreLogField
    @NotNull(message = "{StartPredictionIn.specialId.isnull}")
    @ApiModelProperty(value = "项目id", required = true)
    private Long projectId;
    @IgnoreLogField
    @NotEmpty(message = "{AnnotationDeleteVO.createBy.isnull}")
    @ApiModelProperty(value = "用户id列表", required = true)
    private List<Long> userId;
    @ApiModelProperty(value = "项目成员列表")
    private List<ProjectMemberInfo> projectMemberInfos;

}
