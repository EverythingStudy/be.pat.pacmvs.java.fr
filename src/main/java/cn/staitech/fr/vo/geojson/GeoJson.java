package cn.staitech.fr.vo.geojson;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class GeoJson {

    @ApiModelProperty(value = "type")
    private String type = "FeatureCollection";

    @ApiModelProperty(value = "标注数据")
    private List<Features> features;

}
