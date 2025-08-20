package cn.staitech.fr.vo.project.slide;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GetControlGroupReq {
    @ApiModelProperty(value = "项目id")
    private Long projectId;

}
