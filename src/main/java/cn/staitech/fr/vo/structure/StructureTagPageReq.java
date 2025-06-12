package cn.staitech.fr.vo.structure;

import cn.staitech.common.core.domain.DateRangeReq;
import cn.staitech.common.core.domain.PageRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author mugw
 * @version 1.0
 * @description
 * @date 2025/6/3 15:18:03
 */
@Data
public class StructureTagPageReq extends PageRequest {

    @ApiModelProperty(value = "病理指标id")
    @JsonProperty("indicatorId")
    private Long structureTagSetId;
    @ApiModelProperty(value = "标签名称")
    @JsonProperty("categoryName")
    private String structureTagName;
    @ApiModelProperty(value = "结构ID")
    private String structureId;
    @ApiModelProperty(value = "结构名称")
    private String structureName;
    @ApiModelProperty("请求参数（开始和结束时间）")
    private DateRangeReq createTimeParams;
    @ApiModelProperty(value = "机构id", hidden = true)
    private Long organizationId;
    @ApiModelProperty(value = "结构标签id集合", hidden = true)
    private List<Long> structureTagIds;

}
