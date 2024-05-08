package cn.staitech.fr.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author wmy
 * @version 1.0
 * @date 2024/4/2 14:41
 * @description
 */
@Data
public class SingleOrganNumber {
    @ApiModelProperty(value = "脏器数量")
    private Long organNumber;
    @ApiModelProperty(value = "单脏器类型")
    private Long categoryId;
    @ApiModelProperty(value = "切片id")
    private Long slideId;
}
