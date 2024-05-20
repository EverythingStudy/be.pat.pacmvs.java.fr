package cn.staitech.fr.domain.in;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private String structType;

    public IndicatorAddIn(String englishName, String result, String unit) {
        this.englishName = englishName;
        this.result = result;
        this.unit = unit;
    }
}
