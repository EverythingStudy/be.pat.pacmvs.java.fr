package cn.staitech.fr.domain.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author wudi
 * @Date 2024/3/29 17:39
 * @desc
 */
@Data
public class SpecialRecyclingRecoverIn {
    @ApiModelProperty(value = "专题回收站id")
    private Long recyclingId;
    @ApiModelProperty(value = "恢复传0；彻底删除传1")
    private String opcode;
}
