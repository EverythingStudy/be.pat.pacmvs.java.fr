package cn.staitech.fr.domain.history;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author: wangfeng
 * @create: 2024-02-21 18:10:43
 * @Description:
 */
@Data
public class HistoryDTO {
    @ApiModelProperty(value = "业务类型：1 标注,2 考试")
    private Integer bizType;

    @ApiModelProperty(value = "事件类型：1 undo,2 redo")
    private Integer envType;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "切片ID")
    private Long slideId;

    @ApiModelProperty(value = "切片ID")
    private Long singleSlideId;
}
