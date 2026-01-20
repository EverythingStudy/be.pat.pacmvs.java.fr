package cn.staitech.fr.vo.image;


import cn.staitech.sft.logaudit.annotation.IdField;
import cn.staitech.sft.logaudit.req.BatchLogAuditBaseReq;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 图像响应类
 *
 * @author
 * @version 1.0
 * @date
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageLogDel extends BatchLogAuditBaseReq {

    @IdField
    @ApiModelProperty(value = "图像id", hidden = true)
    private Long imageId;

}
