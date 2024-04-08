package cn.staitech.fr.domain.out;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Author wudi
 * @Date 2024/3/29 16:56
 * @desc
 */
@Data
public class SpecialRecyclingListQueryOut {
    @ApiModelProperty(value = "主键id")
    private Long recyclingId;

    @ApiModelProperty("专题编号")
    private String topicName;

    @ApiModelProperty(value = "专题名称")
    private String specialName;

    @ApiModelProperty(value = "种属")
    private String speciesName;

    @ApiModelProperty(value = "种属英文")
    private String speciesNameEn;

    @ApiModelProperty(value = "试验类型")
    private String trialId;

    @ApiModelProperty(value = "染色类型id")
    private String colorType;

    @ApiModelProperty(value = "染色类型名称")
    private String colorName;

    @ApiModelProperty(value = "染色类型英文名称")
    private String colorNameEn;

    @ApiModelProperty(value = "切片数量")
    private Integer slideNum;

    @ApiModelProperty(value = "创建者")
    private String createName;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty(value = "回收时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date recoveryTime;

    @ApiModelProperty(value = "到期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;
}
