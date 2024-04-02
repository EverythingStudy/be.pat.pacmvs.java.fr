package cn.staitech.fr.domain.in;

import cn.staitech.fr.domain.out.ImageListOutVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author wudi
 * @Date 2024/3/7 14:03
 * @desc
 */
@Data
public class ChoiceSaveInVo {
    @ApiModelProperty("专题id")
    @NotNull(message = "{StartPredictionIn.specialId.isnull}")
    private Long specialId;

    @ApiModelProperty(value = "图像ID", required = true)
    @NotEmpty(message = "{PICTURE_NON_CHOOSE}")
    private List<ImageListOutVO> images;



}
