package cn.staitech.fr.vo;

import cn.staitech.common.core.domain.DateRangeReq;
import cn.staitech.common.core.domain.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OrganTagPageReq extends PageRequest {

    @ApiModelProperty(value = "种属Id")
    private String speciesId;

    @ApiModelProperty(value = "脏器名称")
    private String organName;

    @ApiModelProperty(value = "标签简称")
    private String abbreviation;

    @ApiModelProperty(value = "创建时间范围查询条件")
    private DateRangeReq createTime;
}
