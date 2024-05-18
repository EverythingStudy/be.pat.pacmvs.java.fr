package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 
 * @TableName fr_annotation
 */
@TableName(value ="fr_annotation")
@Data
public class Annotation implements Serializable {
    /**
     * 主键id
     */
    @TableId
    private Long annotationId;

    /**
     * 面积
     */
    private String area;

    /**
     * 周长
     */
    private String perimeter;

    /**
     * 轮廓描述
     */
    private String description;

    /**
     * 轮廓标签id
     */
    private Long categoryId;

    /**
     * 轮廓类型
     */
    private String locationType;

    /**
     * 标注类型(AI表示AI算出的标注，Draw表示前端绘制的标注)
     */
    private String annotationType;

    /**
     * 标注创建者
     */
    private Long createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String createTime;

    /**
     * 更新者
     */
    private Long updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String updateTime;

    /**
     * 项目id
     */
    private Long projectId;

    /**
     * 切片id
     */
    private Long slideId;

    /**
     * geojson中数据id
     */
    private String id;

    /**
     * 轮廓坐标
     */
    @TableField(exist = false)
    private String contour;

    private String contour40000;

    private String contour10000;

    private String contour2500;

    private String contour625;
    
    /**
     * 轮廓类型 1：矩形 2：标注轮廓
     */
    private Long contourType;

    private Long singleSlideId;

    private int single;

    @TableField(exist = false)
    private String  operation;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    
    @TableField(exist = false)
    private List<Long> slideIdList;

    @TableField(exist = false)
    private Long sequenceNumber;

    @TableField(exist = false)
    private Boolean filigreeContour;

    @TableField(exist = false)
    private List<Long> structureSizeList;

    @TableField(exist = false)
    private Long magnification;

    @TableField(exist = false)
    private String collectContour;

    @TableField(exist = false)
    private String results;

    @TableField(exist = false)
    private List<Annotation> list;

    @TableField(exist = false)
    private String intersectsResults;

    private Integer structureSize;

    @TableField(exist = false)
    private BigDecimal structureAreaNum = BigDecimal.ZERO;

    @TableField(exist = false)
    private BigDecimal structurePerimeterNum = BigDecimal.ZERO;



    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Annotation other = (Annotation) that;
        return (this.getAnnotationId() == null ? other.getAnnotationId() == null : this.getAnnotationId().equals(other.getAnnotationId()))
            && (this.getArea() == null ? other.getArea() == null : this.getArea().equals(other.getArea()))
            && (this.getPerimeter() == null ? other.getPerimeter() == null : this.getPerimeter().equals(other.getPerimeter()))
            && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()))
            && (this.getCategoryId() == null ? other.getCategoryId() == null : this.getCategoryId().equals(other.getCategoryId()))
            && (this.getLocationType() == null ? other.getLocationType() == null : this.getLocationType().equals(other.getLocationType()))
            && (this.getAnnotationType() == null ? other.getAnnotationType() == null : this.getAnnotationType().equals(other.getAnnotationType()))
            && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateBy() == null ? other.getUpdateBy() == null : this.getUpdateBy().equals(other.getUpdateBy()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getProjectId() == null ? other.getProjectId() == null : this.getProjectId().equals(other.getProjectId()))
            && (this.getSlideId() == null ? other.getSlideId() == null : this.getSlideId().equals(other.getSlideId()))
            && (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getContourType() == null ? other.getContourType() == null : this.getContourType().equals(other.getContourType()))
            && (this.getContour() == null ? other.getContour() == null : this.getContour().equals(other.getContour()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getAnnotationId() == null) ? 0 : getAnnotationId().hashCode());
        result = prime * result + ((getArea() == null) ? 0 : getArea().hashCode());
        result = prime * result + ((getPerimeter() == null) ? 0 : getPerimeter().hashCode());
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        result = prime * result + ((getCategoryId() == null) ? 0 : getCategoryId().hashCode());
        result = prime * result + ((getLocationType() == null) ? 0 : getLocationType().hashCode());
        result = prime * result + ((getAnnotationType() == null) ? 0 : getAnnotationType().hashCode());
        result = prime * result + ((getCreateBy() == null) ? 0 : getCreateBy().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateBy() == null) ? 0 : getUpdateBy().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getProjectId() == null) ? 0 : getProjectId().hashCode());
        result = prime * result + ((getSlideId() == null) ? 0 : getSlideId().hashCode());
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getContourType() == null) ? 0 : getContourType().hashCode());
        result = prime * result + ((getContour() == null) ? 0 : getContour().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", annotationId=").append(annotationId);
        sb.append(", area=").append(area);
        sb.append(", perimeter=").append(perimeter);
        sb.append(", description=").append(description);
        sb.append(", categoryId=").append(categoryId);
        sb.append(", locationType=").append(locationType);
        sb.append(", annotationType=").append(annotationType);
        sb.append(", createBy=").append(createBy);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateBy=").append(updateBy);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", projectId=").append(projectId);
        sb.append(", slideId=").append(slideId);
        sb.append(", contourType=").append(contourType);
        sb.append(", id=").append(id);
        sb.append(", contour=").append(contour);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}