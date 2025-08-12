package cn.staitech.fr.vo.project.slide;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 脏器识别校对制片数据
 *
 * @author yxy
 */
@Data
public class OrganCheckProductionVo implements Serializable {
    /**
     * 制片信息ID
     */
    @ApiModelProperty(value = "制片信息ID", hidden = true)
    private Long id;
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
     * 蜡块编号
     */
    @ApiModelProperty(value = "蜡块编号")
    private String waxCode;
    /**
     * 是否红色底纹高亮整行
     */
    @ApiModelProperty(value = "是否红色底纹高亮整行")
    private Boolean redHighlight;
}
