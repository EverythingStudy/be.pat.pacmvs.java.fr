package cn.staitech.fr.domain.out;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * @Author wudi
 * @Date 2024/4/10 15:52
 * @desc
 */
@Data
public class SelectImageSlideOut {
    @ApiModelProperty(value = "单切片id")
    private Long singleId;

    @ApiModelProperty(value = "单切片id")
    private Long singleSlideId;

    @ApiModelProperty(value = "专题id")
    private Long specialId;

    @ApiModelProperty(value = "图片id")
    private Long imageId;

    @ApiModelProperty(value = "切片id")
    private Long slideId;

    @ApiModelProperty(value = "单脏器缩略图url地址")
    private String thumbUrl;

    @ApiModelProperty(value = "切片编号")
    private String imageName;

    @ApiModelProperty(value = "组别号")
    private String groupCode;

    @ApiModelProperty(value = "蜡块编号")
    private String waxCode;

    @ApiModelProperty(value = "动物编号")
    private String animalCode;

    @ApiModelProperty(value = "性别（M:雄；F:雌）")
    private String genderFlag;

    @ApiModelProperty(value = "启动者")
    private Long initiateBy;
    @ApiModelProperty(value = "启动者")
    private String userName;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty(value = "单脏器类型")
    private Long categoryId;

    @ApiModelProperty(value = "单脏器类型")
    private String organName;

    @ApiModelProperty(value = "单脏器类型-英文")
    private String organEn;

    @ApiModelProperty(value = "脏器数量")
    private Long organNumber;

    @ApiModelProperty(value = "0未预测、1预测成功、2预测失败、3预测中;ai预测字典标识符：ai_forecast_type")
    private String forecastStatus;

    @ApiModelProperty(value = "人工诊断状态 0：未诊断；1：已诊断;字典标识符：diagnosis_status")
    private String diagnosisStatus;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "多脏器缩略图url地址")
    private String imageThumbUrl;

    @ApiModelProperty(value = "多脏器图片地址")
    private String imageUrl;

    @ApiModelProperty(value = "文件格式")
    private String format;

    @TableField(value = "width")
    @ApiModelProperty(value = "宽度")
    private String width;

    @TableField(value = "height")
    @ApiModelProperty(value = "高度")
    private String height;

    @TableField(value = "resolution_x")
    @ApiModelProperty(value = "x轴分辨率")
    private String resolutionX;

    @TableField(value = "resolution_y")
    @ApiModelProperty(value = "y轴分辨率")
    private String resolutionY;

    @TableField(value = "source_lens")
    @ApiModelProperty(value = "原放大倍数")
    private Integer sourceLens;

    @ApiModelProperty(value = "深度")
    private String depth;

    @ApiModelProperty(value = "大小")
    private String size;

    @ApiModelProperty(value = "全局大小")
    private String globalSize;

    @ApiModelProperty(value = "分辨率")
    private String resolvingPower;

    @ApiModelProperty(value = "每层的切片个数")
    private String tileCountList;

    @TableField(value = "level_count")
    @ApiModelProperty(value = "总层数", hidden = true)
    private Integer levelCount;

    @TableField(value = "chunk_total")
    @ApiModelProperty(value = "前端总切片个数", hidden = true)
    private Integer chunkTotal;

    @TableField(value = "md5")
    @ApiModelProperty(value = "图片的Md5值", hidden = true)
    private String md5;

    @TableField(value = "cache_url")
    @ApiModelProperty(value = "1024缩略图路径（用于缓存、标注缩略图时需要）")
    private String cacheUrl;

    @TableField(value = "multiple")
    @ApiModelProperty(value = "原图缩到cache图的倍数")
    private String multiple;

    @TableField(value = "host_id")
    @ApiModelProperty(value = "所在主机ID")
    private Byte hostId;

    @TableField(exist = false)
    @ApiModelProperty(value = "", hidden = true)
    private String remark;

    @TableField(value = "image_code")
    @ApiModelProperty(value = "切片编号")
    private String imageCode;

    @TableField(value = "topic_id")
    @ApiModelProperty(value = "所属专题", hidden = true)
    private Long topicId;

    @TableField(value = "topic_name")
    @ApiModelProperty(value = "所属专题-专题名称")
    private String topicName;

    @TableField(value = "status")
    @ApiModelProperty(value = "文件状态:0上传中、1上传失败、2解析中、3解析失败、4可用")
    private Integer status;

    @ApiModelProperty(value = "机构编号")
    @TableField(value = "organization_id")
    private Long organizationId;

    @TableField(exist = false)
    @ApiModelProperty(value = "创建时间-查询入参")
    private Map<String, Object> createTimeParams;

    @TableField(exist = false)
    @ApiModelProperty(value = "", hidden = true)
    private Map<String, Object> params;

    @ApiModelProperty(value = "机构名称")
    @TableField(exist = false)
    private String organizationName;

    @ApiModelProperty(value = "轮次ID-1到10")
    private Long roundId;

    @ApiModelProperty(value = "轮次名称")
    @TableField(exist = false)
    private String roundName;

    @ApiModelProperty(value = "业务类型:1原始切片（默认）、2预测切片、7拼接图像或单张图像（单张图像folder_id=0）")
    private Integer bizType;

    @ApiModelProperty(value = "业务类型:1原始切片（默认）、2预测切片、7拼接图像或单张图像（单张图像folder_id=0）")
    @TableField(exist = false)
    private String businessTypeName;

    @ApiModelProperty(value = "图像来源、上传方式(1前端上传，2目录选片，3TCP客户端上传)")
    private Integer source;

    @TableField(exist = false)
    @ApiModelProperty(value = "", hidden = true)
    private String searchValue;

    @ApiModelProperty(value = "项目编号")
    @TableField(exist = false)
    private Long projectId;

    @ApiModelProperty(value = "reviewRoundId")
    @TableField(exist = false)
    private Long reviewRoundId;

    @ApiModelProperty(value = "文件夹编号")
    @TableField(value = "folder_id")
    private Long folderId;

    @ApiModelProperty(value = "创建人")
    @TableField(exist = false)
    private String nickName;

    @ApiModelProperty(value = "文件夹名称")
    @TableField(exist = false)
    private String folderName;





}
