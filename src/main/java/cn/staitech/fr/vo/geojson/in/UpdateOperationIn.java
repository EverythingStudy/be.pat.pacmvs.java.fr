package cn.staitech.fr.vo.geojson.in;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author .
 */
@Data
public class UpdateOperationIn {

    @NotNull(message = "{DescriptionUpdateVO.annotationId.isnull}")
    @ApiModelProperty(value = "标注id")
    private Long marking_id;

    @ApiModelProperty(value = "标注作者(绘制者)")
    private Long update_by;

    @ApiModelProperty(value = "(新图形) 标注坐标")
    private JSONObject geometry;

    @NotBlank(message = "操作不可为空")
    @ApiModelProperty(value = "要执行的操作(UNION:相交,DIFFERENCE:相差,null)")
    private String operation;

    @NotNull(message = "校验不可为空")
    @ApiModelProperty(value = "校验")
    private Boolean check;

    @ApiModelProperty(value = "分辨率")
    private String resolution;
}
