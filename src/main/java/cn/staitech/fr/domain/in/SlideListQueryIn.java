package cn.staitech.fr.domain.in;

import cn.staitech.common.core.domain.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * @Author wudi
 * @Date 2024/4/1 11:03
 * @desc
 */
@Data
public class SlideListQueryIn extends PageRequest {

    @ApiModelProperty(value = "专题id")
    private Long specialId;

    @ApiModelProperty(value = "切片编号")
    private String fileName;

    @ApiModelProperty(value = "状态")
    private String processFlag;

    @ApiModelProperty(value = "切片名称解析，0：成功；1：失败")
    private String analyzeStatus;

    @ApiModelProperty(value = "添加人")
    private String createBy;

    @ApiModelProperty(value = "添加时间")
    private Map<String, Date> createTimeParams;

}
