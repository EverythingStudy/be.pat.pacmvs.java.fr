package cn.staitech.fr.vo.project.slide;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author mugw
 * @version 2.6.0
 * @description 项目切片管理
 * @date 2025/5/14 13:44:14
 */
@Data
public class SlidePageVo {

    @ApiModelProperty(value = "切片id")
    private Long slideId;

    @ApiModelProperty(value = "缩略图url地址")
    private String thumbUrl;

    @ApiModelProperty(value = "切片编号")
    private String fileName;

    @ApiModelProperty(value = "组别号")
    private String groupCode;
    
    @ApiModelProperty(value = "蜡块编号")
    private String waxCode;

    @ApiModelProperty(value="动物编号")
    private String animalCode;

    @ApiModelProperty(value="性别（M:雄；F:雌）")
    private String genderFlag;

    @ApiModelProperty(value = "添加人")
    private String createUser;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "添加时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String imagePath;

    private String format;

    private String width;

    private String height;

    private String resolutionX;

    private String resolutionY;

    private String sourceLens;

    private String imageId;

    private String imageName;

    @ApiModelProperty(value = "是否已阅")
    private Boolean isView = false;

    @ApiModelProperty(value = "已阅片用户")
    private List<Long> viewers;

}
