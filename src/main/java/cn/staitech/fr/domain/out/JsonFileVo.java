package cn.staitech.fr.domain.out;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JsonFileVo {
	 @ApiModelProperty(value = "总大小")
     private Long totalSize;
     @ApiModelProperty(value = "文件列表")
     private List<String> files;
}
