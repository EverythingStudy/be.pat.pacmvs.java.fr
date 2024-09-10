package cn.staitech.fr.domain.out;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;


/**
 * ImageListOutVO
 *
 */
@Data
public class ImageListOutVO {
    @ApiModelProperty(value = "切片ID（原图像ID）")
    private Long imageId;
    @ApiModelProperty(value = "无扩展名文件名称")
    private String fileName;
    @ApiModelProperty(value = "文件名称（文件名）")
    private String imageName;
    @ApiModelProperty(value = "图像url地址")
    private String imageUrl;
    @ApiModelProperty(value = "图片绝对路径")
    private String imagePath;
    @ApiModelProperty(value = "图像预览")
    private String thumbUrl;
    @ApiModelProperty(value = "文件格式")
    private String format;
    @ApiModelProperty(value = "宽度")
    private String width;
    @ApiModelProperty(value = "高度")
    private String height;
    @ApiModelProperty(value = "文件大小")
    private String size;
    @ApiModelProperty(value = "全局大小")
    private String globalSize;
    @ApiModelProperty(value = "原放大倍数")
    private Integer sourceLens;
    @ApiModelProperty(value = "文件状态:0上传中、1上传失败、2解析中、3解析失败、4可用")
    private Integer status;
    @ApiModelProperty(value = "文件状态:0上传中、1上传失败、2解析中、3解析失败、4可用")
    private String fileStatus;
    @ApiModelProperty(value = "创建人id")
    private Long createBy;
    @ApiModelProperty(value = "创建时间/上传时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @ApiModelProperty(value = "更新人id")
    private Long updateBy;
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    @ApiModelProperty(value = "图片（切片）编号")
    private String imageCode;
    @ApiModelProperty(value = "专题ID")
    private Long topicId;
    @ApiModelProperty(value = "专题名称")
    private String topicName;
    @ApiModelProperty(value = "机构编号")
    private Long organizationId;
    @ApiModelProperty(value = "机构名称")
    private String organizationName;
    @ApiModelProperty(value = "轮次ID-1到10")
    private Long roundId;
    @ApiModelProperty(value = "轮次名称")
    private String roundName;
    @ApiModelProperty(value = "业务类型:1原始切片（默认）、2预测切片")
    private Integer businessType;
    @ApiModelProperty(value = "业务类型名称:1原始切片（默认）、2预测切片")
    private String businessTypeName;
    @ApiModelProperty(value = "图像来源(1前端上传，2目录选片，3TCP客户端上传)")
    private Integer source;
    @ApiModelProperty(value = "是否允许删除： 1 禁止选中")
    private Integer deleState;
    @ApiModelProperty(value = "添加状态：0未添加、1已添加")
    private Integer choiceState;
    @ApiModelProperty(value = "文件夹ID")
    private Long folderId;
    @ApiModelProperty(value = "创建人")
    private String nickName;
    @ApiModelProperty(value = "文件夹名称-眼科新添加字段")
    private String folderName;
}
