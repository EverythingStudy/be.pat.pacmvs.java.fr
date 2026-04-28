package cn.staitech.fr.domain;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName fr_contour_json
 */
@TableName(value ="fr_contour_json")
@Data
public class ContourJson implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "contour_json_id", type = IdType.AUTO)
    private Long contourJsonId;

    /**
     * 切片id
     */
    @TableField(value = "slide_id")
    private Long slideId;

    /**
     * 瓦片名称
     */
    @TableField(value = "tile_name")
    private String tileName;

    /**
     * 结构大小
     */
    @TableField(value = "structure_size")
    private Integer structureSize;

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
     * 单切片id
     */
    @TableField(value = "single_slide_id")
    private Long singleSlideId;

    /**
     * 中结构json文件
     */
    @TableField(value = "middle")
    private String middle;

    /**
     * 小结构json文件
     */
    @TableField(value = "small")
    private String small;

    /**
     * 
     */
    @TableField(value = "middle_small")
    private String middleSmall;

    /**
     * 大结构json文件
     */
    @TableField(value = "big")
    private String big;

    @TableField(exist = false)
    private String jsonUrl;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
