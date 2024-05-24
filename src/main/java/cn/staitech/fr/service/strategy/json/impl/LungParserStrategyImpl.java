package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service("Lung")
public class LungParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    private AiForecastService aiForecastService;

    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private CommonJsonParser commonJsonParser;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        log.info("LungParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {

        // 查询精细轮廓面积
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        String accurateArea = singleSlide.getArea();

        Integer count = commonJsonParser.getOrganAreaCount(jsonTask, "14C006");

        //肺泡上皮细胞核数量
        Double density = count / Double.parseDouble(accurateArea);

        // 查询支气管面积
        BigDecimal bronchiArea = commonJsonParser.getOrganArea(jsonTask, "14C002").getStructureAreaNum();

        // 查询血管面积
        BigDecimal vesselArea = commonJsonParser.getOrganArea(jsonTask, "14C003").getStructureAreaNum();

        // 查询血管内红细胞面积
//        BigDecimal intravascularErythrocyteArea = commonJsonParser.getInsideOrOutside(jsonTask,"14C003","14C004",true).getStructureAreaNum();
//
//        // 查询血管外红细胞面积
//        BigDecimal extravascularErythrocyteArea = commonJsonParser.getInsideOrOutside(jsonTask,"14C003","14C004",false).getStructureAreaNum();

        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        indicatorResultsMap.put("肺脏面积", new IndicatorAddIn("Lung area", accurateArea, "平方毫米"));
        indicatorResultsMap.put("肺泡上皮细胞核密度", new IndicatorAddIn("Nucleus density of alveolar epithelial cell", String.valueOf(density), "个/平方毫米"));

        indicatorResultsMap.put("支气管面积", new IndicatorAddIn("支气管面积", String.valueOf(bronchiArea), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("血管面积", new IndicatorAddIn("血管面积", String.valueOf(vesselArea), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("肺泡上皮细胞核数量", new IndicatorAddIn("肺泡上皮细胞核数量", String.valueOf(density), "个", CommonConstant.NUMBER_1));
//        indicatorResultsMap.put("血管内红细胞面积", new IndicatorAddIn("血管内红细胞面积", String.valueOf(intravascularErythrocyteArea), "平方毫米", "1"));
//        indicatorResultsMap.put("血管外红细胞面积", new IndicatorAddIn("血管外红细胞面积", String.valueOf(extravascularErythrocyteArea), "平方毫米", "1"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Lung";
    }
}
