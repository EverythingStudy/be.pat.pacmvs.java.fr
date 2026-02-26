package cn.staitech.fr.vo.project;

import cn.staitech.fr.enums.ProjectStatusEnum;
import cn.staitech.fr.enums.ProjectStatusLogEnum;
import cn.staitech.sft.logaudit.annotation.IgnoreLogField;
import cn.staitech.sft.logaudit.annotation.LogFieldEnumConvert;
import cn.staitech.sft.logaudit.req.LogAuditBaseReq;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import static cn.staitech.fr.constant.LogFieldConvertConstants.CODE;
import static cn.staitech.fr.constant.LogFieldConvertConstants.VALUE;

/**
 * @Author wudi
 * @Date 2024/3/29 16:22
 * @desc
 */
@Data
public class ProjectStatusVo extends LogAuditBaseReq<ProjectDetailVo> {
    @IgnoreLogField
    @ApiModelProperty(required = true, value = "项目id")
    private Long projectId;
    /**
     * 以下属性暂未使用
     */
    @LogFieldEnumConvert(enumClass = ProjectStatusLogEnum.class, valueField = CODE, convertField = VALUE)
    @ApiModelProperty( value = "状态:1-启动，2-暂停，3-完成，6-归档")
    private Integer status;

    @IgnoreLogField
    @ApiModelProperty(value = "用户名")
    private String userName;

    @IgnoreLogField
    @ApiModelProperty(value = "密码")
    private String pwd;

    @IgnoreLogField
    @ApiModelProperty(value = "原因")
    private String reason;
}
