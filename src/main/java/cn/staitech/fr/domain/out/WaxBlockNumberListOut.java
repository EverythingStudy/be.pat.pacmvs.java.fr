package cn.staitech.fr.domain.out;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Author wudi
 * @Date 2024/3/28 16:32
 * @desc
 */
@Data
public class WaxBlockNumberListOut {

    @ApiModelProperty(value = "主键")
    private Long numberId;

    @ApiModelProperty(value = "种属名称")
    private String speciesName;

    @ApiModelProperty(value = "专题名称")
    private String topicName;

    @ApiModelProperty(value = "文件名称")
    private String fileName;

    @ApiModelProperty(value = "机构名称")
    private String organizationName;

    @ApiModelProperty(value = "机构id")
    private Long organizationId;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
