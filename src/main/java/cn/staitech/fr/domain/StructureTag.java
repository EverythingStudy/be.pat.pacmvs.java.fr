package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 结构标签
 * @TableName tb_structure_tag
 */
@TableName(value ="tb_structure_tag")
@Data
public class StructureTag implements Serializable {
    /**
     * 结构标签id
     */
    @TableId(type = IdType.AUTO)
    private Long structureTagId;

    /**
     * 结构标签集ID
     */
    private Long structureTagSetId;

    /**
     * 标注类别名称
     */
    private String structureTagName;

    /**
     * 结构ID
     */
    private String structureId;

    /**
     * 颜色的RGB值
     */
    private String rgb;

    /**
     * 颜色的HEX值
     */
    private String hex;

    /**
     * 颜色名称(备用)
     */
    private String color;

    /**
     * 图层顺序
     */
    private Integer orderNumber;

    /**
     * 组织机构ID
     */
    private Long organizationId;

    /**
     * 删除标志(0-正常，1-删除)
     */
    private Integer delFlag;

    /**
     * 创建人id
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人id
     */
    private Long updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 组内标签顺序
     */
    private Integer groupInnerOrder;

    /**
     * 标签类型 0-下拉筛选标签；1-自定义标签
     */
    private Integer type;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

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
        StructureTag other = (StructureTag) that;
        return (this.getStructureTagId() == null ? other.getStructureTagId() == null : this.getStructureTagId().equals(other.getStructureTagId()))
            && (this.getStructureTagSetId() == null ? other.getStructureTagSetId() == null : this.getStructureTagSetId().equals(other.getStructureTagSetId()))
            && (this.getStructureTagName() == null ? other.getStructureTagName() == null : this.getStructureTagName().equals(other.getStructureTagName()))
            && (this.getStructureId() == null ? other.getStructureId() == null : this.getStructureId().equals(other.getStructureId()))
            && (this.getRgb() == null ? other.getRgb() == null : this.getRgb().equals(other.getRgb()))
            && (this.getHex() == null ? other.getHex() == null : this.getHex().equals(other.getHex()))
            && (this.getColor() == null ? other.getColor() == null : this.getColor().equals(other.getColor()))
            && (this.getOrderNumber() == null ? other.getOrderNumber() == null : this.getOrderNumber().equals(other.getOrderNumber()))
            && (this.getOrganizationId() == null ? other.getOrganizationId() == null : this.getOrganizationId().equals(other.getOrganizationId()))
            && (this.getDelFlag() == null ? other.getDelFlag() == null : this.getDelFlag().equals(other.getDelFlag()))
            && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateBy() == null ? other.getUpdateBy() == null : this.getUpdateBy().equals(other.getUpdateBy()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getGroupInnerOrder() == null ? other.getGroupInnerOrder() == null : this.getGroupInnerOrder().equals(other.getGroupInnerOrder()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getStructureTagId() == null) ? 0 : getStructureTagId().hashCode());
        result = prime * result + ((getStructureTagSetId() == null) ? 0 : getStructureTagSetId().hashCode());
        result = prime * result + ((getStructureTagName() == null) ? 0 : getStructureTagName().hashCode());
        result = prime * result + ((getStructureId() == null) ? 0 : getStructureId().hashCode());
        result = prime * result + ((getRgb() == null) ? 0 : getRgb().hashCode());
        result = prime * result + ((getHex() == null) ? 0 : getHex().hashCode());
        result = prime * result + ((getColor() == null) ? 0 : getColor().hashCode());
        result = prime * result + ((getOrderNumber() == null) ? 0 : getOrderNumber().hashCode());
        result = prime * result + ((getOrganizationId() == null) ? 0 : getOrganizationId().hashCode());
        result = prime * result + ((getDelFlag() == null) ? 0 : getDelFlag().hashCode());
        result = prime * result + ((getCreateBy() == null) ? 0 : getCreateBy().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateBy() == null) ? 0 : getUpdateBy().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getGroupInnerOrder() == null) ? 0 : getGroupInnerOrder().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", structureTagId=").append(structureTagId);
        sb.append(", structureTagSetId=").append(structureTagSetId);
        sb.append(", structureTagName=").append(structureTagName);
        sb.append(", structureId=").append(structureId);
        sb.append(", rgb=").append(rgb);
        sb.append(", hex=").append(hex);
        sb.append(", color=").append(color);
        sb.append(", orderNumber=").append(orderNumber);
        sb.append(", organizationId=").append(organizationId);
        sb.append(", delFlag=").append(delFlag);
        sb.append(", createBy=").append(createBy);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateBy=").append(updateBy);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", groupInnerOrder=").append(groupInnerOrder);
        sb.append(", type=").append(type);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}