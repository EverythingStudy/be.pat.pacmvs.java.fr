package cn.staitech.fr.vo.annotation.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AnnotationById {


    @NotNull(message = "{ARGUMENT_INVALID}")
    @ApiModelProperty(value = "轮廓id", required = true)
    private Long marking_id;



    @ApiModelProperty(value = "项目id", required = true)
    private Long project_id;

    private Boolean isBatch;

    private String traceId;

}
