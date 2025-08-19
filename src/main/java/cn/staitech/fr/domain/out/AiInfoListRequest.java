package cn.staitech.fr.domain.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AiInfoListRequest {

    @ApiModelProperty(value = "切片ID")
    private Long slideId;
    @ApiModelProperty(value = "项目ID")
    private Long projectId;
    @ApiModelProperty(hidden = true)
    private Long singleId;
    @ApiModelProperty(hidden = true)
    private String controlGroup;
    @ApiModelProperty(hidden = true)
    private Long singleSlideId;
}
