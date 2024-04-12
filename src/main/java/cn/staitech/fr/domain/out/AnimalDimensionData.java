package cn.staitech.fr.domain.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author wudi
 * @Date 2024/4/11 16:56
 * @desc
 */
@Data
public class AnimalDimensionData {

    @ApiModelProperty(value = "分组")
    private String groupCode;

    @ApiModelProperty(value = "动物编号")
    private String animalCode;

    @ApiModelProperty(value = "性别（M:雄；F:雌）")
    private String genderFlag;

    @ApiModelProperty(value = "脏器信息列表")
    private List<OrgansData> organs;
}
