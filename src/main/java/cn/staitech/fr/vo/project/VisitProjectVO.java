package cn.staitech.fr.vo.project;

import cn.staitech.sft.logaudit.annotation.IgnoreLogField;
import cn.staitech.sft.logaudit.req.LogAuditBaseReq;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 访问项目日志
 *
 * @author admin
 * @version 1.0
 * @since 2025/12/30
 */
@Data
@ApiModel("访问项目日志")
public class VisitProjectVO extends LogAuditBaseReq {
    @IgnoreLogField
    private Long projectId;
}
