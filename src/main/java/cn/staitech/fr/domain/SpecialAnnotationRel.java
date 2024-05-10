package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

/**
 * 项目标注序列关系表
 * @TableName fr_special_annotation_rel
 */
@TableName(value ="fr_special_annotation_rel")
public class SpecialAnnotationRel implements Serializable {
    /**
     * 项目标注关系id
     */
    @TableId(type = IdType.AUTO)
    private Long specialAnnotationRelId;

    /**
     * 专题ID
     */
    private Long specialId;

    /**
     * 表序列号
     */
    private Long sequenceNumber;

    /**
     * 创建人id
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新者id
     */
    private Long updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 项目标注关系id
     */
    public Long getSpecialAnnotationRelId() {
        return specialAnnotationRelId;
    }

    /**
     * 项目标注关系id
     */
    public void setSpecialAnnotationRelId(Long specialAnnotationRelId) {
        this.specialAnnotationRelId = specialAnnotationRelId;
    }

    /**
     * 专题ID
     */
    public Long getSpecialId() {
        return specialId;
    }

    /**
     * 专题ID
     */
    public void setSpecialId(Long specialId) {
        this.specialId = specialId;
    }

    /**
     * 表序列号
     */
    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * 表序列号
     */
    public void setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * 创建人id
     */
    public Long getCreateBy() {
        return createBy;
    }

    /**
     * 创建人id
     */
    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    /**
     * 创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 更新者id
     */
    public Long getUpdateBy() {
        return updateBy;
    }

    /**
     * 更新者id
     */
    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

    /**
     * 更新时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 更新时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

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
        SpecialAnnotationRel other = (SpecialAnnotationRel) that;
        return (this.getSpecialAnnotationRelId() == null ? other.getSpecialAnnotationRelId() == null : this.getSpecialAnnotationRelId().equals(other.getSpecialAnnotationRelId()))
            && (this.getSpecialId() == null ? other.getSpecialId() == null : this.getSpecialId().equals(other.getSpecialId()))
            && (this.getSequenceNumber() == null ? other.getSequenceNumber() == null : this.getSequenceNumber().equals(other.getSequenceNumber()))
            && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateBy() == null ? other.getUpdateBy() == null : this.getUpdateBy().equals(other.getUpdateBy()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getSpecialAnnotationRelId() == null) ? 0 : getSpecialAnnotationRelId().hashCode());
        result = prime * result + ((getSpecialId() == null) ? 0 : getSpecialId().hashCode());
        result = prime * result + ((getSequenceNumber() == null) ? 0 : getSequenceNumber().hashCode());
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
        sb.append(", specialAnnotationRelId=").append(specialAnnotationRelId);
        sb.append(", specialId=").append(specialId);
        sb.append(", sequenceNumber=").append(sequenceNumber);
        sb.append(", createBy=").append(createBy);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateBy=").append(updateBy);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}