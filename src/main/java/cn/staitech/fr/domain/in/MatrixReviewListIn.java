package cn.staitech.fr.domain.in;

import cn.staitech.common.core.domain.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

import com.baomidou.mybatisplus.annotation.TableField;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author wudi
 * @Date 2024/4/11 11:02
 * @desc
 */
@Data
public class MatrixReviewListIn extends PageRequest {

    @ApiModelProperty(value = "专题id")
    @NotNull(message = "[专题id]不能为空")
    private Long specialId;

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
    
    @ApiModelProperty(value = "0未预测、1预测成功、2预测失败、3预测中;")
    private List<Integer> aiStatusFineList;

    @ApiModelProperty(value = "人工诊断状态 0：未诊断；1：已诊断;字典标识符：diagnosis_status")
    private String diagnosisStatus;
    
    @ApiModelProperty(value = "0未预测、1预测成功、2预测失败、3预测中")
    private List<String> forecastStatusList;

    @ApiModelProperty(value = "时间范围")
    private Map<String, Date> createTimeParams;

    @ApiModelProperty(value = "排序字段：动物编号-animal_code；脏器-category_id")
    private String sortField;

    @ApiModelProperty(value = "排序方式：asc-升序；desc-降序")
    private String sortType;
    
    @ApiModelProperty(value = "标签列表")
    private List<Long> categoryIdList;

    @ApiModelProperty(value = "AI精细轮廓：0未预测、1预测成功、2预测失败、3预测中;")
    private String aiStatusFine;
}
