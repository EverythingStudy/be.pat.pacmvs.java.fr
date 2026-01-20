package cn.staitech.fr.vo.project.slide;

import cn.staitech.fr.mapper.UserMapper;
import cn.staitech.sft.logaudit.annotation.IdField;
import cn.staitech.sft.logaudit.annotation.IgnoreLogField;
import cn.staitech.sft.logaudit.annotation.LogFieldDBConvert;
import cn.staitech.sft.logaudit.req.BatchLogAuditBaseReq;
import cn.staitech.sft.logaudit.req.LogAuditBaseReq;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

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
public class SlideInfoDel extends BatchLogAuditBaseReq {
    @IdField(index = 1,name = "图像系统编号",nameEn = "Image System ID")
    @IgnoreLogField
    @ApiModelProperty(value = "原始切片id")
    private Long imageId;

    @IdField(index = 2,name = "图像项目编号",nameEn = "Image Project ID")
    @IgnoreLogField
    @ApiModelProperty(value = "切片id")
    private Long slideId;

    @IdField(index = 3,name = "图像名称",nameEn = "Image Name")
    @ApiModelProperty(value = "切片编号")
    private String imageCode;
}
