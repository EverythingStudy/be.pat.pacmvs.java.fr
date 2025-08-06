package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

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
    @TableId(type = IdType.AUTO)
    private Long indicatorId;

    /**
     * 标签集
     */
    private String indicatorName;

    /**
     * 标签集英文
     */
    private String indicatorNameEn;

    /**
     * 关联项目数量
     */
    private Integer projectTotal;

    /**
     * 标注类别数量
     */
    private Integer annotationCategoryTotal;

    /**
     * 
     */
    private String speciesId;

    /**
     * 脏器ID
     */
    private String organId;

    /**
     * 病理指标编号
     */
    private String number;

    /**
     * 组织机构ID
     */
    private Long organizationId;

    /**
     * 默认为0，1为删除
     */
    private Integer delFlag;

    /**
     * 标签类型 0:下拉筛选标签；1:自定义标签
     */
    private Integer indicatorType;

    /**
     * 创建者ID
     */
    private Long createBy;

    /**
     * 更新者ID
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
        PathologicalIndicator other = (PathologicalIndicator) that;
        return (this.getIndicatorId() == null ? other.getIndicatorId() == null : this.getIndicatorId().equals(other.getIndicatorId()))
            && (this.getIndicatorName() == null ? other.getIndicatorName() == null : this.getIndicatorName().equals(other.getIndicatorName()))
            && (this.getIndicatorNameEn() == null ? other.getIndicatorNameEn() == null : this.getIndicatorNameEn().equals(other.getIndicatorNameEn()))
            && (this.getProjectTotal() == null ? other.getProjectTotal() == null : this.getProjectTotal().equals(other.getProjectTotal()))
            && (this.getAnnotationCategoryTotal() == null ? other.getAnnotationCategoryTotal() == null : this.getAnnotationCategoryTotal().equals(other.getAnnotationCategoryTotal()))
            && (this.getSpeciesId() == null ? other.getSpeciesId() == null : this.getSpeciesId().equals(other.getSpeciesId()))
            && (this.getOrganId() == null ? other.getOrganId() == null : this.getOrganId().equals(other.getOrganId()))
            && (this.getNumber() == null ? other.getNumber() == null : this.getNumber().equals(other.getNumber()))
            && (this.getOrganizationId() == null ? other.getOrganizationId() == null : this.getOrganizationId().equals(other.getOrganizationId()))
            && (this.getDelFlag() == null ? other.getDelFlag() == null : this.getDelFlag().equals(other.getDelFlag()))
            && (this.getIndicatorType() == null ? other.getIndicatorType() == null : this.getIndicatorType().equals(other.getIndicatorType()))
            && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()))
            && (this.getUpdateBy() == null ? other.getUpdateBy() == null : this.getUpdateBy().equals(other.getUpdateBy()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getIndicatorId() == null) ? 0 : getIndicatorId().hashCode());
        result = prime * result + ((getIndicatorName() == null) ? 0 : getIndicatorName().hashCode());
        result = prime * result + ((getIndicatorNameEn() == null) ? 0 : getIndicatorNameEn().hashCode());
        result = prime * result + ((getProjectTotal() == null) ? 0 : getProjectTotal().hashCode());
        result = prime * result + ((getAnnotationCategoryTotal() == null) ? 0 : getAnnotationCategoryTotal().hashCode());
        result = prime * result + ((getSpeciesId() == null) ? 0 : getSpeciesId().hashCode());
        result = prime * result + ((getOrganId() == null) ? 0 : getOrganId().hashCode());
        result = prime * result + ((getNumber() == null) ? 0 : getNumber().hashCode());
        result = prime * result + ((getOrganizationId() == null) ? 0 : getOrganizationId().hashCode());
        result = prime * result + ((getDelFlag() == null) ? 0 : getDelFlag().hashCode());
        result = prime * result + ((getIndicatorType() == null) ? 0 : getIndicatorType().hashCode());
        result = prime * result + ((getCreateBy() == null) ? 0 : getCreateBy().hashCode());
        result = prime * result + ((getUpdateBy() == null) ? 0 : getUpdateBy().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", indicatorId=").append(indicatorId);
        sb.append(", indicatorName=").append(indicatorName);
        sb.append(", indicatorNameEn=").append(indicatorNameEn);
        sb.append(", projectTotal=").append(projectTotal);
        sb.append(", annotationCategoryTotal=").append(annotationCategoryTotal);
        sb.append(", speciesId=").append(speciesId);
        sb.append(", organId=").append(organId);
        sb.append(", number=").append(number);
        sb.append(", organizationId=").append(organizationId);
        sb.append(", delFlag=").append(delFlag);
        sb.append(", indicatorType=").append(indicatorType);
        sb.append(", createBy=").append(createBy);
        sb.append(", updateBy=").append(updateBy);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}