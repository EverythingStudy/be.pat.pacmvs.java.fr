package cn.staitech.fr.domain.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class ImageExportVO implements Serializable {
    @ApiModelProperty(value = "图片（切片）编号")
    private String imageCode;
    @ApiModelProperty(value = "专题名称")
    private String topicName;
    @ApiModelProperty(value = "0上传中、1上传失败、2解析中、3解析失败、4可用")
    private Integer status;
    @ApiModelProperty(value = "创建时间-查询入参")
    private Map<String, Object> createTimeParams;
    @ApiModelProperty(value = "机构编号")
    private Long organizationId;
}
