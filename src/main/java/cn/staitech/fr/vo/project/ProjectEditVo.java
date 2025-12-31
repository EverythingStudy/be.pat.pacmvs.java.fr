package cn.staitech.fr.vo.project;

import cn.staitech.fr.enums.ColorTypeEnum;
import cn.staitech.fr.enums.TrialTypeEnum;
import cn.staitech.fr.mapper.SpeciesMapper;
import cn.staitech.fr.mapper.SysOrganizationMapper;
import cn.staitech.fr.mapper.UserMapper;
import cn.staitech.sft.logaudit.annotation.IgnoreLogField;
import cn.staitech.sft.logaudit.annotation.LogFieldDBConvert;
import cn.staitech.sft.logaudit.annotation.LogFieldEnumConvert;
import cn.staitech.sft.logaudit.req.LogAuditBaseReq;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.util.Date;

import static cn.staitech.fr.constant.LogFieldConvertConstants.*;

@Data
public class ProjectEditVo extends LogAuditBaseReq<ProjectEditVo> {
    @IgnoreLogField
    @ApiModelProperty("项目id")
    @NotNull(message = "项目id不能为空")
    private Long projectId;

    @ApiModelProperty("项目名称")
    @NotBlank(message = "{SpecialInsertVo.specialName.isnull}")
    @Size(max = 100,message = "{SpecialInsertVo.specialNumber.length}")
    private String projectName;

    @LogFieldDBConvert(mapper = SpeciesMapper.class, convertField = NAME)
    @ApiModelProperty(value = "种属id")
    private String speciesId;

    @LogFieldEnumConvert(enumClass = TrialTypeEnum.class, valueField = CODE, convertField = VALUE)
    @ApiModelProperty(value = "试验类型")
    private Integer trialId;

    @LogFieldEnumConvert(enumClass = ColorTypeEnum.class, valueField = CODE, convertField = VALUE)
    @ApiModelProperty(value = "染色类型")
    private Integer colorType;

    @LogFieldDBConvert(mapper = SysOrganizationMapper.class, convertField = ORGANIZATION_NAME)
    @ApiModelProperty(value = "机构id")
    private Long organizationId;

    @LogFieldDBConvert(mapper = UserMapper.class, convertField = USER_NAME)
    @ApiModelProperty(value = "项目负责人")
    private Long principal;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

}
