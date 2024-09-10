package cn.staitech.fr.domain;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 
 * @TableName fr_contour
 */
@TableName(value ="fr_contour")
@Data
public class Annotation implements Serializable {
    /**
     * 主键id
     */
    @TableField(value = "annotation_id")
    private Long annotationId;

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
    private String contour;

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


    @TableField(exist = false)
    private String  operation;

    @TableField(exist = false)
    private List<Long> slideIdList;

    @TableField(exist = false)
    private Boolean filigreeContour;

    @TableField(exist = false)
    private List<Long> structureSizeList;

    @TableField(exist = false)
    private Long magnification;

    @TableField(exist = false)
    private Long magnifications;

    @TableField(exist = false)
    private String collectContour;

    @TableField(exist = false)
    private String results;

    @TableField(exist = false)
    private List<Annotation> list;

    @TableField(exist = false)
    private String intersectsResults;

    @TableField(exist = false)
    private Integer structureSize;

    @TableField(exist = false)
    private BigDecimal structureAreaNum = BigDecimal.ZERO;

    @TableField(exist = false)
    private BigDecimal structurePerimeterNum = BigDecimal.ZERO;

    @TableField(exist = false)
    private Boolean insideOrOutside;

    @TableField(exist = false)
    private List<Long> singleSlideIdList;

    @TableField(exist = false)
    private List<Long> categoryIdList;

    @TableField(exist = false)
    private List<Long> categoryIdLists;

    @TableField(exist = false)
    private List<String> contourList;

    @TableField(exist = false)
    private String cellType;

    @TableField(exist = false)
    private String contourOne;

    @TableField(exist = false)
    private String contourTwo;

    @TableField(exist = false)
    private Double meanDistance;

    @TableField(exist = false)
    private Double minDistance;

    @TableField(exist = false)
    private String results40000;

    @TableField(exist = false)
    private String results10000;

    @TableField(exist = false)
    private String results2500;

    @TableField(exist = false)
    private String results625;

    @TableField(exist = false)
    private Integer count = 0;

    @TableField(exist = false)
    private String dynamicData;

    public Object getDynamicDataList() {
        return JSON.parseObject(dynamicData);
    }

    @TableField(exist = false)
    private Object dynamicDataList;

    @TableField(exist = false)
    private String areaName;

    @TableField(exist = false)
    private String perimeterName;

    @TableField(exist = false)
    private String countName;

    @TableField(exist = false)
    private String areaUnit;

    @TableField(exist = false)
    private String perimeterUnit;

    @TableField(exist = false)
    private String areaValue;

    @TableField(exist = false)
    private String perimeterValue;

    @TableField(exist = false)
    private String countUnit = "个";

}