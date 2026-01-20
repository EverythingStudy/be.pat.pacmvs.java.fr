package cn.staitech.fr.vo.project;

import cn.staitech.fr.vo.project.slide.SlideInfo;
import cn.staitech.fr.vo.project.slide.SlideInfoDel;
import cn.staitech.sft.logaudit.annotation.IgnoreLogField;
import cn.staitech.sft.logaudit.req.LogAuditBaseReq;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;


@Data
public class ProjectImageVo extends LogAuditBaseReq {
    @IgnoreLogField
    @ApiModelProperty("项目id")
    @NotNull(message = "{StartPredictionIn.specialId.isnull}")
    private Long projectId;

    @ApiModelProperty(value = "图像ID", required = true)
//    @NotEmpty(message = "{PICTURE_NON_CHOOSE}")
    private List<SlideInfo> slideInfos;
    private List<SlideInfoDel> objList;



}
