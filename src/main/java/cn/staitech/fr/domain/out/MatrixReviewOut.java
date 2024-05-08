package cn.staitech.fr.domain.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author wudi
 * @Date 2024/4/10 16:52
 * @desc
 */
@Data
public class MatrixReviewOut {
    @ApiModelProperty(value = "对照组id")
    private String groupId;
    @ApiModelProperty(value = "对照组名称")
    private String groupCode;


}
