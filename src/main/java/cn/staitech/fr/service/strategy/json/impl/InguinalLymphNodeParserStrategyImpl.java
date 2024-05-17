package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.service.strategy.json.ParserStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: wangfeng
 * @create: 2024-05-10 14:18:48
 * @Description: Json Parser 腹股沟淋巴结 Inguinal lymph node
 */
@Slf4j
@Component("Inguinal_lymph_node")
public class InguinalLymphNodeParserStrategyImpl implements ParserStrategy {
    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;

    @Override
    public void parseJson(JsonTask jsonTask, JsonFile jsonFileS) {
        commonJsonParser.parseJson(jsonTask, jsonFileS);
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("腹股沟淋巴结指标计算开始…… {}", jsonTask);
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();

        // 生发中心数量	1	个	 Number of germinal center	1=A  147051
        Integer count = commonJsonParser.getOrganAreaCount(jsonTask, "147051");

        // 5=D:淋巴结面积-平方毫米
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        String accurateArea = singleSlide.getArea();

        indicatorResultsMap.put("生发中心数量", new IndicatorAddIn(" Number of germinal center", count.toString(), "个"));
        indicatorResultsMap.put("淋巴结面积", new IndicatorAddIn("Lymph node area", accurateArea, "平方毫米"));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
