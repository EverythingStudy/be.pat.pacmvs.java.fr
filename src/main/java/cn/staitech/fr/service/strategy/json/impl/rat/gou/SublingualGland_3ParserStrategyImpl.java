package cn.staitech.fr.service.strategy.json.impl.rat.gou;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import cn.staitech.fr.utils.DecimalUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


/**
 * 狗 舌下腺
 */
@Slf4j
@Service
public class SublingualGland_3ParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private CommonJsonCheck commonJsonCheck;
    @Autowired
    private AreaUtils areaUtils;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.debug("SublingualGlandParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("狗舌下腺结构指标计算开始：");

        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        String slideAreaStr = singleSlideMapper.selectById(jsonTask.getSingleId()).getArea();
        BigDecimal slideArea = new BigDecimal(slideAreaStr);

        // 获取各项指标
        BigDecimal ductAreaPer = commonJsonParser.getOrganAreaMicron(jsonTask, "30A06F"); // A: 导管面积（单个）
        BigDecimal ductAreaTotal = commonJsonParser.getOrganAreaMicron(jsonTask, "30A06F"); // B: 导管面积（全片）
        BigDecimal acinusArea = commonJsonParser.getOrganArea(jsonTask, "30A06D").getStructureAreaNum(); // C: 腺泡面积
        Integer acinusNucleusCount = commonJsonParser.getOrganAreaCount(jsonTask, "30A06E"); // D: 腺泡细胞核数量

        // 算法输出指标
        Annotation annotationC = new Annotation();
        annotationC.setAreaName("导管面积（单个）");
        annotationC.setAreaUnit(MULTIPLIED_SQ_UM_THOUSAND);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "30A06F", annotationC, 1);
        //indicatorResultsMap.put("导管面积（单个）", new IndicatorAddIn("", DecimalUtils.setScale3(ductAreaPer), MULTIPLIED_SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "30A06F"));
        indicatorResultsMap.put("导管面积（全片）", new IndicatorAddIn("", DecimalUtils.setScale3(ductAreaTotal), MULTIPLIED_SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "30A06F"));
        indicatorResultsMap.put("腺泡面积", new IndicatorAddIn("", DecimalUtils.setScale3(acinusArea), SQ_MM, CommonConstant.NUMBER_1, "30A06D"));
        indicatorResultsMap.put("腺泡细胞核数量", new IndicatorAddIn("", acinusNucleusCount.toString(), PIECE, CommonConstant.NUMBER_1, "30A06E"));
        //indicatorResultsMap.put("组织轮廓", new IndicatorAddIn("", DecimalUtils.setScale3(slideArea),"平方毫米", CommonConstant.NUMBER_1, "30A111"));

        // 产品呈现指标
        indicatorResultsMap.put("舌下腺面积", new IndicatorAddIn("Sublingual gland area", DecimalUtils.setScale3(slideArea), SQ_MM, "30A111"));

        if (slideArea.compareTo(BigDecimal.ZERO) != 0) {
            // 导管面积占比（全片）= B / E
            BigDecimal ductAreaRatioAll = commonJsonParser.getProportion(ductAreaTotal, slideArea.multiply(new BigDecimal(1000)));
            indicatorResultsMap.put("导管面积占比（全片）", new IndicatorAddIn("Ducts area% (all)", DecimalUtils.setScale3(ductAreaRatioAll), PERCENTAGE, areaUtils.getStructureIds("30A06F", "30A111")));

            // 腺泡面积占比 = C / E
            BigDecimal acinusAreaRatio = commonJsonParser.getProportion(acinusArea, slideArea);
            indicatorResultsMap.put("腺泡面积占比", new IndicatorAddIn("Acinus area%", DecimalUtils.setScale3(acinusAreaRatio), PERCENTAGE, areaUtils.getStructureIds("30A06D", "30A111")));
        }

        if (acinusArea.compareTo(BigDecimal.ZERO) != 0) {
            // 腺泡细胞核密度 = D / C
            BigDecimal acinusNucleusDensity = commonJsonParser.bigDecimalDivideCheck(new BigDecimal(acinusNucleusCount), acinusArea);
            indicatorResultsMap.put("腺泡细胞核密度", new IndicatorAddIn("Nucleus density of acinar cell", DecimalUtils.setScale3(acinusNucleusDensity), SQ_MM_PIECE, areaUtils.getStructureIds("30A06E", "30A06D")));
        }

        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
        log.info("狗舌下腺结构指标计算结束");
    }

    @Override
    public String getAlgorithmCode() {
        return "Salivary_Glands_Sublingual_3";
    }
}
