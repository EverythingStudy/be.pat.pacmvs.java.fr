package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
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
@Service("Thymus")
public class ThymusParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.debug("ThymusParserStrategyImpl init");
    }


    /**
     * 皮质	14403D
     * 髓质	14403E
     * 结缔组织	14403F
     * 红细胞	145004
     * 组织轮廓	144111
     * <p>
     * <p>
     * 皮质面积	A	平方毫米	数据相加输出
     * 髓质面积	B	平方毫米	数据相加输出
     * 髓质内结缔组织面积	C	平方毫米
     * 髓质外结缔组织面积	D	平方毫米
     * 红细胞面积	E	平方毫米	数据相加输出
     * 组织轮廓	F	平方毫米
     * <p>
     * 产品呈现指标	指标代码（仅限本文档）	单位(保留3位小数)	English	计算方式	备注
     * 皮质占比	1	%	Cortex area%	1=（F-B-D）/F
     * 髓质占比	2	%	Medulla area%	2=B/F
     * 皮髓比	3	%	Cortex:medulla ratio	3=（F-B-D）/B
     * 红细胞面积占比	4	%	Erythrocyte area%	4=E/F
     * 结缔组织面积占比	5	%	Connective tissue area%	5=（C+D）/F
     * 胸腺面积	6	平方毫米	Thymus area	6=F
     *
     * @param jsonTask
     */

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        BigDecimal organArea = getOrganArea(jsonTask, "14403D").getStructureAreaNum();
        BigDecimal organArea1 = getOrganArea(jsonTask, "14403F").getStructureAreaNum();
        BigDecimal organArea2 = getOrganArea(jsonTask, "14403E").getStructureAreaNum();
        BigDecimal organArea3 = getOrganArea(jsonTask, "145004").getStructureAreaNum();
        Annotation annotation = commonJsonParser.getInsideOrOutside(jsonTask, "14403E", "14403F", true);
        BigDecimal organArea4 = annotation.getStructureAreaNum();
        indicatorResultsMap.put("髓质内结缔组织面积", new IndicatorAddIn("", organArea4.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        Annotation annotation1 = commonJsonParser.getInsideOrOutside(jsonTask, "14403E", "14403F", false);
        BigDecimal organArea5 = annotation1.getStructureAreaNum();
        indicatorResultsMap.put("髓质外结缔组织面积", new IndicatorAddIn("", organArea5.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("髓质面积", new IndicatorAddIn("", organArea2.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("红细胞面积", new IndicatorAddIn("", organArea3.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        //indicatorResultsMap.put("组织轮廓", new IndicatorAddIn("", singleSlide.getArea(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("胸腺面积", new IndicatorAddIn("Thymus area", singleSlide.getArea(), "平方毫米", CommonConstant.NUMBER_0));
        BigDecimal F = new BigDecimal(singleSlide.getArea());
        BigDecimal b1 = BigDecimal.ZERO;
        if (F.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal temp = organArea2.add(organArea4);
            b1 = commonJsonParser.getProportion(F.subtract(temp), F);
        }
        indicatorResultsMap.put("皮质占比", new IndicatorAddIn("Cortex area%", String.valueOf(b1), "%", CommonConstant.NUMBER_0));
        BigDecimal b2 = BigDecimal.ZERO;
        BigDecimal b3 = BigDecimal.ZERO;
        BigDecimal A = BigDecimal.ZERO;
        if (organArea2.compareTo(BigDecimal.ZERO) != 0 && F.compareTo(BigDecimal.ZERO) != 0) {
            b2 = commonJsonParser.getProportion(organArea2, F);
            A = organArea2.add(organArea4);
            b3 = commonJsonParser.getProportion(F.subtract(A), organArea2);
        }
        indicatorResultsMap.put("皮质面积", new IndicatorAddIn("", F.subtract(A).setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("髓质占比", new IndicatorAddIn("Medulla area%", String.valueOf(b2), "%", CommonConstant.NUMBER_0));
        indicatorResultsMap.put("皮髓比", new IndicatorAddIn("Cortex:medulla ratio", String.valueOf(b3), "%", CommonConstant.NUMBER_0));

        BigDecimal b4 = BigDecimal.ZERO;
        if (organArea3.compareTo(BigDecimal.ZERO) != 0 && F.compareTo(BigDecimal.ZERO) != 0) {
            b4 = commonJsonParser.getProportion(organArea3, F);
        }
        indicatorResultsMap.put("红细胞面积占比", new IndicatorAddIn("Erythrocyte area%", String.valueOf(b4), "%", CommonConstant.NUMBER_0));

        BigDecimal b5 = BigDecimal.ZERO;
        if (organArea1.compareTo(BigDecimal.ZERO) != 0 && F.compareTo(BigDecimal.ZERO) != 0) {
            b5 = commonJsonParser.getProportion(organArea1, F);
        }
        indicatorResultsMap.put("结缔组织面积占比", new IndicatorAddIn("Connective tissue area%", String.valueOf(b5), "%", CommonConstant.NUMBER_0));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Thymus";
    }
}
