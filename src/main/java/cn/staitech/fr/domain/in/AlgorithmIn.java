package cn.staitech.fr.domain.in;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
* @ClassName: AlgorithmIn
* @Description:
* @author wanglibei
* @date 2024年5月7日
* @version V1.0
 */
@Data
public class AlgorithmIn {

    @ApiModelProperty(value = "专题id")
    @NotNull(message = "[专题id]不能为空")
    private Long specialId;

}
