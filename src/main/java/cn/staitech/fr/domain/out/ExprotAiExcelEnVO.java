package cn.staitech.fr.domain.out;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ExprotAiExcelEnVO {

    @ApiModelProperty(value = "专题号")
    @ExcelProperty(index = 0, value = "Study No.")
    private String topicName;

    @ApiModelProperty(value = "项目名称")
    @ExcelProperty(index = 1, value = "Project Name")
    private String specialName;

    @ApiModelProperty(value = "图像名称")
    @ExcelProperty(index = 2, value = "Image Name")
    private String imageName;
    @ApiModelProperty(value = "脏器名称")
    @ExcelProperty(index = 3, value = "Tissue Name")
    private String organName;

    @ApiModelProperty(value = "量化指标")
    @ExcelProperty(index = 4, value = "Indicator Name")
    private String quantitativeIndicators;

    @ApiModelProperty(value = "预测结果")
    @ExcelProperty(index = 5, value = "Value")
    private String results;

    @ApiModelProperty(value = "单位")
    @ExcelProperty(index = 6, value = "Unit")
    private String unit;

    @ExcelIgnore
    @ApiModelProperty(value = "正态分布95%")
    private String normalDistribution;

    @ExcelProperty(index = 7, value = "D.D.I-Ctrl (Lower Bound)")
    private String lowerBound;

    @ExcelProperty(index = 8, value = "D.D.I-Ctrl (Upper Bound)")
    private String upperBound;

    @ApiModelProperty(value = "数值异常")
    @ExcelProperty(index = 9, value = "Abnormal Value")
    private String abnormalValue;

}
