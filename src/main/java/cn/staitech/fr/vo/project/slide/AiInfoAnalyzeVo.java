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
    @ApiModelProperty(value = "AI分析状态：0-未分析、1-脏器识别中、2-脏器识别异常、4-结构未分析、5-结构分析中、6-结构分析完成、7-结构分析失败（列表阅片模式下：非0、1、2状态请使用aiInfoList；矩阵阅片模式下：全状态可使用）-V2.6.1")
    private Integer aiStatus;

    @ApiModelProperty(value = "AI分析结果列表")
    private List<AiInfoListResp> aiInfoList;
}
