package cn.staitech.fr.domain.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author wudi
 * @Date 2024/4/12 11:15
 * @desc
 */
@Data
public class AiDownloadIn {
    @ApiModelProperty(value = "待导出报告数据id；")
    private List<Long> ids;
}
