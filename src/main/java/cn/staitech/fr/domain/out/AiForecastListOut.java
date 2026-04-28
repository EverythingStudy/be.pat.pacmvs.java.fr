package cn.staitech.fr.domain.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author wudi
 * @Date 2024/5/21 14:47
 * @desc
 */
@Data
public class AiForecastListOut {
    /**
     * 定量指标
     */
    @ApiModelProperty(name = "quantitativeIndicators", value = "定量指标")
    private String quantitativeIndicators;

    /**
     * 定量指标英文
     */
    @ApiModelProperty(name = "quantitativeIndicatorsEn", value = "定量指标英文")
    private String quantitativeIndicatorsEn;

    /**
     * 预测结果
     */
    @ApiModelProperty(name = "results", value = "预测结果")
    private String results;

    @ApiModelProperty(name = "averageValue", value = "平均值+标准差")
    private String averageValue;

    @ApiModelProperty(name = "normalDistribution", value = "正态分布95%")
    private String normalDistribution;

    @ApiModelProperty(name = "unit", value = "单位")
    private String unit;



}
