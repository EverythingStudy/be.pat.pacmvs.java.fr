package cn.staitech.fr.vo.project.slide;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SlideSelectListReq {

    @ApiModelProperty(value = "项目ID")
    private String projectId;
}
