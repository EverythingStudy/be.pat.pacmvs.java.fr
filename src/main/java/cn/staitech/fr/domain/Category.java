package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * @TableName fr_category
 */
@TableName(value ="fr_category")
@Data
public class Category implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(name = "categoryId" , value = "主键id")
    private Long categoryId;

    /**
     * 种属
     */
    @ApiModelProperty(name = "species" , value = "种属")
    private String species;

    /**
     * 脏器名称
     */
    @ApiModelProperty(name = "organName" , value = "脏器名称")
    private String organName;

    /**
     * 标签全称
     */
    @ApiModelProperty(name = "categoryFullName" , value = "标签全称")
    private String categoryFullName;

    /**
     * 标签简称
     */
    @ApiModelProperty(name = "categoryAbbreviation" , value = "标签简称")
    private String categoryAbbreviation;

    /**
     * 色值
     */
    @ApiModelProperty(name = "chromaticValue" , value = "色值")
    private String chromaticValue;

    /**
     * 创建时间
     */
    @ApiModelProperty(name = "createTime" , value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String createTime;

    /**
     * 创建者
     */
    @ApiModelProperty(name = "createBy" , value = "创建者")
    private Long createBy;

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
        Category other = (Category) that;
        return (this.getCategoryId() == null ? other.getCategoryId() == null : this.getCategoryId().equals(other.getCategoryId()))
            && (this.getSpecies() == null ? other.getSpecies() == null : this.getSpecies().equals(other.getSpecies()))
            && (this.getOrganName() == null ? other.getOrganName() == null : this.getOrganName().equals(other.getOrganName()))
            && (this.getCategoryFullName() == null ? other.getCategoryFullName() == null : this.getCategoryFullName().equals(other.getCategoryFullName()))
            && (this.getCategoryAbbreviation() == null ? other.getCategoryAbbreviation() == null : this.getCategoryAbbreviation().equals(other.getCategoryAbbreviation()))
            && (this.getChromaticValue() == null ? other.getChromaticValue() == null : this.getChromaticValue().equals(other.getChromaticValue()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getCategoryId() == null) ? 0 : getCategoryId().hashCode());
        result = prime * result + ((getSpecies() == null) ? 0 : getSpecies().hashCode());
        result = prime * result + ((getOrganName() == null) ? 0 : getOrganName().hashCode());
        result = prime * result + ((getCategoryFullName() == null) ? 0 : getCategoryFullName().hashCode());
        result = prime * result + ((getCategoryAbbreviation() == null) ? 0 : getCategoryAbbreviation().hashCode());
        result = prime * result + ((getChromaticValue() == null) ? 0 : getChromaticValue().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getCreateBy() == null) ? 0 : getCreateBy().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", categoryId=").append(categoryId);
        sb.append(", species=").append(species);
        sb.append(", organName=").append(organName);
        sb.append(", categoryFullName=").append(categoryFullName);
        sb.append(", categoryAbbreviation=").append(categoryAbbreviation);
        sb.append(", chromaticValue=").append(chromaticValue);
        sb.append(", createTime=").append(createTime);
        sb.append(", createBy=").append(createBy);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}