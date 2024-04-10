package cn.staitech.fr.domain;

import cn.staitech.common.core.web.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

/**
 * 图像表 tb_image
 *
 * @author WangFeng
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "tb_image")
public class Image extends BaseEntity {
    @TableId(value = "image_id", type = IdType.AUTO)
    @ApiModelProperty(value = "图像id", hidden = true)
    private Long imageId;
    @TableField(value = "file_name")
    @ApiModelProperty(value = "无扩展名文件名称")
    private String fileName;
    @TableField(value = "image_name")
    @ApiModelProperty(value = "图像名称-文件名称（文件名）", hidden = true)
    private String imageName;
    @TableField(value = "image_url")
    @ApiModelProperty(value = "图像url地址", hidden = true)
    private String imageUrl;
    @TableField(value = "image_path")
    @ApiModelProperty(value = "图片绝对路径", hidden = true)
    private String imagePath;
    @TableField(value = "thumb_url")
    @ApiModelProperty(value = "缩略图url地址", hidden = true)
    private String thumbUrl;
    @TableField(value = "macro_url")
    @ApiModelProperty(value = "macro图片URL地址", hidden = true)
    private String macroUrl;
    @TableField(value = "label_url")
    @ApiModelProperty(value = "label图片URL地址", hidden = true)
    private String labelUrl;
    @TableField(value = "format")
    @ApiModelProperty(value = "文件格式", hidden = true)
    private String format;
    @TableField(value = "width")
    @ApiModelProperty(value = "宽度", hidden = true)
    private String width;
    @TableField(value = "height")
    @ApiModelProperty(value = "高度", hidden = true)
    private String height;
    @TableField(value = "depth")
    @ApiModelProperty(value = "深度", hidden = true)
    private String depth;
    @TableField(value = "size")
    @ApiModelProperty(value = "大小", hidden = true)
    private String size;
    @TableField(value = "global_size")
    @ApiModelProperty(value = "大小", hidden = true)
    private String globalSize;
    @TableField(value = "resolving_power")
    @ApiModelProperty(value = "分辨率", hidden = true)
    private String resolvingPower;
    @TableField(value = "tile_count_list")
    @ApiModelProperty(value = "每层的切片个数", hidden = true)
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
    @TableField(value = "resolution_x")
    @ApiModelProperty(value = "x轴分辨率", hidden = true)
    private String resolutionX;
    @TableField(value = "resolution_y")
    @ApiModelProperty(value = "y轴分辨率", hidden = true)
    private String resolutionY;
    @TableField(value = "source_lens")
    @ApiModelProperty(value = "原放大倍数", hidden = true)
    private Integer sourceLens;
    @TableField(value = "create_by")
    @ApiModelProperty(value = "创建人", hidden = true)
    private Long createBy;
    @TableField(value = "create_time")
    @ApiModelProperty(value = "上传时间 - 创建时间", hidden = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @TableField(value = "update_by")
    @ApiModelProperty(value = "", hidden = true)
    private Long updateBy;
    @TableField(value = "update_time")
    @ApiModelProperty(value = "", hidden = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
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
