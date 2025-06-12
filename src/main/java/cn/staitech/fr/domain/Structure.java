package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName tb_structure
 */
@TableName(value ="tb_structure")
@Data
public class Structure implements Serializable {
    /**
     * 种属ID
     */
    private String speciesId;

    /**
     * 脏器编码
     */
    private String organCode;

    /**
     * 结构ID
     */
    private String structureId;

    /**
     * 结构名称
     */
    private String name;

    /**
     * 结构名称en
     */
    private String nameEn;

    /**
     * 结构标签大小 1：大 2：中 3：小
     */
    private Integer structureSize;

    /**
     * RO：结构类型  ROA:标注区域 ROE:考核区域
     */
    private String type;

    /**
     * 机构ID
     */
    private Long organizationId;

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
        Structure other = (Structure) that;
        return (this.getSpeciesId() == null ? other.getSpeciesId() == null : this.getSpeciesId().equals(other.getSpeciesId()))
            && (this.getOrganCode() == null ? other.getOrganCode() == null : this.getOrganCode().equals(other.getOrganCode()))
            && (this.getStructureId() == null ? other.getStructureId() == null : this.getStructureId().equals(other.getStructureId()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getNameEn() == null ? other.getNameEn() == null : this.getNameEn().equals(other.getNameEn()))
            && (this.getStructureSize() == null ? other.getStructureSize() == null : this.getStructureSize().equals(other.getStructureSize()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
            && (this.getOrganizationId() == null ? other.getOrganizationId() == null : this.getOrganizationId().equals(other.getOrganizationId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getSpeciesId() == null) ? 0 : getSpeciesId().hashCode());
        result = prime * result + ((getOrganCode() == null) ? 0 : getOrganCode().hashCode());
        result = prime * result + ((getStructureId() == null) ? 0 : getStructureId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getNameEn() == null) ? 0 : getNameEn().hashCode());
        result = prime * result + ((getStructureSize() == null) ? 0 : getStructureSize().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getOrganizationId() == null) ? 0 : getOrganizationId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", speciesId=").append(speciesId);
        sb.append(", organCode=").append(organCode);
        sb.append(", structureId=").append(structureId);
        sb.append(", name=").append(name);
        sb.append(", nameEn=").append(nameEn);
        sb.append(", structureSize=").append(structureSize);
        sb.append(", type=").append(type);
        sb.append(", organizationId=").append(organizationId);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}