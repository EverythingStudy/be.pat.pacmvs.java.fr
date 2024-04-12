package cn.staitech.fr.vo.diagnosis;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
* @ClassName: TbSpecialDiagnosisDetail
* @Description:诊断明细
* @author wanglibei
* @date 2023年6月28日
* @version V1.0
 */
@Api(value = "诊断明细", tags = "诊断明细")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpecialDiagnosisDetailVo{

	
	@ApiModelProperty(name = "specialDiagnosisDetailId" , value = "诊断明细ID")
	private Long specialDiagnosisDetailId;
    
	@ApiModelProperty(name = "specialDiagnosisId" , value = "诊断ID")
	private Long specialDiagnosisId;
    
	@ApiModelProperty(name = "dictType" , value = "字典类型")
	private String dictType;
    
	@ApiModelProperty(name = "tags" , value = "标签列表")
	private String tags;
    
	@ApiModelProperty(name = "createTime" , value = "创建时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
    

}
