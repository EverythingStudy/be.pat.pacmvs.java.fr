package cn.staitech.fr.vo.project.slide;

import cn.staitech.common.core.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiInfoListVO {


    @ApiModelProperty(value = "量化指标")
    @Excel(name = "量化指标")
    private String quantitativeIndicators;

    @ApiModelProperty(value = "数值")
    @Excel(name = "数值")
    private String results;

    @ApiModelProperty(value = "单位")
    @Excel(name = "单位")
    private String unit;

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private Integer categoryId;

    @ApiModelProperty(name = "normalDistribution", value = "正态分布95%")
    private String normalDistribution;

    @ApiModelProperty(hidden = true)
    @JsonIgnore
    private String structureIds;

    @ApiModelProperty(name = "structureTagIds", value = "结构ID列表")
    private List<Long> structureTagIds = new ArrayList<>();

    /**
     * 是否红色底纹高亮整行
     */
    @ApiModelProperty(value = "是否红色底纹高亮整行")
    private Boolean redHighlight;
}
