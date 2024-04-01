package cn.staitech.fr.domain.in;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AlgorithmGeometry {
	// "Rectangle"
	@ApiModelProperty(value = "标注数据类型(LineString,Polygon,point,pc,p,L)")
	private String locationType;

	@ApiModelProperty(value = "标签名称")
	private String categoryname;

	@ApiModelProperty(value = "面积")
	private String area;

	@ApiModelProperty(value = "周长")
	private String perimeter;

	@ApiModelProperty(value = "标注坐标")
	private JSONObject geometry;

}
