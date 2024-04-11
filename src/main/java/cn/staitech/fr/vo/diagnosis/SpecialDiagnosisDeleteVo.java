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
@Api(value = "人工诊断添加", tags = "人工诊断添加")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpecialDiagnosisDeleteVo {
	
	@ApiModelProperty(name = "specialDiagnosisId" , value = "诊断ID")
	private Long specialDiagnosisId;
    
	@ApiModelProperty(name = "specialId" , value = "专题id")
	private Long specialId;
    
	@ApiModelProperty(name = "projectId" , value = "项目ID")
	private Long projectId;
    
	@ApiModelProperty(name = "subImageId" , value = "新切片ID（切好的单脏器）")
	private Long subImageId;
	
	@ApiModelProperty(name = "groupId" , value = "分组id")
	private Long groupId;
    
	
    
}
