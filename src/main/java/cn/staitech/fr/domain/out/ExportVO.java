package cn.staitech.fr.domain.out;

import com.deepoove.poi.data.PictureRenderData;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExportVO {



    @ApiModelProperty(value = "专题名称")
    private String specialName;

    @ApiModelProperty(value = "专题编号")
    private String topicName;

    @ApiModelProperty(value = "切片编号")
    private String  fileName;

    @ApiModelProperty(value = "种属")
    private String  trialType;

    @ApiModelProperty(value = "组织类型")
    private String organName;

    @ApiModelProperty(value = "性别")
    private   String genderFlag;

    @ApiModelProperty(value = "染色类型")
    private String colorType;

    @ApiModelProperty(value = "组别")
    private   String groupCode;

    @ApiModelProperty(value = "切片文件类型")
    private String format;

    //@ApiModelProperty(value = "集合数据")
    private List<ExportListVO> list;
 
   // @ApiModelProperty(value = "表格")
    private List<ExportListVO> table;

 
   // @ApiModelProperty(value = "图片")
    private PictureRenderData img;

    private String thumbUrl;

    private String organizationName;

    private String abnormalStatus;
    
}