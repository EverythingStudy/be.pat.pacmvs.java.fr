package cn.staitech.fr.domain.in;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 指标信息类
 */
@Data
@AllArgsConstructor
public class IndicatorAddIn {

    // 指标英文名称
    private String englishName;

    // 预测结果
    private String result;

    // 单位
    private String unit;


}
