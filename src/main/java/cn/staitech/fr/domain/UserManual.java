package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户手册
 * @TableName tb_user_manual
 */
@TableName(value ="tb_user_manual")
@Data
public class UserManual implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long userManualId;

    /**
     * 用户手册地址
     */
    private String userManualUrl;

    /**
     * 语种类型 1:中文；2:英文
     */
    private Integer messagesType;

    /**
     * 组织机构ID
     */
    private Long organizationId;

    /**
     * 默认为0，1为删除
     */
    private Integer delFlag;

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
        UserManual other = (UserManual) that;
        return (this.getUserManualId() == null ? other.getUserManualId() == null : this.getUserManualId().equals(other.getUserManualId()))
            && (this.getUserManualUrl() == null ? other.getUserManualUrl() == null : this.getUserManualUrl().equals(other.getUserManualUrl()))
            && (this.getMessagesType() == null ? other.getMessagesType() == null : this.getMessagesType().equals(other.getMessagesType()))
            && (this.getOrganizationId() == null ? other.getOrganizationId() == null : this.getOrganizationId().equals(other.getOrganizationId()))
            && (this.getDelFlag() == null ? other.getDelFlag() == null : this.getDelFlag().equals(other.getDelFlag()))
            && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()))
            && (this.getUpdateBy() == null ? other.getUpdateBy() == null : this.getUpdateBy().equals(other.getUpdateBy()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getUserManualId() == null) ? 0 : getUserManualId().hashCode());
        result = prime * result + ((getUserManualUrl() == null) ? 0 : getUserManualUrl().hashCode());
        result = prime * result + ((getMessagesType() == null) ? 0 : getMessagesType().hashCode());
        result = prime * result + ((getOrganizationId() == null) ? 0 : getOrganizationId().hashCode());
        result = prime * result + ((getDelFlag() == null) ? 0 : getDelFlag().hashCode());
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
        sb.append(", userManualId=").append(userManualId);
        sb.append(", userManualUrl=").append(userManualUrl);
        sb.append(", messagesType=").append(messagesType);
        sb.append(", organizationId=").append(organizationId);
        sb.append(", delFlag=").append(delFlag);
        sb.append(", createBy=").append(createBy);
        sb.append(", updateBy=").append(updateBy);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}