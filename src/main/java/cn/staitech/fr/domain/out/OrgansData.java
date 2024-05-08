package cn.staitech.fr.domain.out;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author wudi
 * @Date 2024/4/11 16:58
 * @desc
 */
@Data
public class OrgansData {

    @ApiModelProperty(value = "单脏器切片id")
    private Long singleId;

    @ApiModelProperty(value = "切片id")
    private Long slideId;

    @ApiModelProperty(value = "单脏器图片缩略图地址")
    private String thumbUrl;

    @ApiModelProperty(value = "单脏器类型")
    private Long categoryId;

    @ApiModelProperty(value = "切片编号")
    private String fileName;

    @ApiModelProperty(value = "0未预测、1预测成功、2预测失败、3预测中;ai预测字典标识符：ai_forecast_type")
    private String forecastStatus;

    @ApiModelProperty(value = "人工诊断状态 0：未诊断；1：已诊断;字典标识符：diagnosis_status")
    private String diagnosisStatus;
}
