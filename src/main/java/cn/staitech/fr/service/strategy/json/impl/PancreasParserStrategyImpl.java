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
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mugw
 * @version 1.0
 * @description 大鼠-内分泌系统-胰腺
 * @date 2024/5/13 10:06:53
 */
@Slf4j
@Service("Pancreas")
public class PancreasParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.debug("PancreasParserStrategyImpl init");
    }

    /**
     * 上皮细胞核	105075
     * 酶原颗粒	105076
     * 胰岛	105077
     * 胰岛细胞核  105078
     * 间质	105027
     * 导管	10506F
     * 导管细胞核	10507B
     * 血管	105003
     * 红细胞	105004
     * 组织轮廓	105111
     * 算法输出指标	指标代码（仅限本文档）	单位（保留小数点后三位）	备注
     * 上皮细胞核数量	A	个	无
     * 酶原颗粒面积	B	平方毫米	数据相加输出
     * 胰岛数量	C	个	无
     * 胰岛面积（单个）	D	103平方微米	单个胰岛面积输出
     * 胰岛面积（全片）	E	平方毫米	数据相加输出
     * 胰岛细胞核数量（单个）	F	个	单个胰岛内胰岛细胞核数量输出
     * 间质面积	G	平方毫米	数据相加输出
     * 导管数量	H	个	无
     * 导管面积（单个）	I	103平方微米	单个导管面积输出
     * 导管面积（全片）	J	平方毫米	数据相加输出
     * 导管细胞核数量（单个）	K	个	单个导管内导管细胞核数量输出
     * 血管面积	L	平方毫米	无
     * 血管内红细胞面积 M	平方毫米	数据相加输出
     * 血管外红细胞面积 N	平方毫米	数据相加输出
     * 组织轮廓面积	O	平方毫米	数据相加输出
     * 胰岛细胞核数量（全片）	P	个	无
     * <p>
     * 产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后三位）	English	计算方式	备注
     * 上皮细胞核密度	1	个/平方毫米	Nucleus density of  epithelial cell	1=A/(O-E-G)
     * 酶原颗粒面积占比	2	%	Zymogen granule area%	2=B/O
     * 胰岛面积占比	3	%	Pancreatic islet area%	3=E/O
     * 胰岛细胞核密度（单个）	4	个/103平方微米	Nucleus density of pancreatic islet（per）	4=F/D	以95%置信区间和均数±标准差呈现
     * 间质面积占比	5	%	Mesenchyme area%	5=G/O
     * 导管面积占比	6	%	Ducts area%	6=J/O
     * 导管细胞核密度（单个）	7	个/103平方微米	Nucleus density of duct（per）	7=K/I	以95%置信区间和均数±标准差呈现
     * 血管内红细胞面积占比	    8	%	Intravascular erythrocyte area%	8=M/O
     * 血管外红细胞面积占比   	9	%	Extravascular erythrocyte area%	9=N/O
     * 血管面积占比	10	%	Vessel area%	10=L/O
     * 腺泡面积占比	11	%	Pancreatic acinus area%	11=（O-E-G）/O
     * 胰岛细胞核密度（全片）	12	个/平方毫米	Nucleus density of pancreatic islet（all）	12=P/E
     * 胰腺面积	13	平方毫米	Pancreas area	13=O
     *
     * @param jsonTask
     */
    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        //B 酶原颗粒面积
        BigDecimal organArea = getOrganArea(jsonTask, "105076").getStructureAreaNum();
        //E 胰岛面积（全片）mm2
        BigDecimal organArea1 = getOrganArea(jsonTask, "105077").getStructureAreaNum();
        //G 间质面积
        BigDecimal organArea2 = getOrganArea(jsonTask, "105027").getStructureAreaNum();
        //J 导管面积
        BigDecimal organArea3 = getOrganArea(jsonTask, "10506F").getStructureAreaNum();
        //L 血管面积
        BigDecimal organArea4 = getOrganArea(jsonTask, "105003").getStructureAreaNum();
        //A 上皮细胞核数量
        Integer countA = getOrganAreaCount(jsonTask, "105075");

        Integer count1 = getOrganAreaCount(jsonTask, "105077");
        //I 单位103 μm2 导管面积（单个）; J单位mm2 导管面积（全片）
        Integer count2 = getOrganAreaCount(jsonTask, "10506F");
        //胰岛细胞核
        Integer count3 = getOrganAreaCount(jsonTask, "105078");
        //M 血管内红细胞面积
        Annotation annotationInner = getInsideOrOutside(jsonTask, "105003", "105004", true);
        //N 血管外红细胞面积
        Annotation annotationOuter = getInsideOrOutside(jsonTask, "105003", "105004", false);
        //O 组织轮廓面积
        BigDecimal organAreaO = getOrganArea(jsonTask, "105111").getStructureAreaNum();

        indicatorResultsMap.put("血管内红细胞面积", new IndicatorAddIn("", annotationInner.getStructureAreaNum().setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1));
        indicatorResultsMap.put("血管外红细胞面积", new IndicatorAddIn("", annotationOuter.getStructureAreaNum().setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1));
        indicatorResultsMap.put("酶原颗粒面积", new IndicatorAddIn("", organArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1));
        indicatorResultsMap.put("胰岛面积（全片）", new IndicatorAddIn("", organArea1.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1));

        indicatorResultsMap.put("间质面积", new IndicatorAddIn("", organArea2.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1));

        indicatorResultsMap.put("导管面积（全片）", new IndicatorAddIn("", organArea3.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1));

        indicatorResultsMap.put("血管面积", new IndicatorAddIn("", organArea4.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1));

        indicatorResultsMap.put("上皮细胞核数量", new IndicatorAddIn("", String.valueOf(countA), "个", CommonConstant.NUMBER_1));

        indicatorResultsMap.put("胰岛数量", new IndicatorAddIn("", String.valueOf(count1), "个", CommonConstant.NUMBER_1));

        indicatorResultsMap.put("导管数量", new IndicatorAddIn("", String.valueOf(count2), "个", CommonConstant.NUMBER_1));

        indicatorResultsMap.put("胰岛细胞核数量（全片）", new IndicatorAddIn("", String.valueOf(count3), "个", CommonConstant.NUMBER_1));
        //indicatorResultsMap.put("组织轮廓面积", new IndicatorAddIn("", singleSlide.getArea(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("胰腺面积", new IndicatorAddIn("Pancreas area%", organAreaO.setScale(3, RoundingMode.DOWN).toString(), SQ_MM, CommonConstant.NUMBER_0));

        indicatorResultsMap.put("胰岛面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1));
        indicatorResultsMap.put("导管面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1));
        indicatorResultsMap.put("胰岛细胞核数量（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1));
        indicatorResultsMap.put("导管细胞核数量（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1));
        BigDecimal O = new BigDecimal(singleSlide.getArea());
        String result = "";
        if (countA != 0) {
            BigDecimal t = O.subtract(organArea1).subtract(organArea2);
            result = BigDecimal.valueOf(Long.valueOf(countA)).divide(t, 3, RoundingMode.HALF_UP).toString();
        }
        indicatorResultsMap.put("上皮细胞核密度", new IndicatorAddIn("Nucleus density of  epithelial cell", result, SQ_MM_PIECE, CommonConstant.NUMBER_0));
        result = "0.000";
        if (!organArea.equals(BigDecimal.ZERO) && !O.equals(BigDecimal.ZERO)) {
            result = commonJsonParser.getProportion(organArea, O).toString();
        }
        indicatorResultsMap.put("酶原颗粒面积占比", new IndicatorAddIn("Zymogen granule area%", result, PERCENTAGE, CommonConstant.NUMBER_0));
        result = "0.000";
        if (!organArea1.equals(BigDecimal.ZERO) && !O.equals(BigDecimal.ZERO)) {
            result = commonJsonParser.getProportion(organArea1, O).toString();
        }
        indicatorResultsMap.put("胰岛面积占比", new IndicatorAddIn("Pancreatic islet area%", result, PERCENTAGE, CommonConstant.NUMBER_0));
        result = "0.000";
        if (!organArea2.equals(BigDecimal.ZERO) && !O.equals(BigDecimal.ZERO)) {
            result = commonJsonParser.getProportion(organArea2, O).toString();
        }
        indicatorResultsMap.put("间质面积占比", new IndicatorAddIn("Interstitial area%", result, PERCENTAGE, CommonConstant.NUMBER_0));
        result = "0.000";
        if (!organArea3.equals(BigDecimal.ZERO) && !O.equals(BigDecimal.ZERO)) {
            result = commonJsonParser.getProportion(organArea3, O).toString();
        }
        indicatorResultsMap.put("导管面积占比", new IndicatorAddIn("Vascular area%", result, PERCENTAGE, CommonConstant.NUMBER_0));
        result = "0.000";
        if (!annotationInner.getStructureAreaNum().equals(BigDecimal.ZERO) && !O.equals(BigDecimal.ZERO)) {
            result = commonJsonParser.getProportion(annotationInner.getStructureAreaNum(), O).toString();
        }
        indicatorResultsMap.put("血管内红细胞面积占比", new IndicatorAddIn("Intravascular erythrocyte area%", result, PERCENTAGE, CommonConstant.NUMBER_0));
        result = "0.000";
        if (!annotationOuter.getStructureAreaNum().equals(BigDecimal.ZERO) && !O.equals(BigDecimal.ZERO)) {
            result = commonJsonParser.getProportion(annotationOuter.getStructureAreaNum(), O).toString();
        }
        indicatorResultsMap.put("血管外红细胞面积占比", new IndicatorAddIn("Extravascular erythrocyte area%", result, PERCENTAGE, CommonConstant.NUMBER_0));
        result = "0.000";
        if (!organArea4.equals(BigDecimal.ZERO) && !O.equals(BigDecimal.ZERO)) {
            result = commonJsonParser.getProportion(organArea4, O).toString();
        }
        indicatorResultsMap.put("血管面积占比", new IndicatorAddIn("Vessel area%", result, PERCENTAGE, CommonConstant.NUMBER_0));
        result = "0.000";
        if (!O.equals(BigDecimal.ZERO)) {
            result = commonJsonParser.getProportion(O.subtract(organArea1).subtract(organArea2), O).toString();
        }
        indicatorResultsMap.put("腺泡面积占比", new IndicatorAddIn("Pancreatic acinus area%", result, PERCENTAGE, CommonConstant.NUMBER_0));
        result = "0.000";
        if (count3 != 0 && !organArea1.equals(BigDecimal.ZERO)) {
            result = BigDecimal.valueOf(count3).divide(organArea1, 3, RoundingMode.HALF_UP).toString();
        }
        indicatorResultsMap.put("胰岛细胞核密度（全片）", new IndicatorAddIn("Nucleus density of pancreatic islet（all）", result, SQ_MM_PIECE, CommonConstant.NUMBER_0));
        List<Annotation> annotationList = getStructureContourList(jsonTask, "105077");
        List<BigDecimal> dataList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(annotationList)) {
            for (Annotation annotation : annotationList) {
                String contour = annotation.getContour();
                //P
                Annotation temp = getContourInsideOrOutside(jsonTask, contour, "105078", true);
                if (annotation.getStructureAreaNum().compareTo(BigDecimal.ZERO) > 0 && temp.getCount() != 0) {
                    dataList.add(BigDecimal.valueOf(temp.getCount()).divide((annotation.getStructureAreaNum().multiply(BigDecimal.valueOf(1000))), 3, RoundingMode.HALF_UP));
                }
            }
        }
        indicatorResultsMap.put("胰岛细胞核密度（单个）", createComplexIndicator(dataList, "Nucleus density of pancreatic islet（per）", SQ_UM_PICE, CommonConstant.NUMBER_0));
        annotationList = getStructureContourList(jsonTask, "10506F");
        if (CollectionUtils.isNotEmpty(annotationList)) {
            dataList.clear();
            for (Annotation annotation : annotationList) {
                String contour = annotation.getContour();
                //K
                Annotation temp = getContourInsideOrOutside(jsonTask, contour, "10507B", true);
                if (annotation.getStructureAreaNum().compareTo(BigDecimal.ZERO) > 0 && temp.getCount() != 0) {
                    dataList.add(BigDecimal.valueOf(temp.getCount()).divide(annotation.getStructureAreaNum().multiply(BigDecimal.valueOf(1000)), 3, RoundingMode.HALF_UP));
                }
            }
        }
        indicatorResultsMap.put("导管细胞核密度（单个）", createComplexIndicator(dataList, "Nucleus density of duct（per）", SQ_UM_PICE, CommonConstant.NUMBER_0));
        Annotation annotationBy = new Annotation();
        annotationBy.setCountName("胰岛细胞核数量（单个）");
        commonJsonParser.putAnnotationDynamicData(jsonTask, "105077", "105078", annotationBy);
        annotationBy.setCountName("导管细胞核数量（单个）");
        commonJsonParser.putAnnotationDynamicData(jsonTask, "10506F", "10507B", annotationBy);
        annotationBy.setCountName(null);
        annotationBy.setAreaName("胰岛面积（单个）");
        annotationBy.setAreaUnit("×10³平方微米");
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "105077", annotationBy, 1);
        annotationBy.setCountName(null);
        annotationBy.setAreaName("导管面积（单个）");
        annotationBy.setAreaUnit("×10³平方微米");
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "10506F", annotationBy, 1);
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Pancreas";
    }
}
