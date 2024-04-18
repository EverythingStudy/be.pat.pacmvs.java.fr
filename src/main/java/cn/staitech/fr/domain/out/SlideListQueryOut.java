package cn.staitech.fr.domain.out;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Author wudi
 * @Date 2024/4/1 11:03
 * @desc
 */
@Data
public class SlideListQueryOut {

    @ApiModelProperty(value = "切片id")
    private Long slideId;

    @ApiModelProperty(value = "缩略图url地址")
    private String thumbUrl;

    @ApiModelProperty(value = "切片编号")
    private String fileName;

    @ApiModelProperty(value = "组别号")
    private String groupCode;
    
    @ApiModelProperty(value = "蜡块编号")
    private  String waxCode;

    @ApiModelProperty(value="动物编号")
    private String animalCode;

    @ApiModelProperty(value="性别（M:雄；F:雌）")
    private String genderFlag;

    @ApiModelProperty(value = "添加人")
    private String createUser;

    @ApiModelProperty(value = "添加时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty(value = "处理状态（0：待切图,1：切图中,2：切图成功 3：切图失败）")
    private Integer processFlag;

    @ApiModelProperty(value = "脏器类型")
    private String organs;

    @ApiModelProperty(value = "切片名称解析，0：成功；1：失败")
    private String analyzeStatus;

}
