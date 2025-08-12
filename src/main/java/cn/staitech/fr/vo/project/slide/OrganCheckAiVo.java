package cn.staitech.fr.vo.project.slide;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 脏器识别校对AI数据
 *
 * @author yxy
 */
@Data
public class OrganCheckAiVo implements Serializable {
    /**
     * 单脏器切片ID
     */
    @ApiModelProperty(value = "单脏器切片ID", hidden = true)
    private Long singleId;
    /**
     * 脏器标签ID
     */
    @ApiModelProperty(value = "脏器标签ID")
    private Long organTagId;
    /**
     * 脏器名称
     */
    @ApiModelProperty(value = "脏器名称")
    private String organName;
    /**
     * 脏器英文名称
     */
    @ApiModelProperty(value = "脏器英文名称")
    private String organEn;
    /**
     * rgb值
     */
    @ApiModelProperty(value = "rgb值")
    private String rgb;
    /**
     * 色值
     */
    @ApiModelProperty(value = "色值")
    private String chromaticValue;
    /**
     * 是否红色底纹高亮整行
     */
    @ApiModelProperty(value = "是否红色底纹高亮整行")
    private Boolean redHighlight;
}
