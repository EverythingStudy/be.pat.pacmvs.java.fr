package cn.staitech.fr.vo.diagnosis;

import java.util.List;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

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
public class SpecialDiagnosisAddVo {
	
	@ApiModelProperty(name = "specialDiagnosisId" , value = "诊断ID")
	private Long diagnosisId;
    
	@ApiModelProperty(name = "specialId" , value = "专题id")
	private Long specialId;
    
	@NotNull(message = "{PARAMETER_ABNORMALITY}")
	@ApiModelProperty(name = "singleId" , value = "新切片ID（切好的单脏器）")
	private Long singleId;
	
//	@ApiModelProperty(name = "groupId" , value = "分组id")
//	private Long groupId;
    
	@NotNull(message = "{PARAMETER_ABNORMALITY}")
	@ApiModelProperty(name = "viscera" , value = "脏器或组织")
	private String viscera;
    
	@NotNull(message = "{PARAMETER_ABNORMALITY}")
	@ApiModelProperty(name = "position" , value = "部位")
	private String position;
	
	@NotNull(message = "{PARAMETER_ABNORMALITY}")
	@ApiModelProperty(name = "lesion" , value = "病理改变")
	private String lesion;
	
	@NotNull(message = "{PARAMETER_ABNORMALITY}")
	@ApiModelProperty(name = "ddefinition" , value = "修饰")
	private List<String> ddefinition;
	
	@NotNull(message = "{PARAMETER_ABNORMALITY}")
	@ApiModelProperty(name = "grade" , value = "病变级别")
	private String grade;
	
    
	@ApiModelProperty(name = "remark" , value = "备注")
	private String remark;
    
}
