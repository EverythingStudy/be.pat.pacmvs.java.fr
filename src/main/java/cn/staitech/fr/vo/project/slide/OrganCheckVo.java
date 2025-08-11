package cn.staitech.fr.vo.project.slide;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 脏器识别校对
 *
 * @author yxy
 */
@Data
public class OrganCheckVo implements Serializable {
    /**
     * 是否校对通过
     */
    @ApiModelProperty(value = "是否校对通过")
    private boolean success;
}
