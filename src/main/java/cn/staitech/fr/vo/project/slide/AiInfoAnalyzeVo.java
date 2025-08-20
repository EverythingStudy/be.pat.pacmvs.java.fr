package cn.staitech.fr.vo.project.slide;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 返回指标信息结果
 */
@Data
public class AiInfoAnalyzeVo {

    /**
     * AI分析状态
     */
    @ApiModelProperty(value = "AI分析状态：0-未分析、1-脏器识别中、2-脏器识别异常")
    private Integer aiStatus;

    @ApiModelProperty(value = "AI分析结果列表")
    private List<AiInfoListResp> aiInfoList;
}
