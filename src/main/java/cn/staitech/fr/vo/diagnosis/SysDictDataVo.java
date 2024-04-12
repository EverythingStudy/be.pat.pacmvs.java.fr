package cn.staitech.fr.vo.diagnosis;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
* @ClassName: SysDictData
* @Description:
* @author wanglibei
* @date 2023年6月27日
* @version V1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysDictDataVo{

	
	@ApiModelProperty(name = "dictCode" , value = "字典编码")
	private Long dictCode;
    
	@ApiModelProperty(name = "dictSort" , value = "字典排序")
	private Integer dictSort;
    
	@ApiModelProperty(name = "dictValue" , value = "字典键值")
	private String dictValue;
	
	@ApiModelProperty(name = "dictLabel" , value = "字典标签")
	private String dictLabel;
    
	@ApiModelProperty(name = "dictLabelEn" , value = "字典标签(英语)")
	private String dictLabelEn;
    
    
	@ApiModelProperty(name = "dictType" , value = "字典类型")
	private String dictType;
    
	@ApiModelProperty(name = "filter" , value = "过滤条件")
	private String filter;
    
	@ApiModelProperty(name = "dictValueInt" , value = "字典键值2")
	private Integer dictValueInt;

}
