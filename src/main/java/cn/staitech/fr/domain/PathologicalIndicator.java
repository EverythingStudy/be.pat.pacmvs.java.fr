package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * tb_pathological_indicator
 * @TableName tb_pathological_indicator
 */
@TableName(value ="tb_pathological_indicator")
@Data
public class PathologicalIndicator implements Serializable {
    /**
     * 指标ID
     */
    @TableId(value = "indicator_id", type = IdType.AUTO)
    private Long indicatorId;

    /**
     * 标签集
     */
    @TableField(value = "indicator_name")
    private String indicatorName;

    /**
     * 标签集英文
     */
    @TableField(value = "indicator_name_en")
    private String indicatorNameEn;

    /**
     * 关联项目数量
     */
    @TableField(value = "project_total")
    private Integer projectTotal;

    /**
     * 标注类别数量
     */
    @TableField(value = "annotation_category_total")
    private Integer annotationCategoryTotal;

    /**
     * 
     */
    @TableField(value = "species_id")
    private String speciesId;

    /**
     * 脏器ID
     */
    @TableField(value = "organ_id")
    private String organId;

    /**
     * 病理指标编号
     */
    @TableField(value = "number")
    private String number;

    /**
     * 组织机构ID
     */
    @TableField(value = "organization_id")
    private Long organizationId;

    /**
     * 默认为0，1为删除
     */
    @TableField(value = "del_flag")
    private Integer delFlag;

    /**
     * 标签类型 0:下拉筛选标签；1:自定义标签
     */
    @TableField(value = "indicator_type")
    private Integer indicatorType;

    /**
     * 创建者ID
     */
    @TableField(value = "create_by")
    private Long createBy;

    /**
     * 更新者ID
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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}