package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
* 机构表
* @TableName sys_organization
*/
public class SysOrganization implements Serializable {

    /**
    * 机构ID、序号
    */
    @NotNull(message="[机构ID、序号]不能为空")
    @ApiModelProperty("机构ID、序号")
    @TableId(type = IdType.AUTO)
    private Long organizationId;
    /**
    * 机构名称
    */
    @NotBlank(message="[机构名称]不能为空")
    @Size(max= 500,message="编码长度不能超过500")
    @ApiModelProperty("机构名称")
    @Length(max= 500,message="编码长度不能超过500")
    private String organizationName;
    /**
    * 联系人
    */
    @Size(max= 30,message="编码长度不能超过30")
    @ApiModelProperty("联系人")
    @Length(max= 30,message="编码长度不能超过30")
    private String contactName;
    /**
    * 联系方式
    */
    @Size(max= 20,message="编码长度不能超过20")
    @ApiModelProperty("联系方式")
    @Length(max= 20,message="编码长度不能超过20")
    private String phoneNumber;
    /**
    * 帐号状态（0正常开启 1禁用）
    */
    @ApiModelProperty("帐号状态（0正常开启 1禁用）")
    private String status;
    /**
    * 删除标志（0代表存在 1代表删除）
    */
    @ApiModelProperty("删除标志（0代表存在 1代表删除）")
    private String delFlag;
    /**
    * 创建者ID
    */
    @ApiModelProperty("创建者ID")
    private Long createBy;
    /**
    * 创建时间
    */
    @ApiModelProperty("创建时间")
    private Date createTime;
    /**
    * 更新者ID
    */
    @ApiModelProperty("更新者ID")
    private Long updateBy;
    /**
    * 更新时间
    */
    @ApiModelProperty("更新时间")
    private Date updateTime;
    /**
    * 机构编号
    */
    @ApiModelProperty("机构编号")
    private Integer organizationNumber;

    /**
    * 机构ID、序号
    */
    private void setOrganizationId(Long organizationId){
    this.organizationId = organizationId;
    }

    /**
    * 机构名称
    */
    private void setOrganizationName(String organizationName){
    this.organizationName = organizationName;
    }

    /**
    * 联系人
    */
    private void setContactName(String contactName){
    this.contactName = contactName;
    }

    /**
    * 联系方式
    */
    private void setPhoneNumber(String phoneNumber){
    this.phoneNumber = phoneNumber;
    }

    /**
    * 帐号状态（0正常开启 1禁用）
    */
    private void setStatus(String status){
    this.status = status;
    }

    /**
    * 删除标志（0代表存在 1代表删除）
    */
    private void setDelFlag(String delFlag){
    this.delFlag = delFlag;
    }

    /**
    * 创建者ID
    */
    private void setCreateBy(Long createBy){
    this.createBy = createBy;
    }

    /**
    * 创建时间
    */
    private void setCreateTime(Date createTime){
    this.createTime = createTime;
    }

    /**
    * 更新者ID
    */
    private void setUpdateBy(Long updateBy){
    this.updateBy = updateBy;
    }

    /**
    * 更新时间
    */
    private void setUpdateTime(Date updateTime){
    this.updateTime = updateTime;
    }

    /**
    * 机构编号
    */
    private void setOrganizationNumber(Integer organizationNumber){
    this.organizationNumber = organizationNumber;
    }


    /**
    * 机构ID、序号
    */
    private Long getOrganizationId(){
    return this.organizationId;
    }

    /**
    * 机构名称
    */
    private String getOrganizationName(){
    return this.organizationName;
    }

    /**
    * 联系人
    */
    private String getContactName(){
    return this.contactName;
    }

    /**
    * 联系方式
    */
    private String getPhoneNumber(){
    return this.phoneNumber;
    }

    /**
    * 帐号状态（0正常开启 1禁用）
    */
    private String getStatus(){
    return this.status;
    }

    /**
    * 删除标志（0代表存在 1代表删除）
    */
    private String getDelFlag(){
    return this.delFlag;
    }

    /**
    * 创建者ID
    */
    private Long getCreateBy(){
    return this.createBy;
    }

    /**
    * 创建时间
    */
    private Date getCreateTime(){
    return this.createTime;
    }

    /**
    * 更新者ID
    */
    private Long getUpdateBy(){
    return this.updateBy;
    }

    /**
    * 更新时间
    */
    private Date getUpdateTime(){
    return this.updateTime;
    }

    /**
    * 机构编号
    */
    private Integer getOrganizationNumber(){
    return this.organizationNumber;
    }

}
