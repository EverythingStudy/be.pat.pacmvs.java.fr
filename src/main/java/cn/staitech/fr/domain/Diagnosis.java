package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import cn.staitech.fr.vo.diagnosis.SpecialDiagnosisAddVo.DdefinitionChild;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 人工诊断表
 * </p>
 *
 * @author wanglibei
 * @since 2024-04-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("fr_diagnosis")
@ApiModel(value="Diagnosis对象", description="人工诊断表")
public class Diagnosis implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "诊断ID")
    @TableId(value = "diagnosis_id", type = IdType.AUTO)
    private Long diagnosisId;

    @ApiModelProperty(value = "专题id")
    private Long specialId;

    @ApiModelProperty(value = "分组id")
    private Long groupId;

    @ApiModelProperty(value = "专题选片ID")
    private Long singleId;

    @ApiModelProperty(value = "诊断脏器")
    private String viscera;

    @ApiModelProperty(value = "诊断脏器名称")
    private String viscreaName;

    @ApiModelProperty(value = "部位")
    private String position;

    @ApiModelProperty(value = "部位名称")
    private String positionName;
    
    @ApiModelProperty(name = "positionSource" , value = "部位来源 0：字典  1：自定义")
	private int positionSource;

    @ApiModelProperty(value = "病理改变")
    private String lesion;

    @ApiModelProperty(value = "病理病变名称")
    private String lesionName;
    
    @ApiModelProperty(name = "lesionSource" , value = "病理改变来源 0：字典  1：自定义")
	private int lesionSource;

    @ApiModelProperty(value = "修饰")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<DdefinitionChild> ddefinition;

    @ApiModelProperty(value = "修饰名称")
    private String ddefinitionName;
    
//    @ApiModelProperty(name = "ddefinitionSource" , value = "修饰来源 0：字典  1：自定义")
//	private String ddefinitionSource;

    @ApiModelProperty(value = "病变级别")
    private String grade;

    @ApiModelProperty(value = "病变级别名称")
    private String gradeName;
    
    @ApiModelProperty(name = "gradeSource" , value = "病变级别来源 0：字典  1：自定义")
	private int gradeSource;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "逻辑删除状态（0:删除 1:未删除）")
    private Integer deleteFlag;

    @ApiModelProperty(value = "创建人id")
    private Long createBy;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新人id")
    private Long updateBy;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;


}
