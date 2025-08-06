package cn.staitech.fr.vo.project;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 制片信息
 *
 * @author yxy
 */
@Data
public class ProductionReq implements Serializable {
    /**
     * 机构ID
     */
    @ApiModelProperty(hidden = true)
    private Long organizationId;
    /**
     * 项目ID
     */
    @NotNull(message = "项目ID不能为空")
    @ApiModelProperty(value = "项目ID", required = true)
    private Long projectId;
}
