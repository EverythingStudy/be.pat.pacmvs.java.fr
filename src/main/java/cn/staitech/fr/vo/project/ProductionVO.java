package cn.staitech.fr.vo.project;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 制片信息
 *
 * @author yxy
 */
@Data
public class ProductionVO {
    /**
     * 种属蜡块模板表ID
     */
    @ApiModelProperty(value = "种属蜡块模板表ID：与下拉列表配合使用")
    private Long waxCodeId;
    /**
     * 种属ID
     */
    @ApiModelProperty(value = "种属ID", hidden = true)
    private String speciesId;
    /**
     * 蜡块编号
     */
    @ApiModelProperty(value = "蜡块编号")
    private String waxCode;
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
     * 取材块数
     */
    @ApiModelProperty(value = "取材块数")
    private Integer blockCount;
    /**
     * 性别（M：男性；F：女性；N：不区分性别）
     */
    @ApiModelProperty(value = "性别（M：男性；F：女性；N：不区分性别）")
    private String sexFlag;
}
