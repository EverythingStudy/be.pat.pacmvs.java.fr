package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.*;
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
@Service("AdrenalGland")
public class AdrenalGlandParserStrategyImpl extends AbstractCustomParserStrategy {
    @Resource
    private SingleSlideMapper singleSlideMapper;
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
        log.debug("AdrenalGlandParserStrategyImpl init");
    }

    /**
     * 皮质	10103D
     * 髓质	10103E
     * 肾上腺实质细胞核	101068
     * 红细胞	101004
     * 组织轮廓	101111
     * 算法输出指标	指标代码（仅限本文档）	单位（保留小数点后三位）	备注
     * 皮质面积	A	平方毫米
     * 髓质面积	B	平方毫米
     * 皮质细胞核数量	C	个
     * 髓质细胞核数量	D	个
     * 红细胞面积（全片）	E	平方毫米
     * 组织轮廓面积	F	平方毫米
     *
     *
     * 产品呈现指标	指标代码（仅限本文档）	单位(保留小数点后三位)	English	计算方式	备注
     * 皮质面积占比	1	%	Cortex area %	1=A/F
     * 髓质面积占比	2	%	Medulla area%	2=B/F
     * 皮髓比	3	%	Cortex:Medulla ratio	3=A/B
     * 皮质细胞核密度	4	个/平方毫米	Nucleus density of adrenal cortex	4=C/A
     * 髓质细胞核密度	5	个/平方毫米	Nucleus density of adrenal medulla	5=D/B
     * 红细胞面积占比	6	%	Erythrocyte area%	6=E/F
     * 肾上腺面积	7	平方毫米	Adrenal gland area	7=F
     */

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal organArea = getOrganArea(jsonTask, "10103D").getStructureAreaNum();
        BigDecimal organArea1 = getOrganArea(jsonTask, "101004").getStructureAreaNum();
        BigDecimal organArea2 = getOrganArea(jsonTask, "10103E").getStructureAreaNum();
        Integer C = getInsideOrOutside(jsonTask, "10103D", "101068", true).getCount();
        Integer D = getInsideOrOutside(jsonTask, "10103E", "101068", true).getCount();
        indicatorResultsMap.put("皮质面积", new IndicatorAddIn("", organArea.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("红细胞面积（全片）", new IndicatorAddIn("", organArea1.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("髓质面积", new IndicatorAddIn("", organArea2.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("皮质细胞核数量", new IndicatorAddIn("", String.valueOf(C), "个", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("髓质细胞核数量", new IndicatorAddIn("", String.valueOf(D), "个", CommonConstant.NUMBER_1));
        //indicatorResultsMap.put("组织轮廓面积", new IndicatorAddIn("", singleSlide.getArea(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("肾上腺面积", new IndicatorAddIn("Adrenal gland area%", singleSlide.getArea(), "平方毫米"));
        BigDecimal F = new BigDecimal(singleSlide.getArea());
        BigDecimal b1 = BigDecimal.ZERO;
        if (organArea.compareTo(BigDecimal.ZERO) != 0 && F.compareTo(BigDecimal.ZERO) != 0) {
            b1 = commonJsonParser.getProportion(organArea, F);
        }
        indicatorResultsMap.put("皮质面积占比", new IndicatorAddIn("Cortex area %", String.valueOf(b1), "%", CommonConstant.NUMBER_0));

        BigDecimal b2 = BigDecimal.ZERO;
        if (organArea2.compareTo(BigDecimal.ZERO) != 0 && F.compareTo(BigDecimal.ZERO) != 0) {
            b2 = commonJsonParser.getProportion(organArea2, F);
        }
        indicatorResultsMap.put("髓质面积占比", new IndicatorAddIn("Medulla area%", String.valueOf(b2), "%", CommonConstant.NUMBER_0));
        BigDecimal b3 = BigDecimal.ZERO;
        if (organArea.compareTo(BigDecimal.ZERO) != 0 && organArea2.compareTo(BigDecimal.ZERO) != 0) {
            b3 = commonJsonParser.getProportion(organArea, organArea2);
        }
        indicatorResultsMap.put("皮髓比", new IndicatorAddIn("Cortex:Medulla ratio", String.valueOf(b3), "%", CommonConstant.NUMBER_0));
        BigDecimal b4 = BigDecimal.ZERO;
        if (C != 0 && organArea.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal temp = new BigDecimal(C);
            b4 = temp.divide(organArea,3,RoundingMode.HALF_UP);
        }
        indicatorResultsMap.put("皮质细胞核密度", new IndicatorAddIn("Nucleus density of adrenal cortex", String.valueOf(b4), "个/平方毫米", CommonConstant.NUMBER_0));
        BigDecimal b5 = BigDecimal.ZERO;
        if (D != 0 && organArea2.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal temp = new BigDecimal(D);
            b5 = temp.divide(organArea2,3,RoundingMode.HALF_UP);
        }
        indicatorResultsMap.put("髓质细胞核密度", new IndicatorAddIn("Nucleus density of adrenal medulla", String.valueOf(b5), "个/平方毫米", CommonConstant.NUMBER_0));
        BigDecimal b6 = BigDecimal.ZERO;
        if (organArea1.compareTo(BigDecimal.ZERO) != 0 && F.compareTo(BigDecimal.ZERO) != 0) {
            b6 = commonJsonParser.getProportion(organArea1, F);
        }
        indicatorResultsMap.put("红细胞面积占比", new IndicatorAddIn("Erythrocyte area%", String.valueOf(b6), "%", CommonConstant.NUMBER_0));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Adrenal_gland";
    }
}
