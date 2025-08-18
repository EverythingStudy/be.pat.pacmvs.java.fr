package cn.staitech.fr.vo.project.slide;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author yxy
 */
@Data
public class OrganStatusVo {
    /**
     * 单脏器切片id
     */
    @ApiModelProperty(value = "单脏器切片id")
    private Long singleId;
    /**
     * 脏器标签ID
     */
    @ApiModelProperty(value = "脏器标签ID")
    private Long organTagId;
    /**
     * organName
     */
    @ApiModelProperty(value = "脏器名称")
    private String organName;
    /**
     * AI分析状态
     */
    @ApiModelProperty(value = "AI分析状态：4-结构未分析、5-结构分析中、6-结构分析完成、7-结构分析失败-V2.6.1")
    private Integer aiStatus;
    /**
     * 是否指标异常
     */
    @ApiModelProperty(value = "是否指标异常")
    private Boolean abnormalIndicator;
}
