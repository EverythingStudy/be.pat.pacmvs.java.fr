package cn.staitech.fr.vo.project.slide;

import cn.staitech.common.core.domain.DateRangeReq;
import cn.staitech.common.core.domain.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author wudi
 * @Date 2024/4/1 11:03
 * @desc
 */
@Data
public class SlidePageReq extends PageRequest {

    @ApiModelProperty(value = "项目id")
    private Long projectId;

    @ApiModelProperty(value = "切片编号")
    private String fileName;

    @ApiModelProperty(value = "动物编号")
    private String animalCode;

    @ApiModelProperty(value = "组号")
    private String groupCode;

    @ApiModelProperty(value = "性别")
    private String genderFlag;

    @ApiModelProperty(value = "蜡块编号")
    private String waxCode;

    @ApiModelProperty(value = "状态")
    private String processFlag;

    @ApiModelProperty(value = "切片名称解析，0：成功；1：失败")
    private String analyzeStatus;

    @ApiModelProperty(value = "添加人")
    private String createBy;

    @ApiModelProperty(value = "切片id")
    private Long slideId;

    @ApiModelProperty(value = "添加时间")
    private DateRangeReq createTimeParams;
    
    @ApiModelProperty(value = "排序字段")
    private String sortField;
    
    @ApiModelProperty(value = "排序类型")
    private String sortType;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "是否已阅")
    private Boolean isView;

    @ApiModelProperty(value = "当前登录人")
    private String currentUserId;

}
