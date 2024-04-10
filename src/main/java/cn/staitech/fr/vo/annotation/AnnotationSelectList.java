package cn.staitech.fr.vo.annotation;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class AnnotationSelectList {

    @ApiModelProperty(value = "切片id", required = true)
    private Long slideId;

    @ApiModelProperty(value = "标注坐标")
    private JSONObject geometry;

    @ApiModelProperty(value = "标注坐标")
    private List<Long> structureSizeList;

    @ApiModelProperty(value = "分辨率")
    private Long magnification;

    @ApiModelProperty(value = "标签id")
    private Long categoryId;


}
