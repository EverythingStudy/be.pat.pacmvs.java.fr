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

    @ApiModelProperty(value = "单脏器id")
    private Long singleSlideId;

    @ApiModelProperty(value = "精细轮廓(true:之查看精细轮廓,false:将精细轮廓进行过滤)")
    private Boolean filigreeContour;




}
