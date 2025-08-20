package cn.staitech.fr.domain.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.List;

/**
 * @Author wudi
 * @Date 2024/4/12 11:15
 * @desc
 */
@Data
public class AiDownloadIn {
    @ApiModelProperty(value = "切片id")
    @Size(max = 10, message = "")
    private List<Long> ids;

    @ApiModelProperty(value = "项目Id")
    private Long projectId;
}
