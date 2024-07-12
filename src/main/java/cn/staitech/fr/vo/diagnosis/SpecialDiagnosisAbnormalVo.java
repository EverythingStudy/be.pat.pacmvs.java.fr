package cn.staitech.fr.vo.diagnosis;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
* @ClassName: TbSpecialDiagnosis
* @Description:
* @author wanglibei
* @date 2023年6月28日
* @version V1.0
 */
@Api(value = "人工诊断未见异常", tags = "人工诊断未见异常")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpecialDiagnosisAbnormalVo {
	
	@NotNull(message = "{PARAMETER_ABNORMALITY}")
	@ApiModelProperty(name = "singleId" , value = "新切片ID（切好的单脏器）")
	private Long singleId;
	
	@ApiModelProperty(name = "abnormalStatus", value = "异常状态 0：取消 ；1：未见异常")
	private String abnormalStatus;
    
}
