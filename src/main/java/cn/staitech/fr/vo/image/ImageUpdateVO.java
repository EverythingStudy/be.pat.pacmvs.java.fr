package cn.staitech.fr.vo.image;

import cn.staitech.fr.mapper.SysOrganizationMapper;
import cn.staitech.sft.logaudit.annotation.IgnoreLogField;
import cn.staitech.sft.logaudit.annotation.LogFieldDBConvert;
import cn.staitech.sft.logaudit.req.LogAuditBaseReq;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.Size;

import static cn.staitech.fr.constant.LogFieldConvertConstants.ORGANIZATION_NAME;


@Data
public class ImageUpdateVO extends LogAuditBaseReq<ImageUpdateVO> {
    @ApiModelProperty(value = "切片编号-图像ID", required = true)
    @IgnoreLogField
    private Long imageId;
    @IgnoreLogField
    @Size(min = 1, max = 100, message = "{ImageTopicVO.imageName.length}")
    @ApiModelProperty(value = "文件名称-无扩展名-切片编号")
    private String fileName;
    @IgnoreLogField
    @Size(min = 1, max = 100, message = "{ImageTopicVO.imageName.length}")
    @ApiModelProperty(value = "文件名称-带扩展名")
    private String imageName;
    @ApiModelProperty(value = "机构编号")
    @LogFieldDBConvert(mapper = SysOrganizationMapper.class, convertField = ORGANIZATION_NAME)
    private Long organizationId;
    @IgnoreLogField
    @ApiModelProperty(value = "所属专题-专题名称", hidden = true)
    private String topicName;
}
