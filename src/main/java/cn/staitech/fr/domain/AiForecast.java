package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @TableName fr_ai_forecast
 */
@TableName(value = "fr_ai_forecast")
@Data
public class AiForecast implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long forecastId;

    /**
     * 单脏器切片id
     */
    @ApiModelProperty(name = "singleSlideId", value = "单脏器切片id")
    private Long singleSlideId;

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

    /**
     * 范围
     */
    @ApiModelProperty(name = "forecastRange", value = "范围")
    private String forecastRange;

    /**
     * 创建者
     */
    @ApiModelProperty(name = "createBy", value = "创建者")
    private Long createBy;

    /**
     * 创建时间
     */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String createTime;

    @ApiModelProperty(name = "unit", value = "单位")
    private String unit;

    @ApiModelProperty("结构指标类别\n" + "0：产品呈现指标\n" + "1：算法输出指标")
    private String structType;

    @ApiModelProperty("指标计算机构编码")
    private String structureIds;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}