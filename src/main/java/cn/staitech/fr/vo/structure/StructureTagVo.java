package cn.staitech.fr.vo.structure;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author mugw
 * @version 1.0
 * @description 结构标签
 * @date 2025/5/14 13:44:14
 */
@Data
public class StructureTagVo {

    @ApiModelProperty(value = "标签ID")
    @JsonProperty("categoryId")
    private Long structureTagId;

    @ApiModelProperty(required = true, value = "结构ID")
    @NotBlank(message = "{PathologicalIndicatorCategoryVO.structureId.isnull}")
    private String structureId;

    @ApiModelProperty(required = true, value = "结构名称")
    private String structureName;

    private String structureNameEn;

    @NotBlank(message = "{StatisticCategoryListOutVO.rgb.isnull}")
    @ApiModelProperty(required = true, value = "颜色RBG值")
    private String rgb;

    @NotBlank(message = "{StatisticCategoryListOutVO.rgb.isnull}")
    @ApiModelProperty(required = true, value = "颜色值HEX")
    private String hex;

    @NotNull(message = "{PathologicalIndicatorCategoryVO.indicatorId.isnull}")
    @ApiModelProperty(required = true, value = "结构指标ID")
    @JsonProperty("indicatorId")
    private Long structureTagSetId;

    @ApiModelProperty(value = "图层顺序")
    @NotNull(message = "{StatisticCategoryListOutVO.orderNumber.isnull}")
    private Integer orderNumber;

    @ApiModelProperty(value = "标签类型 0:下拉筛选标签；1:自定义标签")
    @JsonProperty("categoryType")
    private Integer type = 0;
}
