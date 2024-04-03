package cn.staitech.fr.domain.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * 
* @ClassName: AlgorithmImageOut
* @Description:
* @author wanglibei
* @date 2024年4月1日
* @version V1.0
 */
@Data
public class AlgorithmImageOut {
    @ApiModelProperty(value = "切片ID")
    private Long slideId;
    @ApiModelProperty(value = "imageId")
    private Long imageId;
    @ApiModelProperty(value = "图像url地址")
    private String imageUrl;
//    @ApiModelProperty(value = "图片绝对路径")
//    private String imagePath;
}
