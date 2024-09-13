package cn.staitech.fr.vo.annotation.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gjt
 */
@Data
public class DistanceGet {

    @ApiModelProperty(value = "标注id", required = true)
    private Long annotationIdOne;

    @ApiModelProperty(value = "标注id", required = true)
    private Long annotationIdTwo;

    @ApiModelProperty(value = "标注类型")
    private String annotationTypeOne;

    @ApiModelProperty(value = "标注类型")
    private String annotationTypeTwo;

    @ApiModelProperty(value = "轮廓类型")
    private Long contourTypeOne;

    @ApiModelProperty(value = "轮廓类型")
    private Long contourTypeTwo;

    @ApiModelProperty(value = "专题id", required = true)
    private Long specialId;

}
