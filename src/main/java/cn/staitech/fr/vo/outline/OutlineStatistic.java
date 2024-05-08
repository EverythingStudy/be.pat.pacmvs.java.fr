package cn.staitech.fr.vo.outline;

import cn.staitech.fr.domain.Outline;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * OutlineStatistic
 *
 * @author wangfeng
 * @since 2024-01-04 14:55:03
 */
@Data
public class OutlineStatistic {

    @ApiModelProperty(value = "标注列表")
    List<Outline> list;

    @ApiModelProperty(value = "查询类型：1面积,2周长")
    private Integer bizType;

    @ApiModelProperty(value = " 平均值")
    private Double average;

    @ApiModelProperty(value = "标准偏差")
    private Double standardDeviation;

    @ApiModelProperty(value = "总和")
    private Double sum;

    @ApiModelProperty(value = "总个数")
    private Integer total;

    @ApiModelProperty(value = "最小值")
    private Double minValue;

    @ApiModelProperty(value = "最大值")
    private Double maxValue;
}
