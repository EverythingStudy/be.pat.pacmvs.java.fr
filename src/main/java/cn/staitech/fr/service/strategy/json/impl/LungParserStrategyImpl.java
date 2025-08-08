package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
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
import java.util.HashMap;
import java.util.Map;

/**
 * 大鼠-肺脏
 */
@Slf4j
@Service("Lung")
public class LungParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    private AiForecastService aiForecastService;

    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private CommonJsonCheck commonJsonCheck;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("LungParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {

        // 查询精细轮廓面积
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        //String accurateArea = singleSlide.getArea();
        BigDecimal outlineArea = commonJsonParser.getOrganArea(jsonTask, "14C111").getStructureAreaNum();
        Integer count = commonJsonParser.getOrganAreaCount(jsonTask, "14C006");

        //肺泡上皮细胞核密度
        //Double density = count / Double.parseDouble(outlineArea);
        Double density = count / outlineArea.doubleValue();

        // 查询支气管面积
        BigDecimal bronchiArea = commonJsonParser.getOrganArea(jsonTask, "14C002").getStructureAreaNum();

        // 查询血管面积
        BigDecimal vesselArea = commonJsonParser.getOrganArea(jsonTask, "14C003").getStructureAreaNum();

        // 查询血管内红细胞面积
        BigDecimal intravascularErythrocyteArea = commonJsonParser.getInsideOrOutside(jsonTask, "14C003", "14C004", true).getStructureAreaNum();

        // 查询血管外红细胞面积
        BigDecimal extravascularErythrocyteArea = commonJsonParser.getInsideOrOutside(jsonTask, "14C003", "14C004", false).getStructureAreaNum();

        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();

        // 支气管面积占比
        BigDecimal bronchiAreas = commonJsonParser.getProportion(bronchiArea, outlineArea);

        // 血管面积占比
        BigDecimal vesselAreas = commonJsonParser.getProportion(vesselArea, outlineArea);

        // 血管内红细胞面积占比
        BigDecimal intravascularErythrocyteAreas = commonJsonParser.getProportion(intravascularErythrocyteArea, outlineArea);

        // 血管外红细胞面积占比
        BigDecimal extravascularErythrocyteAreas = commonJsonParser.getProportion(extravascularErythrocyteArea, outlineArea);

        indicatorResultsMap.put("支气管面积", new IndicatorAddIn("支气管面积", String.valueOf(bronchiArea.setScale(3, BigDecimal.ROUND_HALF_UP)), SQ_MM, CommonConstant.NUMBER_1));
//        indicatorResultsMap.put("血管面积", new IndicatorAddIn("血管面积", String.valueOf(vesselArea.setScale(3, BigDecimal.ROUND_HALF_UP)), SQ_MM, CommonConstant.NUMBER_1));
//        indicatorResultsMap.put("血管内红细胞面积", new IndicatorAddIn("血管内红细胞面积", String.valueOf(intravascularErythrocyteArea.setScale(3, BigDecimal.ROUND_HALF_UP)), SQ_MM, CommonConstant.NUMBER_1));
//        indicatorResultsMap.put("血管外红细胞面积", new IndicatorAddIn("血管外红细胞面积", String.valueOf(extravascularErythrocyteArea.setScale(3, BigDecimal.ROUND_HALF_UP)), SQ_MM, CommonConstant.NUMBER_1));
//        indicatorResultsMap.put("肺泡上皮细胞核数量", new IndicatorAddIn("肺泡上皮细胞核数量", String.valueOf(count), PIECE, CommonConstant.NUMBER_1));

        indicatorResultsMap.put("支气管面积占比", new IndicatorAddIn("Bronchi area%", String.valueOf(bronchiAreas), PERCENTAGE));
        indicatorResultsMap.put("血管面积占比", new IndicatorAddIn("Vessel area%", String.valueOf(vesselAreas), PERCENTAGE));
//        indicatorResultsMap.put("血管内红细胞面积占比", new IndicatorAddIn("Intravascular erythrocyte area%", String.valueOf(intravascularErythrocyteAreas), PERCENTAGE));
//        indicatorResultsMap.put("血管外红细胞面积占比", new IndicatorAddIn("Extravascular erythrocyte area%", String.valueOf(extravascularErythrocyteAreas), PERCENTAGE));
//        indicatorResultsMap.put("肺泡上皮细胞核密度", new IndicatorAddIn("Nucleus density of alveolar epithelial cell", String.valueOf(density.setScale(3, BigDecimal.ROUND_HALF_UP)), SQ_MM_PIECE));
        indicatorResultsMap.put("肺脏面积", new IndicatorAddIn("Lung area", String.valueOf(outlineArea.setScale(3, BigDecimal.ROUND_HALF_UP)), SQ_MM));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Lung";
    }
}
