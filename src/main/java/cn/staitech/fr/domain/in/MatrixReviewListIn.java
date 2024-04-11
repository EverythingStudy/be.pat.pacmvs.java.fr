package cn.staitech.fr.domain.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;
import java.util.Map;

/**
 * @Author wudi
 * @Date 2024/4/11 11:02
 * @desc
 */
@Data
public class MatrixReviewListIn {
    @ApiModelProperty(value = "动物编号")
    private String animalCode;

    @ApiModelProperty(value = "切片编号")
    private String imageName;

    @ApiModelProperty(value = "组号")
    private String groupCode;

    @ApiModelProperty(value = "脏器类型")
    private Long categoryId;

    @ApiModelProperty(value = "0未预测、1预测成功、2预测失败、3预测中;ai预测字典标识符：ai_forecast_type")
    private String forecastStatus;

    @ApiModelProperty(value = "人工诊断状态 0：未诊断；1：已诊断;字典标识符：diagnosis_status")
    private String diagnosisStatus;

    @ApiModelProperty(value = "时间范围")
    private Map<String, Date> createTimeParams;
}
