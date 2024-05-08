package cn.staitech.fr.vo.diagnosis;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

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
@Api(value = "人工诊断", tags = "人工诊断")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpecialDiagnosisVo {

	
	@ApiModelProperty(name = "specialDiagnosisId" , value = "诊断ID")
	private Long diagnosisId;
    
	@ApiModelProperty(name = "specialId" , value = "专题id")
	private Long specialId;
    
	@ApiModelProperty(name = "slideId" , value = "新切片ID（切好的单脏器）")
	private Long slideId;
    
	@ApiModelProperty(name = "remark" , value = "备注")
	private String remark;
    
	@ApiModelProperty(name = "createBy" , value = "创建人id")
	private Long createBy;
	@ApiModelProperty(name = "createUser" , value = "创建人id")
	private String createUser;
    
	@ApiModelProperty(name = "createTime" , value = "创建时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
    
	@ApiModelProperty(name = "updateBy" , value = "更新人id")
	private Long updateBy;
    
	@ApiModelProperty(name = "updateTime" , value = "更新时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;
	
	
	@ApiModelProperty(name = "viscera" , value = "脏器或组织")
	private String viscera;
	
	@ApiModelProperty(name = "viscera" , value = "部位")
	private String position;
	
	@ApiModelProperty(name = "positionSource" , value = "部位来源 0：字典  1：自定义")
	private String positionSource;
	
	@ApiModelProperty(name = "lesion" , value = "病理改变")
	private String lesion;
	
	@ApiModelProperty(name = "lesionSource" , value = "病理改变来源 0：字典  1：自定义")
	private String lesionSource;
	
	@ApiModelProperty(name = "ddefinition" , value = "修饰")
	private List<String> ddefinition;
	
	@ApiModelProperty(name = "ddefinitionSource" , value = "修饰来源 0：字典  1：自定义")
	private String ddefinitionSource;
	
	@ApiModelProperty(name = "grade" , value = "病变级别")
	private String grade;
	
	@ApiModelProperty(name = "gradeSource" , value = "病变级别来源 0：字典  1：自定义")
	private String gradeSource;
	
	@ApiModelProperty(name = "editStatus" , value = "是否可以编辑 0:不可以1:可以")
	private Integer editStatus = 0;
	
	
	
    
}
