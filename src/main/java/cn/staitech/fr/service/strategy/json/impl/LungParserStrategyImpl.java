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
 * 大鼠-u肺脏
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

        // A查询所有支气管面积之和
        BigDecimal bronchiArea = commonJsonParser.getOrganArea(jsonTask, "14C002").getStructureAreaNum();

        // B查询所有血管面积之和
        BigDecimal vesselArea = commonJsonParser.getOrganArea(jsonTask, "14C003").getStructureAreaNum();

        // C所有血管轮廓内的红细胞轮廓面积之和
        BigDecimal intravascularErythrocyteArea = commonJsonParser.getInsideOrOutside(jsonTask, "14C003", "14C004", true).getStructureAreaNum();

        // D所有血管轮廓外的红细胞轮廓面积之和
        BigDecimal extravascularErythrocyteArea = commonJsonParser.getInsideOrOutside(jsonTask, "14C003", "14C004", false).getStructureAreaNum();
        //E肺泡上皮细胞核数量总个数
        Integer count = commonJsonParser.getOrganAreaCount(jsonTask, "14C006");
        // F所有轮廓面积之和
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        String accurateArea = singleSlide.getArea();
//        BigDecimal outlineArea = commonJsonParser.getOrganArea(jsonTask, "14C111").getStructureAreaNum();
        BigDecimal outlineArea = new BigDecimal(accurateArea);

        // 1支气管面积占比 A/F
        BigDecimal bronchiAreas = commonJsonParser.getProportion(bronchiArea, outlineArea);

        // 2 血管面积占比 B/F
        BigDecimal vesselAreas = commonJsonParser.getProportion(vesselArea, outlineArea);

        // 3 血管内红细胞面积占比 3=C/F
        BigDecimal intravascularErythrocyteAreas = commonJsonParser.getProportion(intravascularErythrocyteArea, outlineArea);

        // 4血管外红细胞面积占比 4=D/F
        BigDecimal extravascularErythrocyteAreas = commonJsonParser.getProportion(extravascularErythrocyteArea, outlineArea);

        //5 肺泡上皮细胞核密度 5=E/F
        //Double density = count / Double.parseDouble(outlineArea);
        Double density = count / outlineArea.doubleValue();
        //指标
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();

        indicatorResultsMap.put("支气管面积", new IndicatorAddIn("支气管面积", String.valueOf(bronchiArea.setScale(3, BigDecimal.ROUND_HALF_UP)), SQ_MM, CommonConstant.NUMBER_1, "14C002"));
        indicatorResultsMap.put("血管面积", new IndicatorAddIn("血管面积", String.valueOf(vesselArea.setScale(3, BigDecimal.ROUND_HALF_UP)), SQ_MM, CommonConstant.NUMBER_1, "14C003"));
        indicatorResultsMap.put("血管内红细胞面积", new IndicatorAddIn("血管内红细胞面积", String.valueOf(intravascularErythrocyteArea.setScale(3, BigDecimal.ROUND_HALF_UP)), SQ_MM, CommonConstant.NUMBER_1, "14C003,14C004"));
        indicatorResultsMap.put("血管外红细胞面积", new IndicatorAddIn("血管外红细胞面积", String.valueOf(extravascularErythrocyteArea.setScale(3, BigDecimal.ROUND_HALF_UP)), SQ_MM, CommonConstant.NUMBER_1, "14C003,14C004"));
        indicatorResultsMap.put("肺泡上皮细胞核数量", new IndicatorAddIn("肺泡上皮细胞核数量", String.valueOf(count), PIECE, CommonConstant.NUMBER_1, "14C006"));

        indicatorResultsMap.put("支气管面积占比", new IndicatorAddIn("Bronchi area%", String.valueOf(bronchiAreas), PERCENTAGE, CommonConstant.NUMBER_0, "14C002,14C111"));
        indicatorResultsMap.put("血管面积占比", new IndicatorAddIn("Vessel area%", String.valueOf(vesselAreas), PERCENTAGE, CommonConstant.NUMBER_0, "14C003,14C111"));
        indicatorResultsMap.put("血管内红细胞面积占比", new IndicatorAddIn("Intravascular erythrocyte area%", String.valueOf(intravascularErythrocyteAreas), PERCENTAGE, CommonConstant.NUMBER_0, "14C003,14C004,14C111"));
        indicatorResultsMap.put("血管外红细胞面积占比", new IndicatorAddIn("Extravascular erythrocyte area%", String.valueOf(extravascularErythrocyteAreas), PERCENTAGE, CommonConstant.NUMBER_0, "14C003,14C004,14C111"));
        indicatorResultsMap.put("肺泡上皮细胞核密度", new IndicatorAddIn("Nucleus density of alveolar epithelial cell", String.valueOf(new BigDecimal(density).setScale(3, BigDecimal.ROUND_HALF_UP)), SQ_MM_PIECE, "14C006,14C111"));
        indicatorResultsMap.put("肺脏面积", new IndicatorAddIn("Lung area", String.valueOf(outlineArea.setScale(3, BigDecimal.ROUND_HALF_UP)), SQ_MM, CommonConstant.NUMBER_0, "14C111"));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Lung";
    }
}
