package cn.staitech.fr.vo.project.slide;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author mugw
 * @version 2.6.0
 * @description 切片信息
 * @date 2025/5/14 13:44:14
 */
@Data
public class SlideDetailVo {

    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    @TableField(exist = false)
    private String description;

    /**
     * 图像ID
     */
    @ApiModelProperty(value = "图像ID")
    private Long imageId;

    /**
     * 切片号
     */
    @ApiModelProperty(value = "切片号")
    private String imageName;

    /**
     * 组别
     */
    @ApiModelProperty(value = "组别")
    private String groupName;

    /**
     * 性别
     */
    @ApiModelProperty(value = "性别")
    private String gender;

    /**
     * 创建人id
     */
    @ApiModelProperty(value = "创建人id")
    private Long createBy;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新人id
     */
    @ApiModelProperty(value = "更新人id")
    private Long updateBy;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 项目ID
     */
    @ApiModelProperty(value = "项目ID")
    private Long topicId;

    /**
     * 项目名称
     */
    @ApiModelProperty(value = "项目名称")
    private String topicName;


    private Integer status;

    /**
     * 逻辑删除状态（0删除，1未删除）
     */
    @ApiModelProperty(value = "逻辑删除状态（0删除，1未删除）")
    private Integer deleteFlag;


    /**
     * 图像路径
     */
    @ApiModelProperty(value = "图像路径")
    private String imagePath;

    // =====================================================
    /**
     * 切片ID
     */
    @ApiModelProperty(value = "切片ID")
    @TableField(exist = false, value = "slide_id")
    private Long slideId;

    /**
     * 缩略图url地址
     */
    @TableField(exist = false, value = "thumb_url")
    @ApiModelProperty(value = "缩略图url地址")
    private String thumbUrl;

    @TableField(exist = false, value = "format")
    @ApiModelProperty(value = "文件格式")
    private String format;
    @TableField(exist = false, value = "width")
    @ApiModelProperty(value = "宽度")
    private String width;
    @TableField(exist = false, value = "height")
    @ApiModelProperty(value = "高度")
    private String height;
    @TableField(exist = false, value = "resolution_x")
    @ApiModelProperty(value = "x轴分辨率")
    private String resolutionX;
    @TableField(exist = false, value = "resolution_y")
    @ApiModelProperty(value = "y轴分辨率")
    private String resolutionY;
    @TableField(exist = false, value = "source_lens")
    @ApiModelProperty(value = "原放大倍数")
    private Integer sourceLens;
    @ApiModelProperty(value = "已阅片用户")
    private List<Long> viewers;

}
