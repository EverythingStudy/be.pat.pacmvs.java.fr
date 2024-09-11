package cn.staitech.fr.domain;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 专题选片表
 * @TableName fr_slide
 */
@TableName(value ="fr_slide")
@Data
public class Slide implements Serializable {
    /**
     * 切片ID
     */
    @TableId(type = IdType.AUTO)
    private Long slideId;

    /**
     * 专题ID
     */
    private Long specialId;

    /**
     * 图像ID
     */
    private Long imageId;



    /**
     * 创建者
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新者
     */
    private Long updateBy;

    private String delFlag;

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
        Slide other = (Slide) that;
        return (this.getSlideId() == null ? other.getSlideId() == null : this.getSlideId().equals(other.getSlideId()))
            && (this.getSpecialId() == null ? other.getSpecialId() == null : this.getSpecialId().equals(other.getSpecialId()))
            && (this.getImageId() == null ? other.getImageId() == null : this.getImageId().equals(other.getImageId()))
            && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateBy() == null ? other.getUpdateBy() == null : this.getUpdateBy().equals(other.getUpdateBy()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getSlideId() == null) ? 0 : getSlideId().hashCode());
        result = prime * result + ((getSpecialId() == null) ? 0 : getSpecialId().hashCode());
        result = prime * result + ((getImageId() == null) ? 0 : getImageId().hashCode());
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
        sb.append(", slideId=").append(slideId);
        sb.append(", specialId=").append(specialId);
        sb.append(", imageId=").append(imageId);
        sb.append(", createBy=").append(createBy);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateBy=").append(updateBy);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

}