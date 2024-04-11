package cn.staitech.fr.vo.diagnosis;

import java.util.ArrayList;
import java.util.List;

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
    
//	@ApiModelProperty(name = "specialId" , value = "专题id")
//	private Long specialId;
    
    
//	@ApiModelProperty(name = "singleId" , value = "新切片ID（切好的单脏器）")
//	private Long singleId;
	
//	@ApiModelProperty(name = "groupId" , value = "分组id")
//	private Long groupId;
    
	
    
}
