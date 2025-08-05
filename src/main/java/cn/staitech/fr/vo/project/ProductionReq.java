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
    @NotNull(message = "{TOPIC_ID_IS_NULL}")
    @ApiModelProperty(value = "项目ID")
    private Long projectId;
}
