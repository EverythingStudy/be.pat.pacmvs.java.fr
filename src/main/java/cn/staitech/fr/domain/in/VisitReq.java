package cn.staitech.fr.domain.in;

import cn.staitech.sft.logaudit.annotation.IgnoreLogField;
import cn.staitech.sft.logaudit.req.LogAuditBaseReq;
import lombok.Data;

@Data
public class VisitReq extends LogAuditBaseReq {

    @IgnoreLogField
    private Long slideId;

}
