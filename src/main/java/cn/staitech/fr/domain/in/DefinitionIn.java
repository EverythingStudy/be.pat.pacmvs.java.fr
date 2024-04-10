package cn.staitech.fr.domain.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
* @ClassName: DefinitionIn
* @Description:清晰度
* @author wanglibei
* @date 2024年3月29日
* @version V1.0
 */
@Data
public class DefinitionIn {

    @ApiModelProperty(value = "切片ID")
    private Long imageId;

    @ApiModelProperty(value = "ai分析状态 1： 2：3：")
    private Integer aiAnalyzed;

}
