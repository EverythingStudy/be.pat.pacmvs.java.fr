package cn.staitech.fr.domain.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author wudi
 * @Date 2024/3/29 15:37
 * @desc
 */
@Data
public class SpecialAddIn {

    @ApiModelProperty(value = "专题id")
    @NotNull(message = "专题编号不能为空")
    private Long topicId;

    @ApiModelProperty(value = "专题名称")
    @NotBlank(message = "专题名称不能为空")
    private String specialName;

    @ApiModelProperty(value = "种属id")
    @NotBlank(message = "种属id不能为空")
    private String speciesId;

    @ApiModelProperty(value = "试验类型")
    @NotBlank(message = "试验类型不能为空")
    private String trialType;

    @ApiModelProperty(value = "染色类型")
    @NotNull(message = "染色类型不能为空")
    private Integer colorType;

    @ApiModelProperty(value = "机构id")
    @NotNull(message = "机构id不能为空")
    private Long organizationId;



}
