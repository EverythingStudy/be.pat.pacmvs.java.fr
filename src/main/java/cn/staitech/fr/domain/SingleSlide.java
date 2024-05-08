package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wmy
 * @version 1.0
 * @date 2024/4/2 13:05
 * @description
 */
@TableName(value = "fr_single_slide")
@Data
public class SingleSlide implements Serializable {

    @ApiModelProperty(value = "单脏器切片id")
    @TableId(value = "single_id", type = IdType.AUTO)
    private Long singleId;

    @ApiModelProperty(value = "切片id")
    private Long slideId;

    @ApiModelProperty(value = "单脏器图片缩略图地址")
    private String thumbUrl;

    @ApiModelProperty(value = "单脏器类型")
    private Long categoryId;

    @ApiModelProperty(value = "0未预测、1预测成功、2预测失败、3预测中")
    private String forecastStatus;

    @ApiModelProperty(value = "人工诊断状态 0：未诊断；1：已诊断")
    private String diagnosisStatus;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "单切片描述")
    private String description;
    
    @ApiModelProperty(value = "异常状态 0：默认值 ；1：未见异常")
    private String abnormalStatus;
    
    @ApiModelProperty(value = "未见异常创建人")
    private Long abnormalCreateBy;
    
    @ApiModelProperty(value = "未见异常创建时间")
    private Date abnormalCreateTime;


}
