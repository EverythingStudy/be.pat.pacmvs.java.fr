package cn.staitech.fr.vo.outline;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Redis: OUTLINE_ROOT:
 *
 * @author wangfeng
 * @since 2024-01-09 14:55:03
 */
@Data
public class OutlineRoot implements Serializable {
    @ApiModelProperty(value = "项目ID")
    private Long projectId;

    @ApiModelProperty(value = "创建者ID")
    private Long createBy;

    @ApiModelProperty(value = "图像ID")
    private Long imageId;

    @ApiModelProperty(value = "切片ID")
    private Long slideId;

    @ApiModelProperty(value = "token")
    private String token;
}
