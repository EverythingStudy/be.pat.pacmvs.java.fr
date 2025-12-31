package cn.staitech.fr.vo.project.slide;

import cn.staitech.fr.mapper.SysOrganizationMapper;
import cn.staitech.fr.mapper.UserMapper;
import cn.staitech.sft.logaudit.annotation.IgnoreLogField;
import cn.staitech.sft.logaudit.annotation.LogFieldDBConvert;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

import static cn.staitech.fr.constant.LogFieldConvertConstants.ORGANIZATION_NAME;
import static cn.staitech.fr.constant.LogFieldConvertConstants.USER_NAME;

/**
 * 记录日志切片信息
 *
 * @author admin
 * @version 1.0
 * @since 2025/12/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlideInfo {
    @IgnoreLogField
    @ApiModelProperty(value = "原始切片id")
    private Long imageId;

    @ApiModelProperty(value = "切片编号")
    private String imageCode;

    @ApiModelProperty(value = "组别号")
    private String groupCode;

    @ApiModelProperty(value = "蜡块号")
    private String waxCode;

    @ApiModelProperty(value = "动物编号")
    private String animalCode;

    @ApiModelProperty(value = "性别")
    private String sexFlag;

    @LogFieldDBConvert(mapper = UserMapper.class, convertField = USER_NAME)
    @ApiModelProperty(value = "添加人", hidden = true)
    private Long createBy;

    @ApiModelProperty(value = "添加时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
