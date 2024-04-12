package cn.staitech.fr.domain.out;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExportListVO {


    @ApiModelProperty(value = "部位名称")
    private String positionName;

    @ApiModelProperty(value = "病理病变名称")
    private String lesionName;

    @ApiModelProperty(value = "修饰名称")
    private String ddefinitionName;

    @ApiModelProperty(value = "病变级别名称")
    private String gradeName;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建人id")
    private String createBy;

    @ApiModelProperty(value = "创建时间")
    private String createTime;
 
}