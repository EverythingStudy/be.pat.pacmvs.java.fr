package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.Annotation;
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
 * @Description: Json Parser 大鼠大脑 Brain BR1_BR2
 */
@Slf4j
@Component("Brain")
public class BrainParserStrategyImpl implements ParserStrategy {
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
        log.info("大鼠大脑 BR1_BR2 指标计算开始……{}", jsonTask);
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();

        //        大脑
        //        结构	编码
        //        脉络丛	13209C(有数据)
        //        血管	132003（暂无数据!!!!!!）
        //        红细胞	132004(有数据)
        //        组织轮廓	132111

        //        算法输出指标	指标代码（仅限本文档）	单位（保留小数点后3位）	备注
        //        脉络丛面积	A	平方毫米	无
        Annotation choroidOPlexusAreaAnnotation = commonJsonParser.getOrganArea(jsonTask, "13209C");
        // TODO:需先判断血管与红细胞的关系，再进行面积计算，本期暂不计算
        //        血管外红细胞面积	B	103平方微米	无
        //        血管内红细胞面积	C	平方毫米	无
        //        大脑面积	D	平方毫米	无

        //        产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后3位）	English	计算方式	备注
        //        脉络丛面积占比	1	%	Choroid Plexus area %	1=A/D	无
        //        血管外红细胞面积占比	2	%	Extravascular Erythrocyte area%	2=B/D	无
        //        血管内红细胞面积占比	3	%	Intravascular Erythrocyte area%	3=C/D	无
        //        大脑面积	4	平方毫米	Brain area	4=D	无

        // D:精细轮廓总面积（大鼠大脑）- 平方毫米
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        String accurateArea = singleSlide.getArea();

        indicatorResultsMap.put("脉络丛面积", new IndicatorAddIn("Choroid Plexus area", choroidOPlexusAreaAnnotation.getArea(), "平方毫米"));
        indicatorResultsMap.put("血管外红细胞面积", new IndicatorAddIn("Extravascular Erythrocyte area", "", "10³平方微米"));
        indicatorResultsMap.put("血管内红细胞面积", new IndicatorAddIn("Intravascular Erythrocyte area", "", "平方毫米"));
        indicatorResultsMap.put("大脑面积", new IndicatorAddIn("Brain area", accurateArea, "平方毫米"));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
