package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户信息表
 * @TableName sys_user
 */
@TableName(value ="sys_user")
@Data
public class User implements Serializable {
    /**
     * 用户ID
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * 用户编码
     */
    @TableField(value = "user_code")
    private Integer userCode;

    /**
     * 角色ID
     */
    @TableField(value = "role_id")
    private Long roleId;

    /**
     * 机构ID
     */
    @TableField(value = "organization_id")
    private Long organizationId;

    /**
     * 用户账号
     */
    @TableField(value = "user_name")
    private String userName;

    /**
     * 用户姓名
     */
    @TableField(value = "nick_name")
    private String nickName;

    /**
     * 用户类型（0系统用户）
     */
    @TableField(value = "user_type")
    private String userType;

    /**
     * 部门
     */
    @TableField(value = "dept")
    private String dept;

    /**
     * 用户邮箱
     */
    @TableField(value = "email")
    private String email;

    /**
     * 手机号码
     */
    @TableField(value = "phonenumber")
    private String phonenumber;

    /**
     * 用户性别（0男 1女）
     */
    @TableField(value = "sex")
    private String sex;

    /**
     * 密码
     */
    @TableField(value = "password")
    private String password;

    /**
     * 帐号状态（0正常 1停用）
     */
    @TableField(value = "status")
    private String status;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    @TableField(value = "del_flag")
    private String delFlag;

    /**
     * 登录状态（0首次登录 1非首次登录）
     */
    @TableField(value = "login_status")
    private String loginStatus;

    /**
     * 最后登录IP
     */
    @TableField(value = "login_ip")
    private String loginIp;

    /**
     * 最后登录时间
     */
    @TableField(value = "login_date")
    private Date loginDate;

    /**
     * 创建者ID
     */
    @TableField(value = "create_by")
    private Long createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新者ID
     */
    @TableField(value = "update_by")
    private Long updateBy;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}