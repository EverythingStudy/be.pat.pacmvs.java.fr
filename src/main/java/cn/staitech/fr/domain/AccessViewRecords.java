package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 访问view页面次数记录首页日活
 * </p>
 *
 * @author jiazx
 * @since 2025-08-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_access_view_records")
@ApiModel(value="TbAccessViewRecords对象", description="访问view页面次数记录首页日活")
public class AccessViewRecords implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "记录id")
    @TableId(value = "view_record_id", type = IdType.AUTO)
    private Long viewRecordId;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "切片id")
    private Long slideId;

    @ApiModelProperty(value = "项目id")
    private Long projectId;

    @ApiModelProperty(value = "访问时间")
    private Date accessTime;


}
