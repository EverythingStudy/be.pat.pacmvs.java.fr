package cn.staitech.fr.domain.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AiInfoListRequest {

    @ApiModelProperty(value = "切片ID")
    private Long slideId;
    @ApiModelProperty(value = "项目ID")
    private Long projectId;
    @ApiModelProperty(value = "", hidden = true)
    private Long singleId;
    @ApiModelProperty(value = "", hidden = true)
    private String controlGroup;
    @ApiModelProperty(value = "", hidden = true)
    private Long singleSlideId;
}
