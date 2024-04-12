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
 * 人工诊断明细表
 * </p>
 *
 * @author wanglibei
 * @since 2024-04-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("fr_diagnosis_detail")
@ApiModel(value="DiagnosisDetail对象", description="人工诊断明细表")
public class DiagnosisDetail implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "诊断明细ID")
    @TableId(value = "diagnosis_detail_id", type = IdType.AUTO)
    private Long diagnosisDetailId;

    @ApiModelProperty(value = "诊断ID")
    private Long diagnosisId;

    @ApiModelProperty(value = "字典类型")
    private String dictType;

    @ApiModelProperty(value = "标签列表")
    private String tags;

    @ApiModelProperty(value = "标签名称")
    private String tagName;
    

    @ApiModelProperty(value = "逻辑删除状态（0:删除 1:未删除）")
    private Integer deleteFlag;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;


}
