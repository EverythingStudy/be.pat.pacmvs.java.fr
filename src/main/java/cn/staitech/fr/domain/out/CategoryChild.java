package cn.staitech.fr.domain.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CategoryChild {
	@ApiModelProperty(name = "categoryName" , value = "标签名称")
	private String categoryName;
	@ApiModelProperty(name = "categoryNumber" , value = "标签数量")
	private Long categoryNumber;
	@ApiModelProperty(name = "categoryColour" , value = "标签颜色 0：黑色 1：红色")
	private int categoryColour;
}
