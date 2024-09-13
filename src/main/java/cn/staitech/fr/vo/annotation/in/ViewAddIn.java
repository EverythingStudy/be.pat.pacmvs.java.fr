package cn.staitech.fr.vo.annotation.in;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ViewAddIn {
    @NotNull(message = "{DescriptionUpdateVO.annotationId.isnull}")
    @ApiModelProperty(value = "标注id")
    private String marking_id;

    // @NotNull(message = "{MarkingDelIn.slideId.notNull}")
    @ApiModelProperty(value = "切片id")
    private Long slide_id;

    @ApiModelProperty(value = "标注作者(绘制者)")
    private Long update_by;

    // @NotNull(message = "{viewAddIn.createBy.notNull}")
    @ApiModelProperty(value = "标注作者(绘制者)")
    private Long create_by;

    @ApiModelProperty(value = "面积")
    private String area;

    @ApiModelProperty(value = "周长")
    private String perimeter;

    @ApiModelProperty(value = "标注颜色id")
    private Long category_id;

    @ApiModelProperty(value = "标注数据类型(LineString,Polygon,point,pc,p,L)")
    private String location_type;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "标注坐标")
    private JSONObject geometry;

    @ApiModelProperty(value = "测量轮廓类型(0:正常,表示有关系,默认为0")
    private Integer measure_type;

    @ApiModelProperty(value = "测量关系")
    private String measure_relation;

    @ApiModelProperty(value = "测量轮廓表示名称:L")
    private String measure_name;

    @ApiModelProperty(value = "测量轮廓标识：1")
    private Integer measure_number;

    @ApiModelProperty(value = " 周长（圆）")
    private String radius;

    @ApiModelProperty(value = "平均间距")
    private Double mean_distance;

    @ApiModelProperty(value = "最大间距")
    private Double max_distance;

    @ApiModelProperty(value = "最小间距")
    private Double min_distance;

    @ApiModelProperty(value = "内角")
    private String inner_angle;

    @ApiModelProperty(value = "外角")
    private String exterior_angle;

    @ApiModelProperty(value = "中心")
    private String center_point;

    @ApiModelProperty(value = "单切片id")
    private Long single_slide_id;

    @ApiModelProperty(value = "要执行的操作(UNION:相交,DIFFERENCE:相差,UPDATE:修改,DELETE:删除,添加:INSERT,null)")
    private String operation;

    @ApiModelProperty(value = "接口每次请求的ID")
    private String traceId;

    private Long sequenceNumber;

    @ApiModelProperty(value = "是否批量请求接口")
    private Boolean isBatch = false;
}
