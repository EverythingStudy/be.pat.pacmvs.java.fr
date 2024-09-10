package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 项目标注序列关系表
 * @TableName fr_special_annotation_rel
 */
@TableName(value ="fr_special_annotation_rel")
@Data
public class SpecialAnnotationRel implements Serializable {
    /**
     * 项目标注关系id
     */
    @TableId(value = "special_annotation_rel_id", type = IdType.AUTO)
    private Long specialAnnotationRelId;

    /**
     * 专题ID
     */
    @TableField(value = "special_id")
    private Long specialId;

    /**
     * 表序列号
     */
    @TableField(value = "sequence_number")
    private Long sequenceNumber;

    /**
     * 创建人id
     */
    @TableField(value = "create_by")
    private Long createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新者id
     */
    @TableField(value = "update_by")
    private Long updateBy;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}