package cn.staitech.fr.domain;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill; 

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import java.util.Date;
import java.util.List;
import java.io.Serializable;
import java.math.*;

/**
 * 
 * 
 * @author wanglibei
 * @date 2026-02-11 17:21:31
 */
 
@TableName("fr_annotation")
@Data
@Accessors(chain = true)
@Schema(description="")
public class FrAnnotation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键id")
    @TableId(value = "annotation_id", type = IdType.AUTO)
    private Long annotationId;

    @Schema(description = "面积")
    @TableField("area")
    private BigDecimal area;

    @Schema(description = "周长")
    @TableField("perimeter")
    private BigDecimal perimeter;

    @Schema(description = "轮廓描述")
    @TableField("description")
    private String description;

    @Schema(description = "标签id")
    @TableField("tag_id")
    private Long tagId;

    @Schema(description = "轮廓坐标625")
    @TableField("contour")
    private String contour;

    @Schema(description = "轮廓类型")
    @TableField("location_type")
    private String locationType;

    @Schema(description = "标注类型(AI表示AI算出的标注，Draw表示前端绘制的标注)")
    @TableField("annotation_type")
    private String annotationType;

    @Schema(description = "创建者")
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @Schema(description = "更新者")
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @Schema(description = "切片id")
    @TableField("slide_id")
    private Long slideId;

    @Schema(description = "geojson中数据id")
    @TableField("json_id")
    private String jsonId;

    @Schema(description = "标注类型：默认 0  1 粗轮廓 2 精细轮廓")
    @TableField("contour_type")
    private Integer contourType;

    @Schema(description = "")
    @TableField("category_id")
    private Long categoryId;

//    @Schema(description = "")
//    @TableField("project_id")
//    private Long projectId;

    @Schema(description = "")
    @TableField("contour_polygon")
    private String contourPolygon;

//    @Schema(description = "")
//    @TableField("id")
//    private String id;

    @Schema(description = "脏器ID")
    @TableField("single_slide_id")
    private Long singleSlideId;
    
    @Schema(description = "组织轮廓id")
    @TableField("structure_id")
    private String structureId;
    
    @TableField(exist = false)
    private List<FrAnnotation> list;

}