package cn.staitech.fr.vo.project;

import cn.staitech.common.core.constant.Constants;
import cn.staitech.common.core.domain.DateRangeReq;
import cn.staitech.common.core.domain.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author mugw
 * @version 2.6.0
 * @description
 * @date 2025/5/14 13:44:14
 */
@Data
public class ProjectPageReq extends PageRequest {

    @ApiModelProperty(value = "项目id")
    private Long projectId;

    @ApiModelProperty("项目编号")
    private String topicName;

    @ApiModelProperty(value = "项目名称")
    private String projectName;

    @ApiModelProperty(value = "种属id")
    private String speciesId;

    @ApiModelProperty(value = "试验类型")
    private Integer trialId;

    @ApiModelProperty(value = "染色类型")
    private Integer colorType;

    @ApiModelProperty(value = "状态(0-待启动，1-进行中，2-暂停，3-已完成，6-归档)")
    private List<Integer> status;

    @ApiModelProperty(value = "项目名称")
    private String createName;

    @ApiModelProperty(value = "机构id", hidden = true)
    private Long organizationId;

    @ApiModelProperty(value = "用户id", hidden = true)
    private Long userId;

    @ApiModelProperty(value = "时间范围")
    private DateRangeReq createTimeParams;

    @ApiModelProperty(value = "回收时间")
    private DateRangeReq recoveryTimeParams;

    @ApiModelProperty(value = "到期时间")
    private DateRangeReq expireTimeParams;

    @ApiModelProperty(value = "项目负责人名称")
    private String principalName;

    private String delFlag = Constants.DEL_FLAG_NORMAL;
}
