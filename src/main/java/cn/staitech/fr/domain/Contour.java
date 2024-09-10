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
 * @TableName fr_contour
 */
@TableName(value ="fr_contour")
@Data
public class Contour implements Serializable {
    /**
     * 主键id
     */
    @TableField(value = "contour_id")
    private Long contourId;

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
     * 轮廓描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 轮廓标签id
     */
    @TableField(value = "category_id")
    private Long categoryId;

    /**
     * 轮廓坐标625
     */
    @TableField(value = "contour")
    private Object contour;

    /**
     * 轮廓类型
     */
    @TableField(value = "location_type")
    private String locationType;

    /**
     * 标注类型(AI表示AI算出的标注，Draw表示前端绘制的标注)
     */
    @TableField(value = "annotation_type")
    private String annotationType;

    /**
     * 标注创建者
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
     * 切片id
     */
    @TableField(value = "slide_id")
    private Long slideId;

    /**
     * geojson中数据id
     */
    @TableField(value = "json_id")
    private String jsonId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    
    @TableField(exist = false)
    private Long sequenceNumber;
}