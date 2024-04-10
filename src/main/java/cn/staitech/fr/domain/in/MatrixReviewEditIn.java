package cn.staitech.fr.domain.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author wudi
 * @Date 2024/4/10 16:56
 * @desc
 */
@Data
public class MatrixReviewEditIn {

    @ApiModelProperty(value = "专题id")
    private Long specialId;

    @ApiModelProperty(value = "对照组id")
    private String groupId;
}
