package cn.staitech.fr.service.strategy.json.impl.rat.gou;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import cn.staitech.fr.utils.DecimalUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 狗 肝脏
 */
@Slf4j
@Component
public class Liver_3ParserStrategyImpl extends AbstractCustomParserStrategy {
    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private CommonJsonCheck commonJsonCheck;
    @Resource
    private AreaUtils areaUtils;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("Liver_3ParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("狗肝脏结构指标计算开始：");

        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        String accurateAreaStr = singleSlideMapper.selectById(jsonTask.getSingleId()).getArea();
        BigDecimal accurateArea = new BigDecimal(accurateAreaStr);

        // 获取各项指标
        BigDecimal portalAreaPer = commonJsonParser.getOrganAreaMicron(jsonTask, "312145"); // A: 门管区面积（单个）
        Integer bileDuctCountPer = commonJsonParser.getOrganAreaCount(jsonTask, "31214A"); // B: 胆管数量（单个门管区）
        BigDecimal bileDuctAreaPer = commonJsonParser.getOrganArea(jsonTask, "31214A").getStructureAreaNum(); // C: 胆管面积（单个门管区）
        BigDecimal centralVeinArea = commonJsonParser.getOrganAreaMicron(jsonTask, "312146"); // D: 中央静脉面积
        BigDecimal venaCavaArea = commonJsonParser.getOrganAreaMicron(jsonTask, "312147"); // E: 大静脉面积
        Integer hepatocyteNucleusCount = commonJsonParser.getOrganAreaCount(jsonTask, "312149"); // F: 肝细胞核数量
        BigDecimal hepatocyteNucleusAreaPer = commonJsonParser.getOrganArea(jsonTask, "312149").getStructureAreaNum(); // G: 肝细胞核面积（单个）
        Integer sinusNucleusCount = commonJsonParser.getOrganAreaCount(jsonTask, "31214D"); // H: 窦内细胞核数量
        BigDecimal totalPortalArea = commonJsonParser.getOrganAreaMicron(jsonTask, "312145"); // L: 门管区面积（全片）
        Integer totalBileDuctCount = commonJsonParser.getOrganAreaCount(jsonTask, "31214A"); // J: 胆管数量（全片）
        BigDecimal totalBileDuctArea = commonJsonParser.getOrganAreaMicron(jsonTask, "31214A"); // K: 胆管面积（全片）

        // 算法输出指标
        //indicatorResultsMap.put("门管区面积（单个）", new IndicatorAddIn("", DecimalUtils.setScale3(portalAreaPer), MULTIPLIED_SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "312145"));
        Annotation annotationC = new Annotation();
        annotationC.setAreaName("门管区面积（单个）");
        annotationC.setAreaUnit(MULTIPLIED_SQ_UM_THOUSAND);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "312145", annotationC, 1);

        /*indicatorResultsMap.put("胆管数量（单个门管区）", new IndicatorAddIn("", bileDuctCountPer.toString(), PIECE, CommonConstant.NUMBER_1, "31214A"));
        indicatorResultsMap.put("胆管面积（单个门管区）", new IndicatorAddIn("", DecimalUtils.setScale3(bileDuctAreaPer), SQ_MM, CommonConstant.NUMBER_1, "31214A"));*/

        Annotation annotationA = new Annotation();
        annotationA.setCountName("胆管数量（单个门管区）");
        annotationA.setCountUnit(PIECE);
        annotationA.setAreaName("胆管面积（单个门管区）");
        annotationA.setAreaUnit(SQ_MM);
        commonJsonParser.putAnnotationDynamicData(jsonTask, "312145", "31214A", annotationA, 1,true);

