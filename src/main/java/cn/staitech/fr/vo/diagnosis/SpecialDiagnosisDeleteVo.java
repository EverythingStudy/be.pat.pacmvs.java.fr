package cn.staitech.fr.vo.diagnosis;

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
@Api(value = "人工诊断删除", tags = "人工诊断删除")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpecialDiagnosisDeleteVo {
	
	@ApiModelProperty(name = "diagnosisId" , value = "诊断ID")
	private Long diagnosisId;
    
}
