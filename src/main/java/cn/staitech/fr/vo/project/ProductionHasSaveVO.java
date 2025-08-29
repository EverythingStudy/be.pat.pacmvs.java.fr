package cn.staitech.fr.vo.project;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 制片信息是否保存过
 *
 * @author yxy
 */
@Data
public class ProductionHasSaveVO {
    /**
     * 制片信息是否保存过
     */
    @ApiModelProperty(value = "制片信息是否保存过")
    private Boolean hasSave;
}
