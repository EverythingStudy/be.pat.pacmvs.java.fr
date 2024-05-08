package cn.staitech.fr.domain.in;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author wudi
 * @Date 2024/3/28 16:48
 * @desc
 */
@Data
public class WaxBlockNumberEditIn {
    @ApiModelProperty(value = "主键id")
    @NotNull(message = "{ID.ISNULL}")
    private Long numberId;

    @ApiModelProperty("机构id")
    private Long organizationId;

    @ApiModelProperty("文件名称")
    private String fileName;
}
