package cn.staitech.fr.domain.out;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author wmy
 * @version 1.0
 * @date 2024/4/2 9:39
 * @description
 */
@Data
public class OrganDisassemblyOut {
    @ApiModelProperty(value = "单切片id")
    private Long singleId;
    @ApiModelProperty(value = "切片id")
    private Long slideId;

    @ApiModelProperty(value = "单脏器缩略图url地址")
    private String imageUrl;

    @ApiModelProperty(value = "切片编号")
    private String fileName;

    @ApiModelProperty(value = "组别号")
    private String groupCode;

    @ApiModelProperty(value = "蜡块编号")
    private String waxCode;

    @ApiModelProperty(value = "动物编号")
    private String animalCode;

    @ApiModelProperty(value = "性别（M:雄；F:雌）")
    private String genderFlag;

    @ApiModelProperty(value = "启动者")
    private String initiateBy;

    @ApiModelProperty(value = "启动时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date initiateTime;
    @ApiModelProperty(value = "单脏器类型")
    private Long categoryId;
    @ApiModelProperty(value = "单脏器类型")
    private String organName;
    @ApiModelProperty(value = "脏器数量")
    private Long organNumber;
}
