package cn.staitech.fr.vo.project;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * AI分析
 *
 * @author yxy
 */
@Data
public class AiAnalysisReq implements Serializable {
    /**
     * 项目ID
     */
    @NotNull(message = "项目ID不能为空")
    @ApiModelProperty(value = "项目ID", required = true)
    private Long projectId;
}
