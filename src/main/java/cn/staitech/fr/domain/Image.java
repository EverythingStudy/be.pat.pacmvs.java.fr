package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_image
 */
@TableName(value ="tb_image")
@Data
public class Image implements Serializable {
    /**
     * 图像ID
     */
    @TableId(value = "image_id", type = IdType.AUTO)
    private Long imageId;

    /**
     * 无扩展名文件名称
     */
    @TableField(value = "file_name")
    private String fileName;

    /**
     * 图像名称（文件名）
     */
    @TableField(value = "image_name")
    private String imageName;

    /**
     * 图像的绝对路径
     */
    @TableField(value = "image_path")
    private String imagePath;

    /**
     * 图像URL地址
     */
    @TableField(value = "image_url")
    private String imageUrl;

    /**
     * 缩略图URL地址
     */
    @TableField(value = "thumb_url")
    private String thumbUrl;

    /**
     * macro图片URL地址
     */
    @TableField(value = "macro_url")
    private String macroUrl;

    /**
     * label图片RUL地址
     */
    @TableField(value = "label_url")
    private String labelUrl;

    /**
     * 1024缩略图路径（用于缓存、标注缩略图时需要）
     */
    @TableField(value = "cache_url")
    private String cacheUrl;

    /**
     * 原图缩到cache图的倍数
     */
    @TableField(value = "multiple")
    private String multiple;

    /**
     * 文件格式
     */
    @TableField(value = "format")
    private String format;

    /**
     * 宽度
     */
    @TableField(value = "width")
    private String width;

    /**
     * 高度
     */
    @TableField(value = "height")
    private String height;

    /**
     * 深度
     */
    @TableField(value = "depth")
    private String depth;

    /**
     * 大小
     */
    @TableField(value = "size")
    private String size;

    /**
     * 全局大小
     */
    @TableField(value = "global_size")
    private String globalSize;

    /**
     * 分辨率
     */
    @TableField(value = "resolving_power")
    private String resolvingPower;

    /**
     * 每层的切片个数
     */
    @TableField(value = "tile_count_list")
    private String tileCountList;

    /**
     * 总层数（小于2则失败）
     */
    @TableField(value = "level_count")
    private Integer levelCount;

    /**
     * 前端总切片个数
     */
    @TableField(value = "chunk_total")
    private Integer chunkTotal;

    /**
     * 图片的MD5值
     */
    @TableField(value = "md5")
    private String md5;

    /**
     * x轴分辨率
     */
    @TableField(value = "resolution_x")
    private String resolutionX;

    /**
     * y轴分辨率
     */
    @TableField(value = "resolution_y")
    private String resolutionY;

    /**
     * 原放大倍数
     */
    @TableField(value = "source_lens")
    private Integer sourceLens;

    /**
     * 创建人id
     */
    @TableField(value = "create_by")
    private Long createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新人id
     */
    @TableField(value = "update_by")
    private Long updateBy;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 图片（切片）编号
     */
    @TableField(value = "image_code")
    private String imageCode;

    /**
     * 专题ID
     */
    @TableField(value = "topic_id")
    private Long topicId;

    /**
     * 专题名称
     */
    @TableField(value = "topic_name")
    private String topicName;

    /**
     * 0上传中、1上传失败、2解析中、3解析失败、4可用 5:不可用
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 所在主机ID
     */
    @TableField(value = "host_id")
    private Integer hostId;

    /**
     * 机构ID
     */
    @TableField(value = "organization_id")
    private Long organizationId;

    /**
     * 轮次ID（现默认1到8）
     */
    @TableField(value = "round_id")
    private Long roundId;

    /**
     * 业务类型（1原始切片（默认）、2预测切片）
     */
    @TableField(value = "biz_type")
    private Integer bizType;

    /**
     * 图像来源(1前端上传，2目录选片，3TCP客户端上传)
     */
    @TableField(value = "source")
    private Integer source;

    /**
     * 总块数
     */
    @TableField(value = "fuzzy_count_chunk")
    private Long fuzzyCountChunk;

    /**
     * 专题号
     */
    @TableField(value = "topic_code")
    private String topicCode;

    /**
     * 动物号
     */
    @TableField(value = "animal_code")
    private String animalCode;

    /**
     * 蜡块号
     */
    @TableField(value = "wax_code")
    private String waxCode;

    /**
     * 组别号
     */
    @TableField(value = "group_code")
    private String groupCode;

    /**
     * 性别（0：M；1：F）
     */
    @TableField(value = "sex_flag")
    private Integer sexFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}