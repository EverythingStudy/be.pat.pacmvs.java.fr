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
 * @Description: Json Parser 大鼠肝脏 Liver LI
 */
@Slf4j
@Component("Liver")
public class LiverParserStrategyImpl implements ParserStrategy {
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
        log.info("大鼠肝脏指标计算开始…… {}", jsonTask);
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();

        // H:精细轮廓总面积（肝脏面积）-平方毫米
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        String accurateArea = singleSlide.getArea();

        // 肝细胞核数量 D 个 肝细胞核	112149
        Integer count = commonJsonParser.getOrganAreaCount(jsonTask, "112149");

        // 肝细胞核密度 3 = D/H
        Double density = count / Double.parseDouble(accurateArea);

        indicatorResultsMap.put("肝脏面积", new IndicatorAddIn("Liver area", accurateArea, "平方毫米"));
        indicatorResultsMap.put("肝细胞核密度", new IndicatorAddIn("Nucleus density of hepatocyte", density.toString(), "个/平方毫米"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
