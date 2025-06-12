package cn.staitech.fr.vo.project;

import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author mugw
 * @version 2.6.0
 * @description
 * @date 2025/5/14 13:44:14
 */
@Data
public class ProjectPageVo {
    @ApiModelProperty(value = "项目id")
    private Long projectId;

    @ApiModelProperty("项目编号")
    private String topicName;

    @ApiModelProperty(value = "项目名称")
    private String projectName;

    @ApiModelProperty(value = "种属")
    private String speciesName;

    @ApiModelProperty(value = "种属英文")
    private String speciesNameEn;

    @ApiModelProperty(value = "试验类型")
    private Integer trialId;

    @ApiModelProperty(value = "试验类型")
    private String trialType;

    @ApiModelProperty(value = "试验类型英文描述")
    private String trialTypeEn;

    @ApiModelProperty(value = "染色类型id")
    private String colorType;

    @ApiModelProperty(value = "染色类型名称")
    private String colorName;

    @ApiModelProperty(value = "染色类型英文名称")
    private String colorNameEn;

    @ApiModelProperty(value = "状态(0-待启动，1-进行中，2-暂停，3-已完成，6-归档)")
    private Integer status;

    @ApiModelProperty(value = "创建者")
    private String createName;
    @ApiModelProperty(value = "创建者ID")
    private Long createBy;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty(value = "项目负责人")
    private Long principal;

    @ApiModelProperty(value = "项目负责人名称")
    private String principalName;

    @ApiModelProperty(value = "回收时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date recoveryTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "到期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    public Date getExpireTime() {
        DateUtil.offsetDay(updateTime,30);
        return updateTime;
    }

    @ApiModelProperty(value = "操作按钮")
    private List<String> buttons;

}
