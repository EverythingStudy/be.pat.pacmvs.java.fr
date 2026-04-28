package cn.staitech.fr.vo.project;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 脏器识别校对-view页面数据
 *
 * @author yxy
 */
@Data
public class OrganCheckViewReq implements Serializable {
    /**
     * 切片ID
     */
    @NotNull(message = "切片ID不能为空")
    @ApiModelProperty(value = "切片ID", required = true)
    private Long slideId;
}
