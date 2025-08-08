package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 项目表
 * </p>
 *
 * @author author
 * @since 2024-03-29
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("fr_special")
@ApiModel(value="Special对象", description="项目表")
public class Project implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键id")
    @TableId(value = "special_id", type = IdType.AUTO)
    private Long projectId;

    @ApiModelProperty(value = "项目id")
    private Long topicId;

    @ApiModelProperty(value = "项目编号")
    private String topicName;

    @TableField(value = "special_name")
    @ApiModelProperty(value = "项目名称")
    private String projectName;

    @ApiModelProperty(value = "种属id")
    private String speciesId;

    @ApiModelProperty(value = "试验类型")
    private Integer trialId;

    @ApiModelProperty(value = "染色类型")
    private Integer colorType;

    @ApiModelProperty(value = "病理指标id")
    private Integer indicatorId;

    @ApiModelProperty(value = "状态(0-待启动，1-进行中，2-暂停，3-已完成，6-归档)")
    private Integer status;

    @ApiModelProperty(value = "删除标志(0:正常，1:删除)")
    private String delFlag;

    @ApiModelProperty(value = "机构id")

    private Long organizationId;

    @ApiModelProperty(value = "对照组")
    private String controlGroup;

    @ApiModelProperty(value = "项目负责人")
    private Long principal;

    @ApiModelProperty(value = "创建者")
    private Long createBy;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新者")
    private Long updateBy;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "是否永久删除")
    private Boolean isPermanentDel;

    @TableField(exist = false)
    @ApiModelProperty(value = "操作按钮")
    private List<String> buttons;

    @TableField(exist = false)
    @ApiModelProperty(value = "种属名称:制片信息使用")
    private String speciesName;

    @TableField(exist = false)
    @ApiModelProperty(value = "是否启动过AI分析:制片信息使用")
    private Boolean isAiTrained;

    @TableField(exist = false)
    @ApiModelProperty(value = "查看Ai切片是否分析完成，没有完成设置对照组按钮置灰不可配置")
    private boolean isAiSlideFinished;

    @TableField(exist = false)
    @ApiModelProperty(value = "SOP")
    private String sop = "PATH001";

}
