package cn.staitech.fr.vo.project.slide;

import java.util.Date;
import java.util.List;

import cn.staitech.fr.domain.Organ;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
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
    /**
     * 切片id
     */
    @ApiModelProperty(value = "切片id-V2.6.1")
    private Long slideId;
    /**
     * 缩略图url地址
     */
    @ApiModelProperty(value = "缩略图url地址-V2.6.1")
    private String thumbUrl;
    /**
     * 切片编号
     */
    @ApiModelProperty(value = "切片编号-V2.6.1")
    private String fileName;
    /**
     * 动物编号
     */
    @ApiModelProperty(value="动物编号-V2.6.1")
    private String animalCode;
    /**
     * 蜡块编号
     */
    @ApiModelProperty(value = "蜡块编号-V2.6.1")
    private String waxCode;
    /**
     * 组别号
     */
    @ApiModelProperty(value = "组别号-V2.6.1")
    private String groupCode;
    /**
     * 性别（M:雄；F:雌）
     */
    @ApiModelProperty(value="性别（M:雄；F:雌）-V2.6.1")
    private String genderFlag;
    /**
     * 阅片状态
     */
    @ApiModelProperty(value = "阅片状态集合：0-未阅片；1-已阅片-V2.6.1")
    private Integer viewStatus;
    /**
     * AI分析状态：0-未分析、1-脏器识别中、2-脏器识别异常、3-脏器识别完成、4-结构未分析、5-结构分析中、6-结构分析完成、7-结构分析失败
     */
    @ApiModelProperty(value = "AI分析状态：0-未分析、1-脏器识别中、2-脏器识别异常、4-结构未分析、5-结构分析中、6-结构分析完成、7-结构分析失败（列表阅片模式下：非0、1、2状态请使用organStatusVos；矩阵阅片模式下：全状态可使用）-V2.6.1")
    private Integer aiStatus;
    /**
     * 脏器信息状态集合
     */
    @ApiModelProperty(value = "脏器信息状态集合（列表阅片模式下：aiStatus非0、1、2状态使用）-V2.6.1")
    List<OrganStatusVo> organStatusVos;
    /**
     * 描述
     */
    @ApiModelProperty(value = "描述-V2.6.1")
    private String description;
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间-V2.6.1")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;





    @ApiModelProperty(value = "添加人")
    private String createUser;

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
