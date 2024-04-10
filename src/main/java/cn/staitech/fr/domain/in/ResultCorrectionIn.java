package cn.staitech.fr.domain.in;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
* @ClassName: ResultCorrectionIn
* @Description:
* @author wanglibei
* @date 2024年4月3日
* @version V1.0
 */
@Data
public class ResultCorrectionIn{
    

	@ApiModelProperty(value = "切片id")
    @NotNull(message = "[切片id]不能为空")
    private Long slideId;
    
    @ApiModelProperty(value = "修正状态  0：修正  1：取消修正")
    private Integer correctionStatus;
    

   

}
