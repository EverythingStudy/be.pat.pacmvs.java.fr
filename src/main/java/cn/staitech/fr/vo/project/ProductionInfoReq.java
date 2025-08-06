package cn.staitech.fr.vo.project;

import cn.staitech.fr.utils.validator.StringItemInAnnotation;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 保存制片信息
 *
 * @author yxy
 */
@Data
public class ProductionInfoReq implements Serializable {
    /**
     * 蜡块编号
     */
    @ApiModelProperty(value = "蜡块编号")
    private String waxCode;
    /**
     * 种属蜡块模板ID
     */
    @ApiModelProperty(value = "种属蜡块模板ID：种属脏器下拉列表（取自种属蜡块模板数据）接口中返回")
    private Long templateId;
    /**
     * 取材块数
     */
    @ApiModelProperty(value = "取材块数")
    private Integer blockCount;
    /**
     * 性别（M：男性；F：女性；N：不区分性别）
     */
    @ApiModelProperty(value = "性别（M：男性；F：女性；N：不区分性别）")
    @StringItemInAnnotation(allowedValues = {"M", "F", "N"})
    private String sexFlag;
}
