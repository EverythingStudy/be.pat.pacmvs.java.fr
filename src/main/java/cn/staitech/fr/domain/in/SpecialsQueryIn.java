package cn.staitech.fr.domain.in;

import cn.staitech.common.core.domain.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * @Author wudi
 * @Date 2024/4/10 15:04
 * @desc
 */
@Data
public class SpecialsQueryIn extends PageRequest {

    @ApiModelProperty("专题编号")
    private String topicName;

    @ApiModelProperty(value = "专题名称")
    private String specialName;

    @ApiModelProperty(value = "种属id")
    private String speciesId;

    @ApiModelProperty(value = "试验类型")
    private Integer trialId;

    @ApiModelProperty(value = "染色类型")
    private Integer colorType;

    @ApiModelProperty(value = "状态(0待启动，1进行中，2暂停，3已完成，4锁定)")
    private Integer status;

    @ApiModelProperty(value = "专题名称")
    private String createName;

    @ApiModelProperty(value = "机构id", hidden = true)
    private Long organizationId;

    @ApiModelProperty(value = "操作人", hidden = true)
    private Long loginUserId;

    @ApiModelProperty(value = "时间范围")
    private Map<String, Date> createTimeParams;
}
