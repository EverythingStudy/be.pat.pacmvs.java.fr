package cn.staitech.fr.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExportAiDTO {
    @ApiModelProperty(value = "专题号")
    private String topicName;

    @ApiModelProperty(value = "项目名称")
    private String specialName;

    @ApiModelProperty(value = "图像名称")
    private String imageName;

    @ApiModelProperty(value = "脏器名称")
    private String organName;

}