package cn.staitech.fr.domain.in;

import cn.staitech.fr.constant.CommonConstant;
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

    private String structType;

    public IndicatorAddIn() {
        this.result = CommonConstant.SINGLE_RESULT;
        this.structType = CommonConstant.NUMBER_1;
    }

    public IndicatorAddIn(String englishName, String result, String unit) {
        this.englishName = englishName;
        this.result = result;
        this.unit = unit;
    }

    public IndicatorAddIn(String result, String structType) {
        this.result = result;
        this.structType = structType;
    }

}
