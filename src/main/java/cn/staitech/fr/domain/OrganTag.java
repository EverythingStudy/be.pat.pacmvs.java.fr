package cn.staitech.fr.domain;

import cn.hutool.core.date.DatePattern;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @date: 2020/5/7 16:05
 * @author: mu
 * @description: 脏器标签表
 * @version: 2.6.0
 */
@TableName(value ="tb_organ_tag")
@Data
public class OrganTag implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long organTagId;

    @ApiModelProperty(value = "种属")
    private String speciesId;

    @ApiModelProperty(value = "脏器名称")
    private String organName;

    @ApiModelProperty( value = "脏器名称")
    private String organEn;

    @ApiModelProperty("脏器标签编码")
    private String organTagCode;

    @ApiModelProperty(value = "标签简称")
    private String abbreviation;

    @ApiModelProperty(name = "organizationId" , value = "机构id")
    private Long organizationId;

    @ApiModelProperty(name = "rgb" , value = "rgb值")
    private String rgb;

    @ApiModelProperty(name = "chromaticValue" , value = "色值")
    private String chromaticValue;

    @ApiModelProperty(name = "createTime" , value = "创建时间")
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    private Date createTime;

    @ApiModelProperty(name = "createBy" , value = "创建者")
    private Long createBy;

    @ApiModelProperty(value = "更新者")
    private Long updateBy;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(name = "delFlag" , value = "删除标志(0-正常，1-删除)")
    private Boolean delFlag;

    @ApiModelProperty(name = "algorithmSupportStatus" , value = "算法是否支持(0-支持，1-不支持)")
    private Integer algorithmSupportStatus;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;




}