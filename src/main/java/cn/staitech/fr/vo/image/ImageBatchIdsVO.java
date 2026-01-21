package cn.staitech.fr.vo.image;

import cn.staitech.sft.logaudit.annotation.IgnoreLogField;
import cn.staitech.sft.logaudit.req.LogAuditBaseReq;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author wangf
 */
@Data
public class ImageBatchIdsVO extends LogAuditBaseReq<ImageBatchIdsVO> {
    /**
     * 图像id
     */
    @IgnoreLogField
    @ApiModelProperty(value = "图像ID", required = true)
    private List<Long> imageIdList;

    @ApiModelProperty(value = "日志记录", required = true)
    private List<ImageLogDel> objList;
}
