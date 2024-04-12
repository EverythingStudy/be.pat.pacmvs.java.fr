package cn.staitech.fr.vo.outline;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * OutlineStatistic
 *
 * @author wangfeng
 * @since 2024-01-04 14:55:03
 */
@Data
public class OutlineSelectVO implements Serializable {

    @ApiModelProperty(value = "项目ID")
    private Long projectId;
    @ApiModelProperty(value = "图像ID")
    private Long imageId;

    @NotNull
    @ApiModelProperty(value = "切片ID")
    private Long slideId;

    @ApiModelProperty(value = "单切片ID")
    private Long singleSlideId;

    @NotNull
    @ApiModelProperty(value = "创建者ID")
    private Long createBy;

    @ApiModelProperty(value = "查询类型：1面积,2周长")
    private Integer bizType;

    @ApiModelProperty(value = "最小面积或周长")
    private Double minVal;

    @ApiModelProperty(value = "最大面积或周长")
    private Double maxVal;

    @ApiModelProperty(value = "标签ID")
    private Long categoryId;
}
