package cn.staitech.fr.domain.in;

import cn.staitech.common.core.domain.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Map;

/**
 * @author wmy
 * @version 1.0
 * @date 2024/4/2 9:44
 * @description
 */
@Data
public class OrganDisassemblyQueryIn extends PageRequest {
    @ApiModelProperty(value = "专题id")
    @NotNull(message = "[专题id]不能为空")
    private Long specialId;

    @ApiModelProperty(value = "切片编号")
    private String fileName;

    @ApiModelProperty(value = "启动者")
    // 启动者
    private Long initiateBy;

    @ApiModelProperty(value = "启动时间")
    // 启动时间
    private Map<String, Date> initiateTimeParams;

    @ApiModelProperty(value = "脏器类型")
    private Long categoryId;

}
