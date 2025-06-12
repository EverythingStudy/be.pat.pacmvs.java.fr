package cn.staitech.fr.vo.project;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@Data
public class ProjectVo {

    @ApiModelProperty(value = "项目id")
    @NotNull(message = "{StartPredictionIn.specialId.isnull}")
    private Long topicId;

    @ApiModelProperty(value = "项目名称")
    @NotBlank(message = "{SpecialInsertVo.specialName.isnull}")
    @Size(max = 100,message = "{SpecialInsertVo.specialNumber.length}")
    private String projectName;

    @ApiModelProperty(value = "种属id")
    @NotBlank(message = "{SpecialInsertVo.species.isnull}")
    private String speciesId;

    @ApiModelProperty(value = "试验类型")
    @NotNull(message = "{SpecialInsertVo.trialType.isnull}")
    private Integer trialId;

    @ApiModelProperty(value = "染色类型")
    @NotNull(message = "{SpecialInsertVo.stainType.isnull}")
    private Integer colorType;

    @ApiModelProperty(value = "机构id")
    @NotNull(message = "机构id不能为空")
    private Long organizationId;

    @ApiModelProperty(value = "项目负责人")
    private Long principal;

}
