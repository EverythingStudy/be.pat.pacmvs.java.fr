package cn.staitech.fr.vo.project.slide;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author yxy
 */
@Data
public class SlideListReq {
    /**
     * 项目ID
     */
    @NotNull(message = "项目ID不能为空")
    @ApiModelProperty(value = "项目ID", required = true)
    private Long projectId;
    /**
     * 切片ID集合
     */
    @ApiModelProperty(value = "切片ID集合")
    private List<Long> slideIds;
    /**
     * 脏器标签ID集合-搜索条件数据传递
     */
    @ApiModelProperty(value = "脏器标签ID集合-搜索条件数据传递")
    private List<Long> organTagIds;
    /**
     * 当前登录人
     */
    @ApiModelProperty(value = "当前登录人", hidden = true)
    private String currentUserId;
}
