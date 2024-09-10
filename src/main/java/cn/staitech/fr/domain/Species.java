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

/**
 * <p>
 * 种属表
 * </p>
 *
 * @author author
 * @since 2024-03-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_species")
@ApiModel(value="Species对象", description="种属表")
public class Species implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "种属ID")
    @TableId(value = "species_id", type = IdType.AUTO)
    private String speciesId;

    @ApiModelProperty(value = "种属名称")
    private String name;

    @ApiModelProperty(value = "种属名称EN")
    private String nameEn;

    @ApiModelProperty(value = "机构ID")
    private Long organizationId;

    @ApiModelProperty(value = "标记(1医学评审使用)")
    private Integer badge;


}
