package cn.staitech.fr.domain.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContourFileVo {
	 @ApiModelProperty(value = "总大小")
     private Long totalSize;
}
