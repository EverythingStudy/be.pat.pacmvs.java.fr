package cn.staitech.fr.service.strategy.json.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.AnnotationMapper;
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
 * @author:
 * @create: 2024-05-10 14:18:48
 * @Description: Coagulating_glang Json Parser 大鼠凝固腺
 */
@Slf4j
@Component("Coagulating_glang")
public class CoagulatingGlangParserStrategyImpl implements ParserStrategy {

    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private AnnotationMapper annotationMapper;
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
        // 查询所有未被删除且登录机构相同的数据
        Map<String, Long> pathologicalMap = commonJsonParser.getPathologicalMap(jsonTask.getOrganizationId());
        Long sequenceNumber = commonJsonParser.getSequenceNumber(jsonTask.getSpecialId());
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        indicatorResultsMap.put("大鼠凝固腺面积", new IndicatorAddIn("Coagulating gland area", singleSlide.getArea(), "平方毫米"));
        Annotation annotation = new Annotation();
        annotation.setSingleSlideId(jsonTask.getSingleId());
        // annotation.setSlideId(jsonTask.getSlideId());
        annotation.setCategoryId(pathologicalMap.get("12B074"));
        annotation.setSequenceNumber(sequenceNumber);
        Annotation structureArea = annotationMapper.getStructureArea(annotation);
        indicatorResultsMap.put("腺上皮面积（全片）", new IndicatorAddIn("Acinar epithelial area (all)", ObjectUtil.isNotEmpty(structureArea) ? structureArea.getArea() : "0", "平方毫米"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
