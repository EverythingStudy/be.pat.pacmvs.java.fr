package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mugw
 * @version 1.0
 * @description
 * @date 2024/5/13 10:06:53
 */
@Slf4j
@Service("EyeOpticNerve")
public class D65EyeAndOpticNerveParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private CommonJsonCheck commonJsonCheck;
    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.debug("D65EyeAndOpticNerveParserStrategyImpl init");
    }


    /**
     * 眼球
     * 结构	编码
     * 晶状体	15F101
     * 睫状体-虹膜	15F102
     * 视网膜	15F103
     * 组织轮廓	15F111
     * 算法输出指标	指标代码（仅限本文档）	单位（保留小数点后三位）	备注
     * 晶状体面积	A	平方毫米	若多个数据则相加输出
     * 睫状体-虹膜面积	B	103平方微米	若多个数据则相加输出
     * 视网膜面积	C	平方毫米	若多个数据则相加输出
     * 视网膜周长	D	毫米	若多个数据则相加输出
     * <p>
     * 产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后三位）	English	计算方式	备注
     * 晶状体面积	1	平方毫米	Crystalline lens area	1=A
     * 视网膜面积	3	平方毫米	Retina area	3=C
     * 睫状体-虹膜面积	2	103平方微米	Ciliary body-Iris area	2=B
     * 视网膜平均厚度	4	毫米	Average thickness of retina	4=2C/D
     * <p>
     * <p>
     * <p>
     * 视神经
     * 结构	编码
     * 神经纤维束	13F0BB
     * 神经外膜结缔组织	13F0BA
     * 算法输出指标	指标代码（仅限本文档）	单位（保留小数点后3位）	备注
     * 神经纤维束面积	A	103平方微米	相加输出
     * 神经外膜结缔组织面积	B	平方毫米	无
     * <p>
     * 产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后3位）	English	计算方式	备注
     * 神经纤维束面积	1	平方毫米	Nerve fiber bundles area	1=A	无
     * 神经外膜面积	2	103平方微米	Epineurium area	2=B-A	即神经外膜结缔组织面积
     *
     * @param jsonTask
     */

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        BigDecimal organArea = getOrganArea(jsonTask, "15F101").getStructureAreaNum();
        BigDecimal organArea1 = getOrganArea(jsonTask, "15F102", BigDecimal.valueOf(1000)).getStructureAreaNum();
        BigDecimal organArea2 = getOrganArea(jsonTask, "15F103").getStructureAreaNum();
        BigDecimal organPerimeter = getOrganArea(jsonTask, "15F103").getStructurePerimeterNum();
        BigDecimal organArea3 = getOrganArea(jsonTask, "13F0BB").getStructureAreaNum();
        BigDecimal organArea4 = getOrganArea(jsonTask, "13F0BA").getStructureAreaNum();
        //indicatorResultsMap.put("晶状体面积", new IndicatorAddIn("", organArea.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("晶状体面积", new IndicatorAddIn("Crystalline lens area", organArea.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_0));
        //todo 精度丢失
        indicatorResultsMap.put("睫状体-虹膜面积", new IndicatorAddIn("", organArea1.setScale(3, RoundingMode.HALF_UP).toString(), "10³平方微米", CommonConstant.NUMBER_1));
        //indicatorResultsMap.put("视网膜面积", new IndicatorAddIn("", organArea2.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("视网膜面积", new IndicatorAddIn("Retina area", organArea2.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_0));
        indicatorResultsMap.put("视网膜周长", new IndicatorAddIn("", organPerimeter.setScale(3, RoundingMode.HALF_UP).toString(), "毫米", CommonConstant.NUMBER_1));
        //indicatorResultsMap.put("神经纤维束面积", new IndicatorAddIn("", organArea3.setScale(3, RoundingMode.HALF_UP).toString(), "10³平方微米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("神经纤维束面积", new IndicatorAddIn("Nerve fiber bundles area", organArea3.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_0));
        indicatorResultsMap.put("神经外膜结缔组织面积", new IndicatorAddIn("", organArea4.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));

        BigDecimal A = getOrganArea(jsonTask, "13F0BB",new BigDecimal(1000)).getStructureAreaNum();
        BigDecimal B = getOrganArea(jsonTask, "13F0BA",new BigDecimal(1000)).getStructureAreaNum();
        indicatorResultsMap.put("神经外膜面积", new IndicatorAddIn("Epineurium area", B.subtract(A).toString(), "10³平方微米", CommonConstant.NUMBER_0));


        BigDecimal C = organArea2;
        BigDecimal D = organPerimeter;
        BigDecimal b = BigDecimal.ZERO;
        if (D.compareTo(BigDecimal.ZERO) != 0 && C.compareTo(BigDecimal.ZERO) != 0){
            b = C.multiply(BigDecimal.valueOf(2)).divide(D, 3, RoundingMode.HALF_UP);
        }
        indicatorResultsMap.put("视网膜平均厚度", new IndicatorAddIn("Average thickness of retina", b.toString(), "毫米", CommonConstant.NUMBER_0));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "EyeOpticNerve";
    }
}
