package cn.staitech.fr.vo.project;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ProjectMemberVo {
    @NotNull(message = "{StartPredictionIn.specialId.isnull}")
    @ApiModelProperty(value = "项目id", required = true)
    private Long projectId;
    @NotEmpty(message = "{AnnotationDeleteVO.createBy.isnull}")
    @ApiModelProperty(value = "用户id列表", required = true)
    private List<Long> userId;

}
