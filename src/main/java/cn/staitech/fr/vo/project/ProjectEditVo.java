package cn.staitech.fr.vo.project;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class ProjectEditVo {
    @ApiModelProperty("项目id")
    @NotNull(message = "项目id不能为空")
    private Long projectId;

    @ApiModelProperty("项目名称")
    @NotBlank(message = "{SpecialInsertVo.specialName.isnull}")
    @Size(max = 100,message = "{SpecialInsertVo.specialNumber.length}")
    private String projectName;

    @ApiModelProperty(value = "种属id")
    private String speciesId;

    @ApiModelProperty(value = "试验类型")
    private Integer trialId;

    @ApiModelProperty(value = "染色类型")
    private Integer colorType;

    @ApiModelProperty(value = "机构id")
    private Long organizationId;

    @ApiModelProperty(value = "项目负责人")
    private Long principal;

}
