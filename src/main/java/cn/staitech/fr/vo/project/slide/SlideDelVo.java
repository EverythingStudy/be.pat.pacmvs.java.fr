package cn.staitech.fr.vo.project.slide;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author mugw
 * @version 1.0
 * @description 切片删除对象
 * @date 2025/5/20 09:55:57
 */
@Data
public class SlideDelVo {
    @NotNull(message = "项目id不能为空")
    @ApiModelProperty(value = "项目id")
    private Long projectId;
//    @NotEmpty(message = "切片id集合不能为空")
    @ApiModelProperty(value = "切片id集合")
    private List<Long> slideIds;
}
