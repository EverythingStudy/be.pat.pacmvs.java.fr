package cn.staitech.fr.vo.image;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 图像状态
 *
 * @author wangfeng
 */
@Data
public class ImageStatusVo {
    @ApiModelProperty(value = "状态 - key")
    private Integer key;
    @ApiModelProperty(value = "状态 - value")
    private String value;

    public ImageStatusVo(Integer key, String value) {
        this.key = key;
        this.value = value;
    }
}
