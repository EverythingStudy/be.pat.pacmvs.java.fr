package cn.staitech.fr.vo.structure;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

@Data
public class StructureTagPageVo {

    @ApiModelProperty(value = "脏器编号")
    @JsonProperty("organId")
    private String organCode;
    @ApiModelProperty(value = "中文结构名称")
    private String structureName;
    @ApiModelProperty(value = "英文结构名称")
    private String structureNameEn;
    @ApiModelProperty(value = "标签名称")
    private String structureTagName;
    @ApiModelProperty(value = "颜色名称")
    private String color;
    @ApiModelProperty(value = "颜色值rgb")
    private String rgb;
    @ApiModelProperty(value = "颜色值HEX")
    private String hex;
    @ApiModelProperty(value = "标签编号")
    private String structureId;
    private String number;
    @ApiModelProperty(value = "图层顺序")
    private Integer orderNumber;
    @ApiModelProperty(value = "创建者名字")
    private String userName;
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @ApiModelProperty(value = "1-RO：结构类型  2-ROA:标注区域 3-ROE:考核区域")
    private String typeNumber;
    @JsonProperty("categoryId")
    private Long structureTagId;
}
