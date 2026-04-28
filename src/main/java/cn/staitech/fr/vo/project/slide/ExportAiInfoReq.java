package cn.staitech.fr.vo.project.slide;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ExportAiInfoReq {
    @ApiModelProperty(value = "项目ID")
    private Long projectId;
    @ApiModelProperty(value = "单脏器切片id", hidden = true)
    private Long singleId;
    @ApiModelProperty(value = "单脏器类型id")
    private Long categoryId;
}
