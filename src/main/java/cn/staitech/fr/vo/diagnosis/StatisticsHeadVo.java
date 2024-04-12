package cn.staitech.fr.vo.diagnosis;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
* @ClassName: StatisticsHeadVo
* @Description:
* @author wanglibei
* @date 2023年7月18日
* @version V1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsHeadVo{

	
	@ApiModelProperty(name = "gender" , value = "gender编码")
	private Integer gender;
    
	@ApiModelProperty(name = "sex" , value = "性别")
	private String sex;
    
	@ApiModelProperty(name = "groupName" , value = "组名")
	private String groupName;
	
	@ApiModelProperty(name = "dosage" , value = "剂量")
	private String dosage;
    
    
	@ApiModelProperty(name = "total" , value = "统计总数")
	private Integer total;
	
	@ApiModelProperty(name = "sysVisceraName" , value = "sysVisceraName")
	private String sysVisceraName;
	
	@ApiModelProperty(name = "sysLesionName" , value = "sysLesionName")
	private String sysLesionName;
	
	@ApiModelProperty(name = "sysPositionName" , value = "sysPositionName")
	private String sysPositionName;

	@ApiModelProperty(name = "sysGradeName" , value = "sysGradeName")
	private String sysGradeName;
}
