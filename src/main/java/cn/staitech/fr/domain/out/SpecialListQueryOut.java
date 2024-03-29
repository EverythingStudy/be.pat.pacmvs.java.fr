package cn.staitech.fr.domain.out;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Author wudi
 * @Date 2024/3/29 14:38
 * @desc
 */
@Data
public class SpecialListQueryOut {
    @ApiModelProperty(value = "专题id")
    private Long specialId;

    @ApiModelProperty("专题编号")
    private String topicName;

    @ApiModelProperty(value = "专题名称")
    private String specialName;

    @ApiModelProperty(value = "种属")
    private String speciesName;

    @ApiModelProperty(value = "种属英文")
    private String speciesNameEn;

    @ApiModelProperty(value = "试验类型")
    private String trialType;

    @ApiModelProperty(value = "染色类型id")
    private String colorType;

    @ApiModelProperty(value = "染色类型名称")
    private String colorName;

    @ApiModelProperty(value = "染色类型英文名称")
    private String colorNameEn;

    @ApiModelProperty(value = "状态(0待启动，1进行中，2暂停，3锁定，4已完成)")
    private Integer status;

    @ApiModelProperty(value = "创建者")
    private String createName;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
