package cn.staitech.fr.vo.project.slide;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 脏器识别校对-view页面数据
 *
 * @author yxy
 */
@Data
public class OrganCheckViewVo implements Serializable {
    /**
     * 脏器识别校对AI数据
     */
    @ApiModelProperty(value = "脏器识别校对AI数据")
    private List<OrganCheckAiVo> ais;
    /**
     * 脏器识别校对制片数据
     */
    @ApiModelProperty(value = "脏器识别校对制片数据")
    private List<OrganCheckProductionVo> productions;
}
