package cn.staitech.fr.vo.annotation;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AnnotationCountByCategory {


    @ApiModelProperty(value = "脏器id")
    private Long categoryId;



    @ApiModelProperty(value = "项目id")
    private Integer totalCount;

}
