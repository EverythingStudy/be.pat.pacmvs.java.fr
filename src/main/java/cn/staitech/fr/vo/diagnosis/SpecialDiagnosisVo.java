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
	private Long specialDiagnosisId;
    
	@ApiModelProperty(name = "specialId" , value = "专题id")
	private Long specialId;
    
	@ApiModelProperty(name = "projectId" , value = "项目ID")
	private Long projectId;
    
	@ApiModelProperty(name = "subImageId" , value = "新切片ID（切好的单脏器）")
	private Long subImageId;
	
	@ApiModelProperty(name = "groupId" , value = "分组id")
	private Long groupId;
    
	@ApiModelProperty(name = "remark" , value = "备注")
	private String remark;
    
	@ApiModelProperty(name = "diagnosisStatus" , value = "人工诊断状态：0未诊断，1已诊断")
	private Integer diagnosisStatus = 1;
    
	@ApiModelProperty(name = "status" , value = "是否可用 0:不可用1:可用")
	private Integer status;
    
	@ApiModelProperty(name = "deleteFlag" , value = "逻辑删除状态（0:删除 1:未删除）")
	private Integer deleteFlag;
    
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
	
	
	@ApiModelProperty(name = "position" , value = "部位")
	private List<Object> positionList =  new ArrayList<Object>();
	
	@ApiModelProperty(name = "positionName" , value = "部位标签名称")
	private List<Object> positionNameList =  new ArrayList<Object>();
	
	@ApiModelProperty(name = "positionWord" , value = "病理改变标签")
	private String positionWord;
	
	@ApiModelProperty(name = "ddefinition" , value = "修饰")
	private List<Object> ddefinitionList =  new ArrayList<Object>();
	
	@ApiModelProperty(name = "viscera" , value = "脏器或组织")
	private Long viscera;
	
	@ApiModelProperty(name = "lesion" , value = "病理改变")
	private Long lesion;
	
	@ApiModelProperty(name = "lesionName" , value = "病理改变标签")
	private String lesionName;
	
	
	@ApiModelProperty(name = "grade" , value = "病变级别")
	private Long grade;
	
	@ApiModelProperty(name = "editStatus" , value = "是否可以编辑 0:不可以1:可以")
	private Integer editStatus = 0;
	
	@ApiModelProperty(name = "diagnosticResults" , value = "诊断结果")
	private String diagnosticResults;
	
	@ApiModelProperty(name = "visceraList" , value = "visceraList")
	List<VisceraVo> visceraList = new ArrayList<>();
	
	@ApiModelProperty(name = "disable" , value = "是否展示")
	private boolean disable;
	
	@ApiModelProperty(name = "index" , value = "索引值")
	private Integer index;
	
    
}
