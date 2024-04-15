package cn.staitech.fr.domain.out;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Author wudi
 * @Date 2024/4/10 15:52
 * @desc
 */
@Data
public class MatrixReviewListOut {
    @ApiModelProperty(value = "单切片id")
    private Long singleId;

    @ApiModelProperty(value = "切片id")
    private Long slideId;

    @ApiModelProperty(value = "单脏器缩略图url地址")
    private String thumbUrl;

    @ApiModelProperty(value = "切片编号")
    private String imageName;

    @ApiModelProperty(value = "组别号")
    private String groupCode;

    @ApiModelProperty(value = "蜡块编号")
    private String waxCode;

    @ApiModelProperty(value = "动物编号")
    private String animalCode;

    @ApiModelProperty(value = "性别（M:雄；F:雌）")
    private String genderFlag;

    @ApiModelProperty(value = "启动者")
    private Long initiateBy;
    @ApiModelProperty(value = "启动者")
    private String userName;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty(value = "单脏器类型")
    private Long categoryId;

    @ApiModelProperty(value = "单脏器类型")
    private String organName;

    @ApiModelProperty(value = "单脏器类型-英文")
    private String organEn;

    @ApiModelProperty(value = "脏器数量")
    private Long organNumber;

    @ApiModelProperty(value = "0未预测、1预测成功、2预测失败、3预测中;ai预测字典标识符：ai_forecast_type")
    private String forecastStatus;

    @ApiModelProperty(value = "人工诊断状态 0：未诊断；1：已诊断;字典标识符：diagnosis_status")
    private String diagnosisStatus;

    @ApiModelProperty(value = "描述")
    private String description;

}
