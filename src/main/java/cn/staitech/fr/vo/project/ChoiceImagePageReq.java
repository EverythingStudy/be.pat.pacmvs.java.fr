package cn.staitech.fr.vo.project;

import cn.staitech.common.core.domain.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class ChoiceImagePageReq extends PageRequest {
    @Size(min = 0, max = 200, message = "{ImageTopicVO.imageName.length}")
    @ApiModelProperty(value = "切片编号")
    private String imageName;
    @ApiModelProperty(hidden = true)
    private Long orgId;
    @ApiModelProperty(value = "项目id")
    private Long projectId;

    @ApiModelProperty(value = "添加状态：0未添加、1已添加",hidden = true)
    private Integer choiceState = 0;

}
