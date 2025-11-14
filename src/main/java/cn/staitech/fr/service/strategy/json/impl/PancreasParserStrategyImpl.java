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
import cn.staitech.fr.service.strategy.json.OutlineCustom;
import cn.staitech.fr.utils.MathUtils;
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
public class PancreasParserStrategyImpl extends AbstractCustomParserStrategy implements OutlineCustom {
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


    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        //A 上皮细胞核数量
        Integer countA = getOrganAreaCount(jsonTask, "105075");
        //B 酶原颗粒面积
        BigDecimal organArea = getOrganArea(jsonTask, "105076").getStructureAreaNum();
        //C 胰岛数量
        Integer count1 = getOrganAreaCount(jsonTask, "105077");
        //D 胰岛面积（单个）
        List<Annotation> annotationList = getStructureContourList(jsonTask, "105077");
        //4 胰岛细胞核密度（单个）个/103 μm2 4=F/D
        List<BigDecimal> dataList = new ArrayList<>();
//        if (CollectionUtils.isNotEmpty(annotationList)) {
//            for (Annotation annotation : annotationList) {
//                String contour = annotation.getContour();
//                //F 胰岛细胞核数量（单个）
//                Annotation temp = getContourInsideOrOutside(jsonTask, contour, "105078", true);
//                if (annotation.getStructureAreaNum().compareTo(BigDecimal.ZERO) > 0 && temp.getCount() != 0) {
//                    dataList.add(BigDecimal.valueOf(temp.getCount()).divide((annotation.getStructureAreaNum().multiply(BigDecimal.valueOf(1000))), 3, RoundingMode.HALF_UP));
//                }
//            }
//        }
        //E 胰岛面积（全片）mm2
        BigDecimal organArea1 = getOrganArea(jsonTask, "105077").getStructureAreaNum();
//        //F 胰岛细胞核数量（单个）105077、105078
//        Integer count3 = getOrganAreaCount(jsonTask, "105078");
        //G 间质面积
        BigDecimal organArea2 = getOrganArea(jsonTask, "105027").getStructureAreaNum();
        //H 单位103 μm2 导管面积（单个）;
        Integer count2 = getOrganAreaCount(jsonTask, "10506F");
        //J 导管面积（全片）
        BigDecimal organArea3 = getOrganArea(jsonTask, "10506F").getStructureAreaNum();
        //L 血管面积
        BigDecimal organArea4 = getOrganArea(jsonTask, "105003").getStructureAreaNum();
        //M 血管内红细胞面积
        Annotation annotationInner = getInsideOrOutside(jsonTask, "105003", "105004", true);
        //N 血管外红细胞面积
        Annotation annotationOuter = getInsideOrOutside(jsonTask, "105003", "105004", false);
        //O 组织轮廓面积
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal organAreaO = new BigDecimal(singleSlide.getArea());
        //P 胰岛细胞核数量（全片）105077、105078
        //Integer countP = getOrganAreaCount(jsonTask, "105078");
        //Q 红细胞面积 105004
        BigDecimal organAreaQ = getOrganArea(jsonTask, "105004").getStructureAreaNum();


        //算法输出指标
        indicatorResultsMap.put("上皮细胞核数量", createIndicator(String.valueOf(countA), PIECE, "105075"));
        indicatorResultsMap.put("酶原颗粒面积", createIndicator(organArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "105076"));
        indicatorResultsMap.put("胰岛数量", createIndicator(String.valueOf(count1), PIECE, "105077"));
        indicatorResultsMap.put("胰岛面积（全片）", createIndicator(organArea1.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "105077"));
        indicatorResultsMap.put("间质面积", createIndicator(organArea2.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "105027"));
        indicatorResultsMap.put("导管数量", createIndicator(String.valueOf(count2), PIECE, "10506F"));
        indicatorResultsMap.put("导管面积（全片）", createIndicator(organArea3.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "10506F"));
        //indicatorResultsMap.put("导管细胞核数量（单个）", createDefaultIndicator("10506F,10507B"));
        indicatorResultsMap.put("血管面积", createIndicator(organArea4.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "105003"));
        indicatorResultsMap.put("血管内红细胞面积", createIndicator(annotationInner.getStructureAreaNum().setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "105003,105004"));
        indicatorResultsMap.put("血管外红细胞面积", createIndicator(annotationOuter.getStructureAreaNum().setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "105003,105004"));
        //indicatorResultsMap.put("胰岛细胞核数量（全片）", createIndicator(String.valueOf(countP), PIECE, "105077,105078"));


