package cn.staitech.fr.domain.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author wudi
 * @Date 2024/3/29 9:42
 * @desc
 */
@Data
public class UploadWaxBlockIn {

    @ApiModelProperty("文件数据")
    private MultipartFile file;

    @ApiModelProperty("机构id")
    private Long organizationId;

    @ApiModelProperty("专题id")
    @NotNull(message = "{StartPredictionIn.specialId.isnull}")
    private Long topicId;

    @ApiModelProperty("专题名称")
    private String topicName;

    @ApiModelProperty("种属id")
    @NotBlank(message = "{SpecialInsertVo.species.isnull}")
    private String speciesId;

    @ApiModelProperty("种属名称")
    private String speciesName;

}
