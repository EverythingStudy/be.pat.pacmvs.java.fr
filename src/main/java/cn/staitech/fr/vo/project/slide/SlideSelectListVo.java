package cn.staitech.fr.vo.project.slide;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class SlideSelectListVo {

    @ApiModelProperty(value = "动物编码列表")
    private List<String> animalCodes;
    @ApiModelProperty(value = "获取蜡块下拉列表")
    private List<String> waxCodes;
    @ApiModelProperty(value = "获取组号下拉列表")
    private List<String> groupCodes;
    @ApiModelProperty(value = "获取脏器下拉列表")
    private List<SlideOrganTagVo> organCodes;
}