        //产品定义指标
        //indicatorResultsMap.put("胰腺面积", createNameIndicator("Pancreas area%", organAreaO.setScale(3, RoundingMode.DOWN).toString(), SQ_MM, "105111"));
        BigDecimal O = new BigDecimal(singleSlide.getArea());
        //1 上皮细胞核密度 个/mm2 1=A/(O-E-G)
        if (O.subtract(organArea1).subtract(organArea2) != BigDecimal.ZERO) {
            BigDecimal result = bigDecimalDivideCheck(BigDecimal.valueOf(Long.valueOf(countA)), O.subtract(organArea1).subtract(organArea2));
            indicatorResultsMap.put("上皮细胞核密度", createNameIndicator("Nucleus density of  epithelial cell", result.toString(), SQ_MM_PIECE, "105075,105111,105077,105027"));
        }
        //2 酶原颗粒面积占比 % 2=B/O
        indicatorResultsMap.put("酶原颗粒面积占比", createNameIndicator("Zymogen granule area%", getProportion(organArea, O).toString(), PERCENTAGE, "105076,105111"));
        // 3 胰岛面积占比 % 3=E/O
        indicatorResultsMap.put("胰岛面积占比", createNameIndicator("Pancreatic islet area%", getProportion(organArea1, O).toString(), PERCENTAGE, "105077,105111"));
        //4 胰岛细胞核密度（单个） 个/103 μm2 4=F/D
        //indicatorResultsMap.put("胰岛细胞核密度（单个）", createNameIndicator("Nucleus density of pancreatic islet（per）", MathUtils.getConfidenceInterval(dataList), SQ_UM_PICE, "105077,105078"));
        //5 间质面积占比 % 5=G/O
        indicatorResultsMap.put("间质面积占比", createNameIndicator("Interstitial area%", getProportion(organArea2, O).toString(), PERCENTAGE, "105027,105111"));
        //6 导管面积占比 % 6=J/O
        indicatorResultsMap.put("导管面积占比", createNameIndicator("Vascular area%", getProportion(organArea3, O).toString(), PERCENTAGE, "10506F,105111"));
        //7 导管细胞核密度（单个） 个/103 μm2 7=K/I
        annotationList = getStructureContourList(jsonTask, "10506F");
        if (CollectionUtils.isNotEmpty(annotationList)) {
            dataList.clear();
            for (Annotation annotation : annotationList) {
                String contour = annotation.getContour();
                //K 导管细胞核数量（单个）个
                Annotation temp = getContourInsideOrOutside(jsonTask, contour, "10507B", true);
                if (annotation.getStructureAreaNum().compareTo(BigDecimal.ZERO) > 0 && temp.getCount() != 0) {
                    //7 导管细胞核密度（单个） 个/103 μm2 7=K/I
                    dataList.add(BigDecimal.valueOf(temp.getCount()).divide(annotation.getStructureAreaNum().multiply(BigDecimal.valueOf(1000)), 3, RoundingMode.HALF_UP));
                }
            }
        }
        indicatorResultsMap.put("导管细胞核密度（单个）", new IndicatorAddIn("Nucleus density of duct（per）", MathUtils.getConfidenceInterval(dataList), SQ_UM_PICE, CommonConstant.NUMBER_0, "10506F,10507B"));
        //8 血管内红细胞面积占比 % 8=M/O
        indicatorResultsMap.put("血管内红细胞面积占比", createNameIndicator("Intravascular erythrocyte area%", getProportion(annotationInner.getStructureAreaNum(), O).toString(), PERCENTAGE, "105003,105004,105111"));
        //9 血管外红细胞面积占比 % 9=N/O
        indicatorResultsMap.put("血管外红细胞面积占比", createNameIndicator("Extravascular erythrocyte area%", getProportion(annotationOuter.getStructureAreaNum(), O).toString(), PERCENTAGE, "105003,105004,105111"));
        //10 血管面积占比 % 10=L/O
        indicatorResultsMap.put("血管面积占比", createNameIndicator("Vessel area%", getProportion(organArea4, O).toString(), PERCENTAGE, "105003,105111"));
        //11 腺泡面积占比 % 11=(O-E-G)/O
        indicatorResultsMap.put("腺泡面积占比", createNameIndicator("Pancreatic acinus area%", getProportion(O.subtract(organArea1).subtract(organArea2), O).toString(), PERCENTAGE, "105077,105027,105111"));
        //12 腺泡细胞核密度（全片） 个/mm2 12=P/E
        //indicatorResultsMap.put("胰岛细胞核密度（全片）", createNameIndicator("Nucleus density of pancreatic islet（all）", bigDecimalDivideCheck(new BigDecimal(countP), organArea1).toString(), SQ_MM_PIECE, "105077,105078"));
        //13 胰腺面积 mm2 13=O
        indicatorResultsMap.put("胰腺面积", createNameIndicator("Pancreas area", organAreaO, SQ_MM, "105111"));
        //14 红细胞面积 mm2 14=Q
        indicatorResultsMap.put("红细胞面积", createNameIndicator("Erythrocyte area", organAreaQ, SQ_MM, "105004"));
        Annotation annotationBy = new Annotation();
        //F
        annotationBy.setCountName("胰岛细胞核数量（单个）");
        commonJsonParser.putAnnotationDynamicData(jsonTask, "105077", "105078", annotationBy);
        //K
        annotationBy.setCountName("导管细胞核数量（单个）");
        commonJsonParser.putAnnotationDynamicData(jsonTask, "10506F", "10507B", annotationBy);
        //D
        annotationBy.setCountName(null);
        annotationBy.setAreaName("胰岛面积（单个）");
        annotationBy.setAreaUnit(SQ_UM_THOUSAND);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "105077", annotationBy, 1);
        annotationBy.setCountName(null);
        //I
        annotationBy.setAreaName("导管面积（单个）");
        annotationBy.setAreaUnit(SQ_UM_THOUSAND);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "10506F", annotationBy, 1);
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Pancreas";
    }

    @Override
    public void getCustomOutLine(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal pituitaryH = new BigDecimal(singleSlide.getArea());
        indicatorResultsMap.put("胰腺面积", createNameIndicator("Pancreas area", String.valueOf(pituitaryH.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "105111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
