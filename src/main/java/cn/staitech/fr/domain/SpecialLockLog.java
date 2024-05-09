package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <p>
 * 专题锁定日志表
 * </p>
 *
 * @author wanglibei
 * @since 2024-05-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("fr_special_lock_log")
@ApiModel(value="SpecialLockLog对象", description="专题锁定日志表")
public class SpecialLockLog implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "主键id")
    @TableId(value = "lock_log_id", type = IdType.AUTO)
    private Long lockLogId;

    @ApiModelProperty(value = "专题ID")
    private Long specialId;

    @ApiModelProperty(value = "类型 0：锁定  1：解锁")
    private Integer type;

    @ApiModelProperty(value = "原因")
    private String reason;

    @ApiModelProperty(value = "创建者")
    private Long createBy;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;


}
