package cn.staitech.fr.domain.in;

import cn.staitech.common.core.domain.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

/**
 * @author wmy
 * @version 1.0
 * @date 2024/4/9 13:09
 * @description
 */
@Data
public class ImageVagueQueryIn extends PageRequest {
    @ApiModelProperty(value = "图片（切片）编号")
    private String imageCode;
    @ApiModelProperty(value = "专题名称")
    private String topicName;
    @ApiModelProperty("机构id")
    private Long organizationId;
    @ApiModelProperty(value = "创建时间-查询入参")
    private Map<String, String> createTimeParams;
}
