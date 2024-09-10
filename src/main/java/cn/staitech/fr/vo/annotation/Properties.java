package cn.staitech.fr.vo.annotation;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

@Data
public class Properties {

    @ApiModelProperty(value = "作者(默认0,AI绘制为图片上传作者，其他为操作用户ID)")
    private String annotation_owner;

    @ApiModelProperty(value = "标注id")
    private String marking_id;

    @ApiModelProperty(value = "创建时间")
    private String create_time;

    @ApiModelProperty(value = "创建者")
    private Long create_by;

    @ApiModelProperty(value = "更新者")
    private Long update_by;

    @ApiModelProperty(value = "标注类型(AI表示AI算出的标注，Draw表示前端绘制的标注，Measure表示测量工具数据)")
    private String annotation_type;

    @ApiModelProperty(value = "标注数据类型(LineString,Polygon,point,pc,p,L)")
    private String location_type;

    @ApiModelProperty(value = "面积")
    private String area;

    @ApiModelProperty(value = "周长")
    private String perimeter;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "标签id")
    private Long category_id;

    @ApiModelProperty(value = "标注名称")
    private String measure_full_name;

    @ApiModelProperty(value = "标签名称")
    private String label_name;

    @ApiModelProperty(value = "标注颜色")
    private String label_color;

    @ApiModelProperty(value = "结构编码")
    private String label_code;

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

    @ApiModelProperty(value = "编号")
    private Long number;

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

    @ApiModelProperty(value = "标签总点数")
    private Integer point_count;

    @ApiModelProperty(value = "修改作者")
    private String annotation_update_owner;

    @ApiModelProperty(value = "图像id")
    private Long image_id;

    @ApiModelProperty(value = "项目id")
    private Long project_id;

    @ApiModelProperty(value = "图像url")
    private String image_url;

    @ApiModelProperty(value = "标注类型")
    private Long contour_type;

    @ApiModelProperty(value = "单切片id")
    private Long singleSlideId;

    @ApiModelProperty(value = "区分单脏器还是多脏器")
    private int single;
    @ApiModelProperty(value = "指标")
    private Map<String, Indicator> data_indicators;
    @ApiModelProperty(value = "质点标识")
    private String cell_type;
}
