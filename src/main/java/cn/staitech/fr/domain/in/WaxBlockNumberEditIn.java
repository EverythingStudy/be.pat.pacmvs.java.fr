package cn.staitech.fr.domain.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author wudi
 * @Date 2024/3/28 16:48
 * @desc
 */
@Data
public class WaxBlockNumberEditIn {
    @ApiModelProperty(value = "主键id")
    @NotNull(message = "编号主键id不能为空")
    private Long numberId;

    @ApiModelProperty("机构id")
    private Long organzationId;

    @ApiModelProperty("文件名称")
    private String fileName;
}
