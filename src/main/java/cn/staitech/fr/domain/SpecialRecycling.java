package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 专题回收站表
 * </p>
 *
 * @author author
 * @since 2024-03-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("fr_special_recycling")
@ApiModel(value="SpecialRecycling对象", description="专题回收站表")
public class SpecialRecycling implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键id")
    @TableId(value = "recycling_id", type = IdType.AUTO)
    private Long recyclingId;

    @ApiModelProperty(value = "专题表关联id")
    private Long specialId;

    @ApiModelProperty(value = "切片数量")
    private Integer slideNum;

    @ApiModelProperty(value = "到期时间")
    private Date expireTime;

    @ApiModelProperty(value = "删除标志(0:正常，1:删除)")
    private String delFlag;

    @ApiModelProperty(value = "创建者")
    private Long createBy;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新者")
    private Integer updateBy;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;


}
