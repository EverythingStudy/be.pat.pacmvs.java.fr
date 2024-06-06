package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
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
@Service("Trachea")
public class TracheaParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    private AiForecastService aiForecastService;

    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private CommonJsonParser commonJsonParser;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        log.info("TracheaParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        // 查询精细轮廓面积
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        String accurateArea = singleSlide.getArea();

        // 查询气管腔面积
        BigDecimal organArea = commonJsonParser.getOrganArea(jsonTask, "14D007").getStructureAreaNum();

        // 查询黏膜上皮层
        Annotation annotation = commonJsonParser.getOrganArea(jsonTask, "14D035");

        // 获取黏膜上皮层面积
        BigDecimal mucosaArea = annotation.getStructureAreaNum();

        // 获取黏膜上皮层周长
        BigDecimal mucosaPerimeter = annotation.getStructurePerimeterNum();

        // 获取黏膜上皮细胞核数量
        Integer mucosaCount = commonJsonParser.getOrganAreaCount(jsonTask, "14D036");

        // 查询软骨面积
        BigDecimal cartilageArea = commonJsonParser.getOrganArea(jsonTask, "14D00B").getStructureAreaNum();

        // 使用组织轮廓面积减去气管腔面积
        BigDecimal areaNum = new BigDecimal(accurateArea).subtract(organArea);

        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        indicatorResultsMap.put("气管面积", new IndicatorAddIn("Tracheal area", String.valueOf(areaNum), "平方毫米"));
        indicatorResultsMap.put("气管腔面积", new IndicatorAddIn("气管腔面积", String.valueOf(organArea), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("黏膜上皮层面积", new IndicatorAddIn("黏膜上皮层面积", String.valueOf(mucosaArea), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("黏膜上皮层周长", new IndicatorAddIn("黏膜上皮层周长", String.valueOf(mucosaPerimeter), "毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("黏膜上皮层周长(单个)", createDefaultIndicator());
        indicatorResultsMap.put("黏膜上皮细胞核数量", new IndicatorAddIn("黏膜上皮细胞核数量", String.valueOf(mucosaCount), "个", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("软骨面积", new IndicatorAddIn("软骨面积", String.valueOf(cartilageArea), "平方毫米", CommonConstant.NUMBER_1));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Trachea";
    }
}

