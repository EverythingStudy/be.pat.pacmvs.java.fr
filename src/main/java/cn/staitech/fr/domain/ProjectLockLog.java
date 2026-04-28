package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <p>
 * 项目锁定日志表
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
public class ProjectLockLog implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "主键id")
    @TableId(value = "lock_log_id", type = IdType.AUTO)
    private Long lockLogId;

    @TableField("special_id")
    @ApiModelProperty(value = "项目ID")
    private Long projectId;

    @ApiModelProperty(value = "类型 0：锁定  1：解锁")
    private Integer type;

    @ApiModelProperty(value = "原因")
    private String reason;

    @ApiModelProperty(value = "创建者")
    private Long createBy;
    
    @TableField(exist = false)
    private String  nickName;
    
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;


}
