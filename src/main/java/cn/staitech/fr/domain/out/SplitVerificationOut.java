package cn.staitech.fr.domain.out;

import java.util.Map;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
* @ClassName: SplitVerificationOut
* @Description:
* @author wanglibei
* @date 2024年4月3日
* @version V1.0
 */
@Data
public class SplitVerificationOut {

	@ApiModelProperty(value = "动物编号")
	private String animalCode;
	
    @ApiModelProperty(value = "切图结果（0：正确 1：错误）")
    private Integer processFlag;
    
    @ApiModelProperty(value = "修正标识（ 0：初始 1：正确 2：修正正常 3：错误 ）")
    private Integer checkStatus;

    @ApiModelProperty(value = "蜡块表脏器信息")
    //private String waxOrgan;
    private Map<String,Long> waxOrgan;

    @ApiModelProperty(value = "切图脏器信息")
    //private String annoOrgan;
    private Map<String,Long> annoOrgan;
    
    @ApiModelProperty(value = "切片编号")
    private String fileName;

}
