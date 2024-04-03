package cn.staitech.fr.domain.in;

import java.util.List;

import javax.validation.constraints.NotNull;

import cn.staitech.common.core.domain.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
* @ClassName: SplitVerificationQueryIn
* @Description:
* @author wanglibei
* @date 2024年4月3日
* @version V1.0
 */
@Data
public class SplitVerificationQueryIn extends PageRequest {
    
	/**
	* @Fields serialVersionUID :
	*/
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "专题id")
    @NotNull(message = "[专题id]不能为空")
    private Long specialId;
    
    @ApiModelProperty(value = "动物编号")
    private String animalCode;
    
    @ApiModelProperty(value = "脏器类型")
    private Long categoryId;
    
    @ApiModelProperty(value = "只看核对异常数据  0：全部  1：只看异常数据")
    private Integer checkType;
    
    @ApiModelProperty(value = "查看切片明细  0：否  1：是")
    private Integer detailType;
    
    //明细查询
    @ApiModelProperty(value = "切片编号")
    private String fileName;
    
    @ApiModelProperty(value = "脏器名称")
    private String categoryName;
    
    @ApiModelProperty(value = "切片列表")
    private List<Long> slideIdList;

   

}
