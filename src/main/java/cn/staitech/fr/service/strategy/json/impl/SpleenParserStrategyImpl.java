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
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: wangfeng
 * @create: 2024-05-10 14:18:48
 * @Description: Json Parser 大鼠脾脏
 */
@Slf4j
@Component("Spleen")
public class SpleenParserStrategyImpl implements ParserStrategy {

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
        log.info("大鼠甲状腺指标计算开始");
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();

        // H:精细轮廓总面积（甲状腺）-平方毫米
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        String accurateArea = singleSlide.getArea();

        // I:甲状旁腺组织轮廓面积-平方毫米
        BigDecimal organArea = commonJsonParser.getOrganArea(jsonTask, "108111");

        // 若甲状腺轮廓面积里包括了甲状旁腺，计算时需要用H-I，若甲状旁腺和甲状腺是分开单独识别的，则只需要H
        if (new BigDecimal(accurateArea).compareTo(BigDecimal.ZERO) > 0
                && organArea.compareTo(BigDecimal.ZERO) > 0) {
            // H-I
            BigDecimal areaNum = new BigDecimal(accurateArea).subtract(organArea);
            accurateArea = areaNum.toString();
        }

        indicatorResultsMap.put("甲状腺面积", new IndicatorAddIn("Thyroid gland area", accurateArea, "平方毫米"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
