package cn.staitech.fr.vo.project;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 脏器标签信息
 *
 * @author yxy
 */
@Data
public class OrganTagVO {
    /**
     * 脏器标签ID
     */
    @ApiModelProperty(value = "脏器标签ID")
    private Long organTagId;
    /**
     * 种属
     */
    @ApiModelProperty(value = "种属")
    private String speciesId;
    /**
     * 脏器名称
     */
    @ApiModelProperty(value = "脏器名称")
    private String organName;
    /**
     * 英文名称
     */
    @ApiModelProperty(value = "英文名称")
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
}
