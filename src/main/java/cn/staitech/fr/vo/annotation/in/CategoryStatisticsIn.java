package cn.staitech.fr.vo.annotation.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CategoryStatisticsIn {
    @ApiModelProperty(value = "标注颜色id")
    private Long categoryId;

}
