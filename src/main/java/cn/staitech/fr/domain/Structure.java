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
 * @create: 2023-09-13 16:37:44
 * @Description: 结构
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("tb_structure")
public class Structure {
    /**
     * 结构ID
     */
    @TableId(value = "structure_id", type = IdType.INPUT)
    @ApiModelProperty(value = "结构ID", hidden = true)
    private String structureId;

    /**
     * 结构名称
     */
    @ApiModelProperty(value = "结构名称", required = true)
    private String name;

    /**
     * 结构名称 - En
     */
    @ApiModelProperty(value = "结构名称英文", required = true)
    private String nameEn;

    /**
     * 种属ID
     */
    @ApiModelProperty(value = "种属ID", required = true)
    private String speciesId;

    /**
     * 脏器ID
     */
    @ApiModelProperty(value = "脏器ID", required = true)
    private String organId;

    @ApiModelProperty(value = "RO：结构类型  ROA:标注区域 ROE:考核区域", required = true)
    private String type;

    /**
     * 机构ID
     */
    @ApiModelProperty(value = "机构ID")
    @TableField("organization_id")
    private Long organizationId;
}
