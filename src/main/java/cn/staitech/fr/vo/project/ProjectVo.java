package cn.staitech.fr.vo.project;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.enums.ColorTypeEnum;
import cn.staitech.fr.enums.TrialTypeEnum;
import cn.staitech.fr.mapper.SpeciesMapper;
import cn.staitech.fr.mapper.SysOrganizationMapper;
import cn.staitech.fr.mapper.TopicMapper;
import cn.staitech.fr.mapper.UserMapper;
import cn.staitech.fr.vo.project.slide.SlideInfo;
import cn.staitech.sft.logaudit.annotation.IdField;
import cn.staitech.sft.logaudit.annotation.IgnoreLogField;
import cn.staitech.sft.logaudit.annotation.LogFieldDBConvert;
import cn.staitech.sft.logaudit.annotation.LogFieldEnumConvert;
import cn.staitech.sft.logaudit.req.LogAuditBaseReq;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

import static cn.staitech.fr.constant.LogFieldConvertConstants.*;


@Data
public class ProjectVo extends LogAuditBaseReq<ProjectEditVo> {

//    @IgnoreLogField
//    @ApiModelProperty(value = "项目ID")
//    @IdField(index = 1,name = "项目编号",nameEn = "Project ID")
//    private Long projectId;

    @LogFieldDBConvert(mapper = TopicMapper.class, convertField = TOPIC_NAME)
    @ApiModelProperty(value = "项目id")
    @NotNull(message = "{StartPredictionIn.specialId.isnull}")
    private Long topicId;

    @ApiModelProperty(value = "项目名称")
    @NotBlank(message = "{SpecialInsertVo.specialName.isnull}")
    @Size(max = 100,message = "{SpecialInsertVo.specialNumber.length}")
    private String projectName;

    @ApiModelProperty(value = "种属id")
    @NotBlank(message = "{SpecialInsertVo.species.isnull}")
    @LogFieldDBConvert(mapper = SpeciesMapper.class, convertField = NAME)
    private String speciesId;

    @LogFieldEnumConvert(enumClass = TrialTypeEnum.class, valueField = CODE, convertField = VALUE)
    @ApiModelProperty(value = "试验类型")
    @NotNull(message = "{SpecialInsertVo.trialType.isnull}")
    private Integer trialId;

    @LogFieldEnumConvert(enumClass = ColorTypeEnum.class, valueField = CODE, convertField = VALUE)
    @ApiModelProperty(value = "染色类型")
    @NotNull(message = "{SpecialInsertVo.stainType.isnull}")
    private Integer colorType;

    @LogFieldDBConvert(mapper = SysOrganizationMapper.class, convertField = ORGANIZATION_NAME)
    @ApiModelProperty(value = "机构id")
    @NotNull(message = "机构id不能为空")
    private Long organizationId;


    @LogFieldDBConvert(mapper = UserMapper.class, convertField = USER_NAME)
    @ApiModelProperty(value = "项目负责人")
    private Long principal;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "自动关联该专题所有可用切片")
    private List<SlideInfo> slideInfos;

    @ApiModelProperty(value = "将项目负责人账号添加到项目")
    private List<ProjectMemberInfo> projectMemberInfos;

}
