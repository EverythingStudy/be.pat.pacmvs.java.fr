package cn.staitech.fr.domain;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: wangfeng
 * @create: 2023-09-13 16:41:53
 * @Description: 脏器
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("tb_organ")
public class Organ {
    /**
     * 脏器ID
     */
    @TableId(value = "organ_id", type = IdType.NONE)
    @ApiModelProperty(value = "脏器ID", hidden = true)
    private String organId;

    /**
     * 脏器名称
     */
    @ApiModelProperty(value = "脏器名称", required = true)
    private String name;

    /**
     * 脏器名称 - en
     */
    @ApiModelProperty(value = "脏器名称en", required = true)
    private String nameEn;

    /**
     * 种属编码
     */
    @ApiModelProperty(value = "种属编码", required = true)
    private String speciesCode;

    /**
     * 机构ID
     */
    @ApiModelProperty(value = "机构ID")
    @TableField("organization_id")
    private Long organizationId;
}