package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 人工诊断表
 * </p>
 *
 * @author wanglibei
 * @since 2024-04-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("fr_diagnosis")
@ApiModel(value="Diagnosis对象", description="人工诊断表")
public class Diagnosis implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "诊断ID")
    @TableId(value = "diagnosis_id", type = IdType.AUTO)
    private Long diagnosisId;

    @ApiModelProperty(value = "专题id")
    private Long specialId;

    @ApiModelProperty(value = "专题选片ID")
    private Long singleId;
    
    @ApiModelProperty(name = "groupId" , value = "分组id")
	private Long groupId;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "逻辑删除状态（0:删除 1:未删除）")
    private Integer deleteFlag;

    @ApiModelProperty(value = "创建人id")
    private Long createBy;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新人id")
    private Long updateBy;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;


}
