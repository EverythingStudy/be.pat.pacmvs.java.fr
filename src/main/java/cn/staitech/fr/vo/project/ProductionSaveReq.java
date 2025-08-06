package cn.staitech.fr.vo.project;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 保存制片信息
 *
 * @author yxy
 */
@Data
public class ProductionSaveReq implements Serializable {
    /**
     * 项目ID
     */
    @NotNull(message = "项目ID不能为空")
    @ApiModelProperty(value = "项目ID")
    private Long projectId;
    /**
     * 制片信息
     */
    @ApiModelProperty(value = "制片信息")
    @Valid
    private List<ProductionInfoReq> productions;
}
