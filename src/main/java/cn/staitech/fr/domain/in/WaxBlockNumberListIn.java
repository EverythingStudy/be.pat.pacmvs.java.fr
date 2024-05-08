package cn.staitech.fr.domain.in;

import cn.staitech.common.core.domain.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * @Author wudi
 * @Date 2024/3/28 16:24
 * @desc
 */
@Data
public class WaxBlockNumberListIn extends PageRequest {
    @ApiModelProperty(value = "专题编号")
    private String topicName;
    @ApiModelProperty(value = "种属")
    private String speciesId;
    @ApiModelProperty(value = "机构id")
    private Long organizationId;
    @ApiModelProperty(value = "时间范围")
    private Map<String, Date> createTimeParams;
}
