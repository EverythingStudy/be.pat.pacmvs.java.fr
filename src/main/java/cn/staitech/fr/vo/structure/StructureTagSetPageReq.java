package cn.staitech.fr.vo.structure;

import cn.staitech.common.core.domain.DateRangeReq;
import cn.staitech.common.core.domain.PageRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class StructureTagSetPageReq extends PageRequest {

    /**
     * 物种名称
     */
    private String speciesName;

    /**
     * 物种ID
     */
    private String speciesId;

    /**
     * 器官ID
     */
    @JsonProperty("organId")
    private String organCode;

    /**
     * 创建时间范围查询条件
     */
    private DateRangeReq createTimeParams;

    @ApiModelProperty(value = "机构id", hidden = true)
    private Long organizationId;

}
