package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 结构标签集
 * @TableName tb_structure_tag_set
 */
@TableName(value ="tb_structure_tag_set")
@Data
public class StructureTagSet implements Serializable {
    /**
     * 标签集ID
     */
    @TableId(type = IdType.AUTO)
    private Long structureTagSetId;

    /**
     * 标签集
     */
    private String structureTagSetName;

    /**
     * 标签集英文
     */
    private String structureTagSetNameEn;

    /**
     * 种属id
     */
    private String speciesId;

    /**
     * 脏器编码
     */
    private String organCode;

    /**
     * 组织机构ID
     */
    private Long organizationId;

    /**
     * 删除标志(0-正常，1-删除)
     */
    private Boolean delFlag;

    /**
     * 标签类型 0-下拉筛选标签；1-自定义标签
     */
    private Integer type;

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
        StructureTagSet other = (StructureTagSet) that;
        return (this.getStructureTagSetId() == null ? other.getStructureTagSetId() == null : this.getStructureTagSetId().equals(other.getStructureTagSetId()))
            && (this.getStructureTagSetName() == null ? other.getStructureTagSetName() == null : this.getStructureTagSetName().equals(other.getStructureTagSetName()))
            && (this.getStructureTagSetNameEn() == null ? other.getStructureTagSetNameEn() == null : this.getStructureTagSetNameEn().equals(other.getStructureTagSetNameEn()))
            && (this.getSpeciesId() == null ? other.getSpeciesId() == null : this.getSpeciesId().equals(other.getSpeciesId()))
            && (this.getOrganCode() == null ? other.getOrganCode() == null : this.getOrganCode().equals(other.getOrganCode()))
            && (this.getOrganizationId() == null ? other.getOrganizationId() == null : this.getOrganizationId().equals(other.getOrganizationId()))
            && (this.getDelFlag() == null ? other.getDelFlag() == null : this.getDelFlag().equals(other.getDelFlag()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
            && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateBy() == null ? other.getUpdateBy() == null : this.getUpdateBy().equals(other.getUpdateBy()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getStructureTagSetId() == null) ? 0 : getStructureTagSetId().hashCode());
        result = prime * result + ((getStructureTagSetName() == null) ? 0 : getStructureTagSetName().hashCode());
        result = prime * result + ((getStructureTagSetNameEn() == null) ? 0 : getStructureTagSetNameEn().hashCode());
        result = prime * result + ((getSpeciesId() == null) ? 0 : getSpeciesId().hashCode());
        result = prime * result + ((getOrganCode() == null) ? 0 : getOrganCode().hashCode());
        result = prime * result + ((getOrganizationId() == null) ? 0 : getOrganizationId().hashCode());
        result = prime * result + ((getDelFlag() == null) ? 0 : getDelFlag().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getCreateBy() == null) ? 0 : getCreateBy().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateBy() == null) ? 0 : getUpdateBy().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", structureTagSetId=").append(structureTagSetId);
        sb.append(", structureTagSetName=").append(structureTagSetName);
        sb.append(", structureTagSetNameEn=").append(structureTagSetNameEn);
        sb.append(", speciesId=").append(speciesId);
        sb.append(", organCode=").append(organCode);
        sb.append(", organizationId=").append(organizationId);
        sb.append(", delFlag=").append(delFlag);
        sb.append(", type=").append(type);
        sb.append(", createBy=").append(createBy);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateBy=").append(updateBy);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}