package cn.staitech.fr.domain.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author wudi
 * @Date 2024/3/29 16:22
 * @desc
 */
@Data
public class EditSpecialStatusIn {
    @ApiModelProperty(required = true, value = "专题id")
    private Long specialId;
    /**
     * 以下属性暂未使用
     */
    @ApiModelProperty( value = "状态:启动（取消完成）传1，暂停传2，完成传3，锁定传4")
    private Integer status;
}
