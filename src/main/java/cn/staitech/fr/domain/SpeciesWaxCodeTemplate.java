package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 种属蜡块模板表
 *
 * @author yxy
 */
@TableName(value = "tb_species_wax_code_template")
@Data
public class SpeciesWaxCodeTemplate implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 种属ID
     */
    private String speciesId;
    /**
     * 蜡块编号
     */
    private String waxCode;
    /**
     * 脏器名称
     */
    private String organName;
    /**
     * 英文名称
     */
    private String organEn;
    /**
     * 取材块数
     */
    private Integer blockCount;
    /**
     * 性别（M：男性；F：女性；N：未知）
     */
    private String sexFlag;
    /**
     * 脏器编码
     */
    private String organCode;
    /**
     * 脏器缩写
     */
    private String abbreviation;
    /**
     * 创建人id
     */
    private Long createBy;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新人id
     */
    private Long updateBy;
    /**
     * 更新时间
     */
    private Date updateTime;
}