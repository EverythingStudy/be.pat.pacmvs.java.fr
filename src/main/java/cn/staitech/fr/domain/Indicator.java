package cn.staitech.fr.domain;
import cn.staitech.common.core.web.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

/**
 * @author wangf
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("tb_pathological_indicator")
@Api(value = "病例指标", tags = "病例指标")
public class Indicator extends BaseEntity {
    @ApiModelProperty(hidden = true, value = "病例指标id")
    @TableId(value = "indicator_id", type = IdType.AUTO)
    private Long indicatorId;
    @ApiModelProperty(value = "病例指标名称")
    private String indicatorName;

    @ApiModelProperty(value = "病例指标名称")
    private String indicatorNameEn;
    @ApiModelProperty(hidden = true, value = "关联项目数量")
    private Integer projectTotal;

    @ApiModelProperty(value = "标签数量")
    private Long annotationCategoryTotal;

    @ApiModelProperty(hidden = true, value = "创建者")
    private Long createBy;

    @ApiModelProperty(value = "创建者名称")
    private String userName;

    @ApiModelProperty(hidden = true, value = "用户id")
    private Long userId;

    @ApiModelProperty(hidden = true, value = "种属ID")
    private String speciesId;
    @ApiModelProperty(hidden = true, value = "种属名称")
    @TableField(exist = false)
    private String speciesName;
    @ApiModelProperty(hidden = true, value = "脏器ID")
    private String organId;
    @ApiModelProperty(hidden = true, value = "脏器名称")
    @TableField(exist = false)
    private String organName;
    @ApiModelProperty(hidden = true, value = "更新者")
    private Long updateBy;
    @ApiModelProperty(hidden = true, value = "机构ID")
    private Long organizationId;
    @ApiModelProperty(hidden = true, value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    @ApiModelProperty(hidden = true, value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @ApiModelProperty(value = "请求参数")
    @TableField(exist = false)
    private Map<String, Object> createTimeParams;
    @ApiModelProperty(hidden = true, value = "备注")
    private String remark;
    @ApiModelProperty(hidden = true, value = "搜索值")
    @TableField(exist = false)
    private String searchValue;
    @ApiModelProperty(value = "病例指标编号")
    private String number;
    @ApiModelProperty(hidden = true, value = "删除状态")
    private Integer delFlag;

    @ApiModelProperty(value = "标签类型 0:下拉筛选标签；1:自定义标签")
    private Integer indicatorType;
}
