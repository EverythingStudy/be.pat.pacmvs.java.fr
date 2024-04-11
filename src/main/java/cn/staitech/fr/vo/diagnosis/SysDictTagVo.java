package cn.staitech.fr.vo.diagnosis;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @ClassName: SysDictTagVo
 * @Description:
 * @author wanglibei
 * @date 2023年7月11日
 * @version V1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysDictTagVo{


	@ApiModelProperty(name = "dictType" , value = "字典类型")
	private String dictType;

	@ApiModelProperty(value = "tagId",required = true)
	private String[] tagIdList;
	
	@ApiModelProperty(value = "tagId",required = true)
	private String tagId;
	
	@ApiModelProperty(name = "filter" , value = "filter")
	private String filter;
	

}
