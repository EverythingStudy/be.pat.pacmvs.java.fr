package cn.staitech.fr.vo.project;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 脏器标签信息
 *
 * @author yxy
 */
@Data
public class OrganVO {
    /**
     * 脏器标签ID
     */
    @ApiModelProperty(value = "脏器标签ID")
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
