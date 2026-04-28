package cn.staitech.fr.vo.image;

import cn.staitech.common.core.domain.DateRangeReq;
import cn.staitech.common.core.domain.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lif
 */
@Data
public class ImagePageReq extends PageRequest {

    @ApiModelProperty("专题号")
    private String topicName;

    @ApiModelProperty("切片编号")
    private String imageName;

    @ApiModelProperty(value = "机构编号")
    private Long organizationId;

    @ApiModelProperty("上传时间")
    private DateRangeReq createTimeParams;

    @ApiModelProperty("0上传中、1上传失败、2解析中、3解析失败、4可用、5不可用")
    private Integer status;

}