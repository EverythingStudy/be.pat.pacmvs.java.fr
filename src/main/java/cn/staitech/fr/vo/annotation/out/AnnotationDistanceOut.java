package cn.staitech.fr.vo.annotation.out;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gjt
 */
@Data
public class AnnotationDistanceOut {
    @ApiModelProperty(value = "轮廓点一")
    private JSONObject contourTypeOne;

    @ApiModelProperty(value = "轮廓点二")
    private JSONObject contourTypeTwo;

    @ApiModelProperty(value = "平均间距")
    private Double meanDistance;

    @ApiModelProperty(value = "最小间距")
    private Double minDistance;


}
