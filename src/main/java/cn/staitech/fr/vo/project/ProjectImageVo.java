package cn.staitech.fr.vo.project;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;


@Data
public class ProjectImageVo {
    @ApiModelProperty("项目id")
    @NotNull(message = "{StartPredictionIn.specialId.isnull}")
    private Long projectId;

    @ApiModelProperty(value = "图像ID", required = true)
    @NotEmpty(message = "{PICTURE_NON_CHOOSE}")
    private List<ImageVO> images;



}
