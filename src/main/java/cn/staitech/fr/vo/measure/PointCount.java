package cn.staitech.fr.vo.measure;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PointCount {

    @ApiModelProperty(value = "标签id")
    private Long category_id;

    @ApiModelProperty(value = "点数量")
    private Long point_count;

    @ApiModelProperty(value = "标签名称")
    private String label_name;
}
