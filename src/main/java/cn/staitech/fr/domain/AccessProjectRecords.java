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
 * 访问项目记录表
 * </p>
 *
 * @author wanglibei
 * @since 2024-04-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_access_project_records")
@ApiModel(value="AccessProjectRecords对象", description="访问项目记录表")
public class AccessProjectRecords implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "记录id")
    @TableId(value = "records_id", type = IdType.AUTO)
    private Long recordsId;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "项目id")
    private Long projectId;

    @ApiModelProperty(value = "访问时间")
    private Date accessTime;

    @ApiModelProperty(value = "状态(0待启动，1进行中，2暂停，3已完成，4锁定)")
    private Integer status;

}
