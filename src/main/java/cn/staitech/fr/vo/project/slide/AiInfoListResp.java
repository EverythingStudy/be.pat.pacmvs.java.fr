package cn.staitech.fr.vo.project.slide;

import cn.staitech.common.core.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiInfoListResp {

    @ApiModelProperty(value = "脏器id")
    private Long organTagId;

    @ApiModelProperty(value = "脏器名称")
    @Excel(name = "脏器名称")
    private String organName;

    @ApiModelProperty(value = "AI分析状态：4-结构未分析、5-结构分析中、6-结构分析完成、7-结构分析失败")
    private Integer aiStatus;

    @ApiModelProperty(value = "结构化状态 0未预测、1预测成功、2预测失败、3预测中")
    private String forecastStatus;

    @ApiModelProperty(value = "筛差状态：0未分析、1差异分析完成、2差异分析失败、3差异分析中")
    private Long screeningDifferenceStatus;

    @ApiModelProperty(value = "指标计算机构编码")
    private List<StructureTagVo> structTagList = new ArrayList<>();

    @ApiModelProperty(value = "脏器下量化指标")
    private List<AiInfoListVO> aiInfoList;
}
