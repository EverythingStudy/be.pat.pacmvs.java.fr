package cn.staitech.fr.domain.in;

import cn.staitech.fr.constant.CommonConstant;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 指标信息类
 */
@Data
public class IndicatorAddIn {

    // 指标英文名称
    private String englishName;

    // 预测结果
    private String result;

    // 单位
    private String unit;

    private String structType;
    //指标关联结构编码
    private String structureIds;

    public IndicatorAddIn(String structureIds) {
        this.result = CommonConstant.SINGLE_RESULT;
        this.structType = CommonConstant.NUMBER_1;
        this.structureIds = structureIds;
    }

    public IndicatorAddIn(String englishName, String result, String unit,String structureIds) {
        this.englishName = englishName;
        this.result = result;
        this.unit = unit;
        this.structureIds = structureIds;
    }

    public IndicatorAddIn(String result, String structType,String structureIds) {
        this.result = result;
        this.structType = structType;
        this.structureIds = structureIds;
    }

    public IndicatorAddIn(String englishName, String result, String unit, String structType,String structureIds) {
        this.englishName = englishName;
        this.result = result;
        this.unit = unit;
        this.structType = structType;
        this.structureIds = structureIds;

    }

}
