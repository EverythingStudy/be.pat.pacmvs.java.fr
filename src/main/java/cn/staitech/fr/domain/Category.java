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
    private Long species;

    /**
     * 脏器名称
     */
    @ApiModelProperty(name = "organName" , value = "脏器名称")
    private String organName;

    @ApiModelProperty( value = "脏器名称")
    private String organEn;

    /**
     * 标签全称
     */
    @ApiModelProperty(name = "organId" , value = "标签标签id")
    private String organId;

    /**
     * 标签简称
     */
    @ApiModelProperty(name = "categoryAbbreviation" , value = "标签简称")
    private String categoryAbbreviation;

    /**
     * 标签简称
     */
    @ApiModelProperty(name = "organizationId" , value = "机构id")
    private Long organizationId;


    /**
     * 标签简称
     */
    @ApiModelProperty(name = "rgb" , value = "rgb值")
    private String rgb;

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

    /**
     * 创建者
     */
    @ApiModelProperty(name = "delFlag" , value = "删除标志")
    private String delFlag;

    
    @ApiModelProperty(name = "algorithmSupportStatus" , value = "算法是否支持(0:支持，1:不支持)")
    private Integer algorithmSupportStatus;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;




}