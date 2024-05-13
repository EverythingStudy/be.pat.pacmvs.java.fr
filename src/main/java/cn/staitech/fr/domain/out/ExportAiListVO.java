package cn.staitech.fr.domain.out;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExportAiListVO {


    @ApiModelProperty(value = "定量指标")
    private String quantitativeIndicators;

    @ApiModelProperty(value = "预测结果")
    private String results;

    @ApiModelProperty(value = "参考范围")
    private String forecastRange;

    @ApiModelProperty(value = "单位")
    private String unit;

 
}