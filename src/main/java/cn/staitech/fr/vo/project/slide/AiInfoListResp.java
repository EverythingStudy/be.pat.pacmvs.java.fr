package cn.staitech.fr.vo.project.slide;

import cn.staitech.common.core.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class AiInfoListResp {

    @ApiModelProperty(value = "脏器名称")
    @Excel(name = "脏器名称")
    private String organName;

    @ApiModelProperty(value = "脏器下量化指标")
    private List<AiInfoListVO> aiInfoList;
}
