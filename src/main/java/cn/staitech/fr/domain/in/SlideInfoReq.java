package cn.staitech.fr.domain.in;

import cn.staitech.sft.logaudit.req.LogAuditBaseReq;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

@Data
public class SlideInfoReq extends LogAuditBaseReq {

    @ApiModelProperty(value = "标注id")
    private Long slideId;
}
