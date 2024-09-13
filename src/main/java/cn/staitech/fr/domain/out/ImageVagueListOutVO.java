package cn.staitech.fr.domain.out;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;


/**
 * ImageVagueListOutVO
 */
@Data
public class ImageVagueListOutVO {
    @ApiModelProperty(value = "切片ID（原图像ID）")
    private Long imageId;
    @ApiModelProperty(value = "图像预览")
    private String thumbUrl;
    @ApiModelProperty(value = "图片（切片）编号")
    private String imageCode;
    @ApiModelProperty(value = "专题ID")
    private Long topicId;
    @ApiModelProperty(value = "专题名称")
    private String topicName;
    @ApiModelProperty(value = "文件大小")
    private String size;
    @ApiModelProperty(value = "机构编号")
    private Long organizationId;
    @ApiModelProperty(value = "机构名称")
    private String organizationName;
    @ApiModelProperty(value = "创建时间/上传时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
