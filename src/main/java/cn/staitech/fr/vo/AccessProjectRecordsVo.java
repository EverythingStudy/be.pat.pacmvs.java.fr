package cn.staitech.fr.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author mugw
 * @version 2.6.0
 * @description 近一个月访问记录
 * @date 2025/5/14 13:44:14
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AccessProjectRecordsVo {

    @ApiModelProperty(value = "专题编号")
    private String topicName;

    @ApiModelProperty(value = "项目名称")
    private String specialName;

    @ApiModelProperty(value = "种属名称")
    private String speciesName;

    @ApiModelProperty(value = "试验类型")
    private String trialType;

    @ApiModelProperty(value = "图像数量")
    private String analysisSum;

    @ApiModelProperty(value = "分析数量")
    private String analysisCount;

    @ApiModelProperty(value = "访问时间")
    private String accessTime;

    @ApiModelProperty(value = "状态(0待启动，1进行中，2暂停，3已完成，4锁定)")
    private Integer status;

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private Long specialId;

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private Integer trialId;
}
