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
 * @TableName tb_pathological_indicator_category
 */
@TableName(value ="tb_pathological_indicator_category")
@Data
public class PathologicalIndicatorCategory implements Serializable {
    /**
     * 标注类别ID
     */
    @TableId(type = IdType.AUTO)
    private Long categoryId;

    /**
     * 结构指标ID
     */
    private Long indicatorId;

    /**
     * 标注类别名称
     */
    private String categoryName;

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
     * 完整编码
     */
    private String number;

    /**
     * 图层顺序
     */
    private Integer orderNumber;

    /**
     * 组织机构ID
     */
    private Long organizationId;

    /**
     * 0:默认标注类型；1:unlable
     */
    private Integer annoType;

    /**
     * 默认为0，1为删除
     */
    private Integer delFlag;

    /**
     * 创建者
     */
    private Long createBy;

    /**
     * 更新者
     */
    private Long updateBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 标注编码
     */
    private String categoryCode;

    /**
     * 组内标签顺序
     */
    private Integer groupNumber;

    /**
     * 标签类型 0:下拉筛选标签；1:自定义标签
     */
    private Integer categoryType;

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
        PathologicalIndicatorCategory other = (PathologicalIndicatorCategory) that;
        return (this.getCategoryId() == null ? other.getCategoryId() == null : this.getCategoryId().equals(other.getCategoryId()))
            && (this.getIndicatorId() == null ? other.getIndicatorId() == null : this.getIndicatorId().equals(other.getIndicatorId()))
            && (this.getCategoryName() == null ? other.getCategoryName() == null : this.getCategoryName().equals(other.getCategoryName()))
            && (this.getStructureId() == null ? other.getStructureId() == null : this.getStructureId().equals(other.getStructureId()))
            && (this.getRgb() == null ? other.getRgb() == null : this.getRgb().equals(other.getRgb()))
            && (this.getHex() == null ? other.getHex() == null : this.getHex().equals(other.getHex()))
            && (this.getColor() == null ? other.getColor() == null : this.getColor().equals(other.getColor()))
            && (this.getNumber() == null ? other.getNumber() == null : this.getNumber().equals(other.getNumber()))
            && (this.getOrderNumber() == null ? other.getOrderNumber() == null : this.getOrderNumber().equals(other.getOrderNumber()))
            && (this.getOrganizationId() == null ? other.getOrganizationId() == null : this.getOrganizationId().equals(other.getOrganizationId()))
            && (this.getAnnoType() == null ? other.getAnnoType() == null : this.getAnnoType().equals(other.getAnnoType()))
            && (this.getDelFlag() == null ? other.getDelFlag() == null : this.getDelFlag().equals(other.getDelFlag()))
            && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()))
            && (this.getUpdateBy() == null ? other.getUpdateBy() == null : this.getUpdateBy().equals(other.getUpdateBy()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getCategoryCode() == null ? other.getCategoryCode() == null : this.getCategoryCode().equals(other.getCategoryCode()))
            && (this.getGroupNumber() == null ? other.getGroupNumber() == null : this.getGroupNumber().equals(other.getGroupNumber()))
            && (this.getCategoryType() == null ? other.getCategoryType() == null : this.getCategoryType().equals(other.getCategoryType()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getCategoryId() == null) ? 0 : getCategoryId().hashCode());
        result = prime * result + ((getIndicatorId() == null) ? 0 : getIndicatorId().hashCode());
        result = prime * result + ((getCategoryName() == null) ? 0 : getCategoryName().hashCode());
        result = prime * result + ((getStructureId() == null) ? 0 : getStructureId().hashCode());
        result = prime * result + ((getRgb() == null) ? 0 : getRgb().hashCode());
        result = prime * result + ((getHex() == null) ? 0 : getHex().hashCode());
        result = prime * result + ((getColor() == null) ? 0 : getColor().hashCode());
        result = prime * result + ((getNumber() == null) ? 0 : getNumber().hashCode());
        result = prime * result + ((getOrderNumber() == null) ? 0 : getOrderNumber().hashCode());
        result = prime * result + ((getOrganizationId() == null) ? 0 : getOrganizationId().hashCode());
        result = prime * result + ((getAnnoType() == null) ? 0 : getAnnoType().hashCode());
        result = prime * result + ((getDelFlag() == null) ? 0 : getDelFlag().hashCode());
        result = prime * result + ((getCreateBy() == null) ? 0 : getCreateBy().hashCode());
        result = prime * result + ((getUpdateBy() == null) ? 0 : getUpdateBy().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getCategoryCode() == null) ? 0 : getCategoryCode().hashCode());
        result = prime * result + ((getGroupNumber() == null) ? 0 : getGroupNumber().hashCode());
        result = prime * result + ((getCategoryType() == null) ? 0 : getCategoryType().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", categoryId=").append(categoryId);
        sb.append(", indicatorId=").append(indicatorId);
        sb.append(", categoryName=").append(categoryName);
        sb.append(", structureId=").append(structureId);
        sb.append(", rgb=").append(rgb);
        sb.append(", hex=").append(hex);
        sb.append(", color=").append(color);
        sb.append(", number=").append(number);
        sb.append(", orderNumber=").append(orderNumber);
        sb.append(", organizationId=").append(organizationId);
        sb.append(", annoType=").append(annoType);
        sb.append(", delFlag=").append(delFlag);
        sb.append(", createBy=").append(createBy);
        sb.append(", updateBy=").append(updateBy);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", categoryCode=").append(categoryCode);
        sb.append(", groupNumber=").append(groupNumber);
        sb.append(", categoryType=").append(categoryType);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}