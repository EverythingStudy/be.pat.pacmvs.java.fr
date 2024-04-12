package cn.staitech.fr.domain.out;

import cn.staitech.fr.domain.Category;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author wudi
 * @Date 2024/4/11 16:19
 * @desc
 */
@Data
public class AnimalDimensionOut {

    @ApiModelProperty(value = "脏器列表")
    private List<Category> categoryList;

    @ApiModelProperty(value = "数据列表")
    private List<AnimalDimensionData> dataList;
}
