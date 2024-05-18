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
 * @Description: Json Parser 大鼠小脑
 */
@Slf4j
@Component("Cerebellum")
public class CerebellumParserStrategyImpl implements ParserStrategy {

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
        log.info("小脑指标计算开始…… {}", jsonTask);
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        indicatorResultsMap.put("小脑和脑干面积", new IndicatorAddIn("Cerebellum and Brainstem area", singleSlide.getArea(), "平方毫米"));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