        indicatorResultsMap.put("中央静脉面积", new IndicatorAddIn("", DecimalUtils.setScale3(centralVeinArea), MULTIPLIED_SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "312146"));
        indicatorResultsMap.put("大静脉面积", new IndicatorAddIn("", DecimalUtils.setScale3(venaCavaArea), MULTIPLIED_SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "312147"));
        indicatorResultsMap.put("肝细胞核数量", new IndicatorAddIn("", hepatocyteNucleusCount.toString(), PIECE, CommonConstant.NUMBER_1, "312149"));
        //indicatorResultsMap.put("肝细胞核面积（单个）", new IndicatorAddIn("", DecimalUtils.setScale3(hepatocyteNucleusAreaPer), SQ_MM, CommonConstant.NUMBER_1, "312149"));
        Annotation annotationB = new Annotation();
        annotationB.setAreaName("肝细胞核面积（单个）");
        annotationB.setAreaUnit(SQ_MM);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "312149", annotationB, 1);

        indicatorResultsMap.put("窦内细胞核数量", new IndicatorAddIn("", sinusNucleusCount.toString(), PIECE, CommonConstant.NUMBER_1, "31214D"));
        //indicatorResultsMap.put("组织轮廓面积", new IndicatorAddIn("", DecimalUtils.setScale3(accurateArea), "平方毫米", CommonConstant.NUMBER_1, "312111"));
        indicatorResultsMap.put("胆管数量（全片）", new IndicatorAddIn("", totalBileDuctCount.toString(), PIECE, CommonConstant.NUMBER_1, "31214A"));
        indicatorResultsMap.put("胆管面积（全片）", new IndicatorAddIn("", DecimalUtils.setScale3(totalBileDuctArea), MULTIPLIED_SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "31214A"));
        indicatorResultsMap.put("门管区面积（全片）", new IndicatorAddIn("", DecimalUtils.setScale3(totalPortalArea), MULTIPLIED_SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "312145"));

        // 产品呈现指标
        indicatorResultsMap.put("肝脏面积", new IndicatorAddIn("Liver area", DecimalUtils.setScale3(accurateArea), SQ_MM, "312111"));

        if (portalAreaPer.compareTo(BigDecimal.ZERO) != 0) {
            // 胆管密度（单个）= B / A
            BigDecimal bileDuctDensityPer = commonJsonParser.bigDecimalDivideCheck(new BigDecimal(bileDuctCountPer), portalAreaPer.multiply(new BigDecimal(1000)));
            indicatorResultsMap.put("胆管密度（单个）", new IndicatorAddIn("Density of bile duct (per)", DecimalUtils.setScale3(bileDuctDensityPer), SQ_UM_PICE, areaUtils.getStructureIds("312145", "31214A")));

            // 胆管面积占比（单个）= C / A
            BigDecimal bileDuctAreaRatioPer = commonJsonParser.getProportion(bileDuctAreaPer, portalAreaPer);
            indicatorResultsMap.put("胆管面积占比（单个）", new IndicatorAddIn("Bile duct area% (per)", DecimalUtils.setScale3(bileDuctAreaRatioPer), PERCENTAGE, areaUtils.getStructureIds("312145", "31214A")));
        }

        if (accurateArea.compareTo(BigDecimal.ZERO) != 0) {
            // 静脉面积占比 = (D + E) / I
            BigDecimal veinAreaRatio = commonJsonParser.getProportion(centralVeinArea.add(venaCavaArea), accurateArea.multiply(new BigDecimal(1000)));
            indicatorResultsMap.put("静脉面积占比", new IndicatorAddIn("Vein area%", DecimalUtils.setScale3(veinAreaRatio), PERCENTAGE, areaUtils.getStructureIds("312146", "312147", "312111")));

            // 肝细胞核密度 = F / I
            BigDecimal hepatocyteNucleusDensity = commonJsonParser.bigDecimalDivideCheck(new BigDecimal(hepatocyteNucleusCount), accurateArea);
            indicatorResultsMap.put("肝细胞核密度", new IndicatorAddIn("Nucleus density of hepatocyte", DecimalUtils.setScale3(hepatocyteNucleusDensity), SQ_MM_PIECE, areaUtils.getStructureIds("312149", "312111")));

            indicatorResultsMap.put("肝细胞核面积（单个）", new IndicatorAddIn("Hepatocyte nucleus area (per)", DecimalUtils.setScale3(hepatocyteNucleusAreaPer), SQ_UM, CommonConstant.NUMBER_0, "312149"));

            // 窦内细胞核密度 = H / (I - L - D - E)
            BigDecimal sinusNucleusDensityDenominator = accurateArea.subtract(totalPortalArea.divide(new BigDecimal(1000)))
                    .subtract(centralVeinArea.divide(new BigDecimal(1000)))
                    .subtract(venaCavaArea.divide(new BigDecimal(1000)));
            BigDecimal sinusNucleusDensity = commonJsonParser.bigDecimalDivideCheck(new BigDecimal(sinusNucleusCount), sinusNucleusDensityDenominator);
            indicatorResultsMap.put("窦内细胞核密度", new IndicatorAddIn("Nucleus density of Sinus cell", DecimalUtils.setScale3(sinusNucleusDensity), SQ_MM_PIECE, areaUtils.getStructureIds("31214D", "312111")));

            // 胆管密度（全片）= J / I
            BigDecimal totalBileDuctDensity = commonJsonParser.bigDecimalDivideCheck(new BigDecimal(totalBileDuctCount), accurateArea);
            indicatorResultsMap.put("胆管密度（全片）", new IndicatorAddIn("Density of bile duct (all)", DecimalUtils.setScale3(totalBileDuctDensity), SQ_MM_PIECE, areaUtils.getStructureIds("31214A", "312111")));

            // 胆管面积占比（全片）= K / I
            BigDecimal totalBileDuctAreaRatio = commonJsonParser.getProportion(totalBileDuctArea, accurateArea.multiply(new BigDecimal(1000)));
            indicatorResultsMap.put("胆管面积占比（全片）", new IndicatorAddIn("Bile duct area% (all)", DecimalUtils.setScale3(totalBileDuctAreaRatio), PERCENTAGE, areaUtils.getStructureIds("31214A", "312111")));
        }

        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
        log.info("狗肝脏结构指标计算结束");
    }

    @Override
    public String getAlgorithmCode() {
        return "Liver_3";
    }
}
