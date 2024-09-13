package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName fr_measure_del
 */
@TableName(value ="fr_measure_del")
@Data
public class MeasureDel implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "measure_id", type = IdType.AUTO)
    private Long measureId;

    /**
     * 切片id
     */
    @TableField(value = "slide_id")
    private Long slideId;

    /**
     * 标注类型(AI表示AI算出的标注，Draw表示前端绘制的标注，Measure表示测量工具数据)
     */
    @TableField(value = "annotation_type")
    private String annotationType;

    /**
     * 面积
     */
    @TableField(value = "area")
    private String area;

    /**
     * 周长
     */
    @TableField(value = "perimeter")
    private String perimeter;

    /**
     * 标注名称
     */
    @TableField(value = "number")
    private Long number;

    /**
     * 测量轮廓类型(0:正常,表示有关系,默认为0")
     */
    @TableField(value = "measure_type")
    private Integer measureType;

    /**
     * 测量关系
     */
    @TableField(value = "measure_relation")
    private String measureRelation;

    /**
     * 测量轮廓表示名称:L
     */
    @TableField(value = "measure_name")
    private String measureName;

    /**
     * 测量轮廓标识：1
     */
    @TableField(value = "measure_number")
    private Integer measureNumber;

    /**
     * 平均间距
     */
    @TableField(value = "mean_distance")
    private Double meanDistance;

    /**
     * 最大间距
     */
    @TableField(value = "max_distance")
    private Double maxDistance;

    /**
     * 最小间距
     */
    @TableField(value = "min_distance")
    private Double minDistance;

    /**
     * 内角
     */
    @TableField(value = "inner_angle")
    private String innerAngle;

    /**
     * 外角
     */
    @TableField(value = "exterior_angle")
    private String exteriorAngle;

    /**
     * 中心
     */
    @TableField(value = "center_point")
    private String centerPoint;

    /**
     * 标注数据类型(LineString,Polygon,point,pc,p,L)
     */
    @TableField(value = "location_type")
    private String locationType;

    /**
     * 周长（圆）
     */
    @TableField(value = "radius")
    private String radius;

    /**
     * 标注数据
     */
    @TableField(value = "contour")
    private Object contour;

    /**
     * 创建者
     */
    @TableField(value = "create_by")
    private Long createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新者
     */
    @TableField(value = "update_by")
    private Long updateBy;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 标注名称
     */
    @TableField(value = "measure_full_name")
    private String measureFullName;

    /**
     * 删除者
     */
    @TableField(value = "delete_by")
    private Long deleteBy;

    /**
     * 删除时间
     */
    @TableField(value = "delete_time")
    private Date deleteTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}