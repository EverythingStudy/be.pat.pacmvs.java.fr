package cn.staitech.fr.domain.in;

import cn.staitech.fr.domain.out.ImageListOutVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

/**
 * @Author wudi
 * @Date 2024/3/7 14:03
 * @desc
 */
@Data
public class ChoiceSaveInVo {
    @ApiModelProperty("专题id")
    private Long specialId;

    @ApiModelProperty(value = "图像ID", required = true)
    private List<ImageListOutVO> images;



}
