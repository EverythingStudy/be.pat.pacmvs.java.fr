package cn.staitech.fr.vo.project.slide;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ChangeControlGroupReq {

    @ApiModelProperty(value = "项目id")
    private Long projectId;

    @ApiModelProperty(value = "对照组")
    private String controlGroup;
}
