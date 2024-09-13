package cn.staitech.fr.vo.annotation.in;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RoiIn {

    @ApiModelProperty(value = "标注坐标列表", required = true)
    private List<JSONObject> geometryList;

    @NotNull(message = "{MarkingJsonIn.status.notNull}")
    @ApiModelProperty(value = "0包含，1删除", required = true)
    private Integer roiStatus;

    @ApiModelProperty(value = "切片ID", required = true)
    @NotNull(message = "{SlidePredictionIn.slideId.isnull}")
    private Long slideId;


    @ApiModelProperty(value = "标签id列表")
    private List<Long> categoryIds;

    private Long sequenceNumber;

    @ApiModelProperty(value = "标签id", hidden = true)
    private Long categoryId;
    @ApiModelProperty(value = "创建者id", hidden = true)
    private Long createBy;

    @ApiModelProperty(value = "标注列表", hidden = true)
    private List<Long> annotationIdList;

}
