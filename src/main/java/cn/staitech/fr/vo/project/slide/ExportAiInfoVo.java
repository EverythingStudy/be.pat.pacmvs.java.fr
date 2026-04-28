package cn.staitech.fr.vo.project.slide;

import cn.staitech.common.core.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ExportAiInfoVo {

    @ApiModelProperty(value = "专题号")
    @Excel(name = "专题号")
    private String topicName;

    @ApiModelProperty(value = "项目名称")
    @Excel(name = "项目名称")
    private String specialName;

    @ApiModelProperty(value = "图像名称")
    @Excel(name = "图像名称")
    private String imageName;

    @ApiModelProperty(value = "脏器名称")
    @Excel(name = "脏器名称")
    private String organName;

    @ApiModelProperty(value = "量化指标")
    @Excel(name = "量化指标")
    private String quantitativeIndicators;

    @ApiModelProperty(value = "数值")
    @Excel(name = "数值")
    private String results;

    @ApiModelProperty(value = "单位")
    @Excel(name = "单位")
    private String unit;


    @ApiModelProperty(value = "对照组数值分布区间")
    @Excel(name = "对照组数值分布区间")
    private String forecastRange;

}
