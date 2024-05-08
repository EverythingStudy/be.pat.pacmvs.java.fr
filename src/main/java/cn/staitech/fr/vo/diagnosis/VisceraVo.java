package cn.staitech.fr.vo.diagnosis;
import java.util.ArrayList;
import java.util.List;

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
public class VisceraVo extends SysDictDataVo{
	
	@ApiModelProperty(name = "positionList" , value = "positionList")
	List<SysDictDataVo> positionList = new ArrayList<>();
	
	@ApiModelProperty(name = "lesionList" , value = "lesionList")
	List<SysDictDataVo> lesionList = new ArrayList<>();
	
}
