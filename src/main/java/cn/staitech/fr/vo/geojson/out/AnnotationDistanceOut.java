package cn.staitech.fr.vo.geojson.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AnnotationDistanceOut {
    @ApiModelProperty(value = "轮廓点一")
    private String contourTypeOne;

    @ApiModelProperty(value = "轮廓点二")
    private String contourTypeTwo;

    @ApiModelProperty(value = "平均间距")
    private Double meanDistance;

    @ApiModelProperty(value = "最小间距")
    private Double minDistance;


}
