package cn.staitech.fr.domain.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author wudi
 * @Date 2024/3/29 16:00
 * @desc
 */
@Data
public class SpecialEditIn {
    @ApiModelProperty("专题id")
    @NotNull(message = "")
    private Long specialId;

    @ApiModelProperty("专题名称")
    private String specialName;

    @ApiModelProperty(value = "种属id")
    private String speciesId;

    @ApiModelProperty(value = "试验类型")
    private Integer trialId;

    @ApiModelProperty(value = "染色类型")
    private Integer colorType;

    @ApiModelProperty(value = "机构id")
    private Long organizationId;


}
