package cn.staitech.fr.domain.in;

import java.util.List;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AlgorithmAnnIn{
	/*@ApiModelProperty(name = "specialId" , value = "专题ID")
	@NotNull(message = "专题id不能为空")
	private Long specialId;
    
	@ApiModelProperty(name = "specialImageId" , value = "专题切图ID")
	@NotNull(message = "专题切片id不能为空")
	private Long specialImageId;
	
	@ApiModelProperty(name = "imageId" , value = "图像ID")
	@NotNull(message = "图片id不能为空")
	private Long imageId;
	
	@ApiModelProperty(name = "geometryList" , value = "标注信息")
	private List<AlgorithmGeometry> geometryList;*/
	@ApiModelProperty(name = "slideIdList" , value = "切片id")
	private List<Long> slideIdList;
}

