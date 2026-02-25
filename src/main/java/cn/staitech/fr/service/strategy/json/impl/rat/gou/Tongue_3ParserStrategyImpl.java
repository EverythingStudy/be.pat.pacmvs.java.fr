package cn.staitech.fr.service.strategy.json.impl.rat.gou;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
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
 * 狗 舌
 */
@Slf4j
@Component
public class Tongue_3ParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private AiForecastService aiForecastService;

    @Resource
    private CommonJsonParser commonJsonParser;

    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private CommonJsonCheck commonJsonCheck;
    @Resource
    private AreaUtils areaUtils;
    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("TongueParserStrategyImpl init");
    }

    @Override
    public String getAlgorithmCode() {
        return "Tongue_3";
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("狗舌结构指标面积计算开始：");

        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());

        // 获取各项指标
        BigDecimal stratifiedSquamousEpitheliumArea = commonJsonParser.getOrganArea(jsonTask, "30D114").getStructureAreaNum(); // A: 复层扁平上皮面积
        BigDecimal laminaPropriaAndMuscularisArea = commonJsonParser.getOrganArea(jsonTask, "30D01C").getStructureAreaNum(); // B: 固有层+肌层面积
        BigDecimal tissueContourArea = new BigDecimal(singleSlide.getArea()); // C: 组织轮廓面积

        // 算法输出指标
        indicatorResultsMap.put("复层扁平上皮面积", new IndicatorAddIn("", DecimalUtils.setScale3(stratifiedSquamousEpitheliumArea), "平方毫米", CommonConstant.NUMBER_1, "30D114"));
        indicatorResultsMap.put("固有层+肌层面积", new IndicatorAddIn("", DecimalUtils.setScale3(laminaPropriaAndMuscularisArea), "平方毫米", CommonConstant.NUMBER_1, "30D01C"));
        //indicatorResultsMap.put("组织轮廓", new IndicatorAddIn("", DecimalUtils.setScale3(tissueContourArea), "平方毫米", CommonConstant.NUMBER_1, "30D111"));

        // 产品呈现指标
        indicatorResultsMap.put("舌面积", new IndicatorAddIn("Tongue area", DecimalUtils.setScale3(tissueContourArea), "平方毫米", "30D111"));

        if (tissueContourArea.compareTo(BigDecimal.ZERO) != 0) {
            // 复层扁平上皮面积占比 = A / C
            BigDecimal stratifiedSquamousEpitheliumRatio = commonJsonParser.getProportion(stratifiedSquamousEpitheliumArea, tissueContourArea);
            indicatorResultsMap.put("复层扁平上皮面积占比", new IndicatorAddIn("Stratified squamous epithelium area%", DecimalUtils.percentScale3(stratifiedSquamousEpitheliumRatio), CommonConstant.PERCENTAGE, areaUtils.getStructureIds("30D114", "30D111")));

            // 固有层和肌层面积占比 = B / C
            BigDecimal laminaPropriaAndMuscularisRatio = commonJsonParser.getProportion(laminaPropriaAndMuscularisArea, tissueContourArea);
            indicatorResultsMap.put("固有层和肌层面积占比", new IndicatorAddIn("Lamina propria and Muscularis area%", DecimalUtils.percentScale3(laminaPropriaAndMuscularisRatio), CommonConstant.PERCENTAGE, areaUtils.getStructureIds("30D01C", "30D111")));
        } else {
            indicatorResultsMap.put("复层扁平上皮面积占比", new IndicatorAddIn("Stratified squamous epithelium area%", "0.000", CommonConstant.PERCENTAGE, areaUtils.getStructureIds("30D114", "30D111")));
            indicatorResultsMap.put("固有层和肌层面积占比", new IndicatorAddIn("Lamina propria and Muscularis area%", "0.000", CommonConstant.PERCENTAGE, areaUtils.getStructureIds("30D01C", "30D111")));
        }

        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
        log.info("狗舌结构指标面积计算结束");
    }
}
