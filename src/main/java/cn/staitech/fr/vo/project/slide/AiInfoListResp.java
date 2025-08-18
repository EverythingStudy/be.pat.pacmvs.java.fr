package cn.staitech.fr.vo.project.slide;

import cn.staitech.common.core.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class AiInfoListResp {

    @ApiModelProperty(value = "脏器名称")
    @Excel(name = "脏器名称")
    private String organName;

    @ApiModelProperty(value = "指标计算机构编码")
    private Set<String> structCode;

    @ApiModelProperty(value = "脏器下量化指标")
    private List<AiInfoListVO> aiInfoList;
}
