package cn.staitech.fr.domain.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author wudi
 * @Date 2024/4/1 14:51
 * @desc
 */
@Data
public class AddMemberIn {
    @NotNull(message = "{StartPredictionIn.specialId.isnull}")
    @ApiModelProperty(value = "专题id", required = true)
    private Long specialId;
    @NotEmpty(message = "{AnnotationDeleteVO.createBy.isnull}")
    @ApiModelProperty(value = "用户id列表", required = true)
    private List<Long> userId;

}
