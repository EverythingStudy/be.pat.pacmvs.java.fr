package cn.staitech.fr.vo.project.slide;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SlideOrganTagVo {

    @ApiModelProperty(value = "脏器标签ID")
    private Long organTagId;

    @ApiModelProperty(value = "种属")
    private String speciesId;

    @ApiModelProperty(value = "脏器名称")
    private String organName;

    @ApiModelProperty( value = "脏器名称")
    private String organEn;

    @ApiModelProperty("脏器标签编码")
    private String organTagCode;

}
