package cn.staitech.fr.domain.out;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ExprotAiExcelVO {

    @ApiModelProperty(value = "专题号")
    @ExcelProperty(index = 0, value = "专题号")
    private String topicName;

    @ApiModelProperty(value = "项目名称")
    @ExcelProperty(index = 1, value = "项目名称")
    private String specialName;

    @ApiModelProperty(value = "图像名称")
    @ExcelProperty(index = 2, value = "图像名称")
    private String imageName;
    @ApiModelProperty(value = "脏器名称")
    @ExcelProperty(index = 3, value = "脏器名称")
    private String organName;

    @ApiModelProperty(value = "量化指标")
    @ExcelProperty(index = 4, value = "量化指标")
    private String quantitativeIndicators;

    @ApiModelProperty(value = "预测结果")
    @ExcelProperty(index = 5, value = "数值")
    private String results;

    @ApiModelProperty(value = "单位")
    @ExcelProperty(index = 6, value = "单位")
    private String unit;

    @ExcelIgnore
    @ApiModelProperty(value = "正态分布95%")
    private String normalDistribution;

    @ExcelProperty(index = 7, value = "对照组数值分布区间（下限）")
    private String lowerBound;

    @ExcelProperty(index = 8, value = "对照组数值分布区间（上限）")
    private String upperBound;

    @ApiModelProperty(value = "数值异常")
    @ExcelProperty(index = 9, value = "数值异常")
    private String abnormalValue;

}
