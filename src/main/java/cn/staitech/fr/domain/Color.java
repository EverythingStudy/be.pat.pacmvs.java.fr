package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName tb_color
 */
@TableName(value ="tb_color")
@Data
public class Color implements Serializable {
    /**
     * 颜色ID
     */
    @TableId(type = IdType.AUTO)
    private Integer colorId;

    /**
     * rgb
     */
    private String rgb;

    /**
     * hex
     */
    private String hex;

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
        Color other = (Color) that;
        return (this.getColorId() == null ? other.getColorId() == null : this.getColorId().equals(other.getColorId()))
            && (this.getRgb() == null ? other.getRgb() == null : this.getRgb().equals(other.getRgb()))
            && (this.getHex() == null ? other.getHex() == null : this.getHex().equals(other.getHex()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getColorId() == null) ? 0 : getColorId().hashCode());
        result = prime * result + ((getRgb() == null) ? 0 : getRgb().hashCode());
        result = prime * result + ((getHex() == null) ? 0 : getHex().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", colorId=").append(colorId);
        sb.append(", rgb=").append(rgb);
        sb.append(", hex=").append(hex);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}