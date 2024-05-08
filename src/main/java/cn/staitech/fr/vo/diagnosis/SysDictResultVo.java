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
public class SysDictResultVo{

	
	@ApiModelProperty(name = "ddefinitionList" , value = "ddefinitionList")
	List<SysDictDataVo> ddefinitionList = new ArrayList<>();
	
	
	@ApiModelProperty(name = "gradeList" , value = "gradeList")
	List<SysDictDataVo> gradeList = new ArrayList<>();
	
	
	@ApiModelProperty(name = "visceraList" , value = "visceraList")
	List<VisceraVo> visceraList = new ArrayList<>();
}
