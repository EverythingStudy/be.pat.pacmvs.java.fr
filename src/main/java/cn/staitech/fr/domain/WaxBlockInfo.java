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
 * 蜡块编号明细
 * </p>
 *
 * @author author
 * @since 2024-03-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("fr_wax_block_info")
@ApiModel(value="WaxBlockInfo对象", description="蜡块编号明细")
public class WaxBlockInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "蜡块编号id")
    private Long numberId;

    @ApiModelProperty(value = "蜡块编号")
    private String waxCode;

    @ApiModelProperty(value = "专题id")
    private Long topicId;

    @ApiModelProperty(value = "专题名称")
    private String topicName;

    @ApiModelProperty(value = "种属ID")
    private String speciesId;

    @ApiModelProperty(value = "种属名称")
    private String speciesName;

    @ApiModelProperty(value = "脏器id")
    private String organId;

    @ApiModelProperty(value = "脏器名称")
    private String organName;

    @ApiModelProperty(value = "英文名称")
    private String organNameEn;

    @ApiModelProperty(value = "脏器数量")
    private Integer organNumber;

    @ApiModelProperty(value = "性别（M:雄；F:雌）")
    private String genderFlag;

    @ApiModelProperty(value = "删除标志（0代表存在 1代表删除）")
    private String delFlag;

    @ApiModelProperty(value = "机构ID")
    private Long organizationId;

    @ApiModelProperty(value = "创建者ID")
    private Long createBy;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新者ID")
    private Long updateBy;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;


}
