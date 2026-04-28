package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户设置表
 * @TableName tb_user_setting
 * @version 2.6.0
 */
@TableName(value ="tb_user_setting")
@Data
public class UserSetting implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long userSettingsId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 图像增强开关: 0-关闭, 1-开启
     */
    private Integer enhanceSwitch;

    /**
     * 阅片模式: 1-列表模式, 2-矩阵模式
     */
    private Integer readingMode;

    /**
     * 默认截图尺寸-宽
     */
    private Integer defaultScreenshotWidth;

    /**
     * 默认截图尺寸-高
     */
    private Integer defaultScreenshotHeight;

    /**
     * 滚轮灵敏度
     */
    private Integer scrollSensitivity;

    /**
     * 拖拽灵敏度
     */
    private Integer dragSensitivity;

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
        UserSetting other = (UserSetting) that;
        return (this.getUserSettingsId() == null ? other.getUserSettingsId() == null : this.getUserSettingsId().equals(other.getUserSettingsId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getEnhanceSwitch() == null ? other.getEnhanceSwitch() == null : this.getEnhanceSwitch().equals(other.getEnhanceSwitch()))
            && (this.getReadingMode() == null ? other.getReadingMode() == null : this.getReadingMode().equals(other.getReadingMode()))
            && (this.getDefaultScreenshotWidth() == null ? other.getDefaultScreenshotWidth() == null : this.getDefaultScreenshotWidth().equals(other.getDefaultScreenshotWidth()))
            && (this.getDefaultScreenshotHeight() == null ? other.getDefaultScreenshotHeight() == null : this.getDefaultScreenshotHeight().equals(other.getDefaultScreenshotHeight()))
            && (this.getScrollSensitivity() == null ? other.getScrollSensitivity() == null : this.getScrollSensitivity().equals(other.getScrollSensitivity()))
            && (this.getDragSensitivity() == null ? other.getDragSensitivity() == null : this.getDragSensitivity().equals(other.getDragSensitivity()))
            && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateBy() == null ? other.getUpdateBy() == null : this.getUpdateBy().equals(other.getUpdateBy()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getUserSettingsId() == null) ? 0 : getUserSettingsId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getEnhanceSwitch() == null) ? 0 : getEnhanceSwitch().hashCode());
        result = prime * result + ((getReadingMode() == null) ? 0 : getReadingMode().hashCode());
        result = prime * result + ((getDefaultScreenshotWidth() == null) ? 0 : getDefaultScreenshotWidth().hashCode());
        result = prime * result + ((getDefaultScreenshotHeight() == null) ? 0 : getDefaultScreenshotHeight().hashCode());
        result = prime * result + ((getScrollSensitivity() == null) ? 0 : getScrollSensitivity().hashCode());
        result = prime * result + ((getDragSensitivity() == null) ? 0 : getDragSensitivity().hashCode());
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
        sb.append(", userSettingsId=").append(userSettingsId);
        sb.append(", userId=").append(userId);
        sb.append(", enhanceSwitch=").append(enhanceSwitch);
        sb.append(", readingMode=").append(readingMode);
        sb.append(", defaultScreenshotWidth=").append(defaultScreenshotWidth);
        sb.append(", defaultScreenshotHeight=").append(defaultScreenshotHeight);
        sb.append(", scrollSensitivity=").append(scrollSensitivity);
        sb.append(", dragSensitivity=").append(dragSensitivity);
        sb.append(", createBy=").append(createBy);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateBy=").append(updateBy);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}