package cn.staitech.fr.vo.project;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 种属蜡块模板信息
 *
 * @author yxy
 */
@Data
public class OrganVO {
    /**
     * 主键
     */
    @ApiModelProperty(value = "种属蜡块模板ID")
    private Long templateId;
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
}
