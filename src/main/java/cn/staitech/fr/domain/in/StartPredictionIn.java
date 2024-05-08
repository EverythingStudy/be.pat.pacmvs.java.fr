package cn.staitech.fr.domain.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

import javax.validation.constraints.NotNull;

/**
 * @author wanglibei
 * @version V1.0
 * @ClassName: StartPredictionIn
 * @Description:
 * @date 2023年11月6日
 */
@Data
public class StartPredictionIn {


	@ApiModelProperty(value = "启动切片方式 0：全部启动 1：部分启动")
	@NotNull(message = "{StartPredictionIn.type.isnull}")
	private Integer type;
	
	@ApiModelProperty(value = "专题ID")
	@NotNull(message = "{StartPredictionIn.specialId.isnull}")
	private Long specialId;
	
    @ApiModelProperty(value = "切片ID")
    private List<Long> slideIdList;
}
