package cn.staitech.fr.vo.project.slide;

import cn.staitech.common.core.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class AiInfoListResp {

    @ApiModelProperty(value = "脏器id")
    private Long organTagId;

    @ApiModelProperty(value = "脏器名称")
    @Excel(name = "脏器名称")
    private String organName;

    @ApiModelProperty(value = "AI分析状态：4-结构未分析、5-结构分析中、6-结构分析完成、7-结构分析失败")
    private Integer aiStatus;

    @ApiModelProperty(value = "指标计算机构编码")
    private List<StructureTagVo> structTagList;

    @ApiModelProperty(value = "脏器下量化指标")
    private List<AiInfoListVO> aiInfoList;
}
