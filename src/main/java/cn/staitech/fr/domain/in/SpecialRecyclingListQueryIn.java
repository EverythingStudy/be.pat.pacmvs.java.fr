package cn.staitech.fr.domain.in;

import cn.staitech.common.core.domain.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * @Author wudi
 * @Date 2024/3/29 16:47
 * @desc
 */
@Data
public class SpecialRecyclingListQueryIn extends PageRequest {
    @ApiModelProperty("专题编号")
    private String topicName;

    @ApiModelProperty(value = "专题名称")
    private String specialName;

    @ApiModelProperty(value = "专题创建时间")
    private Map<String, Date> createTimeParams;

    @ApiModelProperty(value = "回收时间")
    private Map<String, Date> recoveryTimeParams;

    @ApiModelProperty(value = "到期时间")
    private Map<String, Date> expireTimeParams;
}
