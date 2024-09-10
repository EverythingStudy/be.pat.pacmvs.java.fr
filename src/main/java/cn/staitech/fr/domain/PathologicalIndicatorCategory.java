package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * tb_pathological_indicator_category
 * @author gjt
 */
@TableName(value ="tb_pathological_indicator_category")
@Data
public class PathologicalIndicatorCategory implements Serializable {
    /**
     * 标注类别ID
     */
    @TableId(value = "category_id", type = IdType.AUTO)
    private Long categoryId;

    /**
     * 结构指标ID
     */
    @TableField(value = "indicator_id")
    private Long indicatorId;

    /**
     * 标注类别名称
     */
    @TableField(value = "category_name")
    private String categoryName;

    /**
     * 结构ID
     */
    @TableField(value = "structure_id")
    private String structureId;

    /**
     * 颜色的RGB值
     */
    @TableField(value = "rgb")
    private String rgb;

    /**
     * 颜色的HEX值
     */
    @TableField(value = "hex")
    private String hex;

    /**
     * 颜色名称(备用)
     */
    @TableField(value = "color")
    private String color;

    /**
     * 完整编码
     */
    @TableField(value = "number")
    private String number;

    /**
     * 图层顺序
     */
    @TableField(value = "order_number")
    private Integer orderNumber;

    /**
     * 组织机构ID
     */
    @TableField(value = "organization_id")
    private Long organizationId;

    /**
     * 0:默认标注类型；1:unlable
     */
    @TableField(value = "anno_type")
    private Integer annoType;

    /**
     * 默认为0，1为删除
     */
    @TableField(value = "del_flag")
    private Integer delFlag;

    /**
     * 创建者
     */
    @TableField(value = "create_by")
    private Long createBy;

    /**
     * 更新者
     */
    @TableField(value = "update_by")
    private Long updateBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 标注编码
     */
    @TableField(value = "category_code")
    private String categoryCode;

    /**
     * 组内标签顺序
     */
    @TableField(value = "group_number")
    private Integer groupNumber;

    /**
     * 标签类型 0:下拉筛选标签；1:自定义标签
     */
    @TableField(value = "category_type")
    private Integer categoryType;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}