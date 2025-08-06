package cn.staitech.fr.vo.category;

import lombok.Data;

import java.util.Date;

@Data
public class CategorySize {
    private Long categoryId;

    /**
     * 结构指标ID
     */
    private Long indicatorId;

    /**
     * 标注类别名称
     */
    private String categoryName;

    /**
     * 结构ID
     */
    private String structureId;

    /**
     * 结构大小
     */
    private Integer structureSize;

    /**
     * 颜色的RGB值
     */
    private String rgb;

    /**
     * 颜色的HEX值
     */
    private String hex;

    /**
     * 颜色名称(备用)
     */
    private String color;

    /**
     * 完整编码
     */
    private String number;

    /**
     * 图层顺序
     */
    private Integer orderNumber;

    /**
     * 组织机构ID
     */
    private Long organizationId;

    /**
     * 0:默认标注类型；1:unlable
     */
    private Integer annoType;

    /**
     * 默认为0，1为删除
     */
    private Integer delFlag;

    /**
     * 创建者
     */
    private Long createBy;

    /**
     * 更新者
     */
    private Long updateBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 标注编码
     */
    private String categoryCode;

    /**
     * 组内标签顺序
     */
    private Integer groupNumber;

    /**
     * 标签类型 0:下拉筛选标签；1:自定义标签
     */
    private Integer categoryType;

    private Integer imageZoom;

}

