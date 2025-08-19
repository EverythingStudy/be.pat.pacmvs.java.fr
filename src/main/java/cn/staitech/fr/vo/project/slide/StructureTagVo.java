package cn.staitech.fr.vo.project.slide;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class StructureTagVo {

    @ApiModelProperty(value = "结构标签id")
    private Long structureTagId;

    @ApiModelProperty(value = "结构标签集ID")
    private Long structureTagSetId;

    @ApiModelProperty(value = "标注类别名称")
    private String structureTagName;

    @ApiModelProperty(value = "结构ID")
    private String structureId;

    @ApiModelProperty(value = "颜色的RGB值")
    private String rgb;

    @ApiModelProperty(value = "颜色的HEX值")
    private String hex;

    @ApiModelProperty(value = "颜色名称")
    private String color;

    @ApiModelProperty(value = "图层顺序")
    private Integer orderNumber;
}
