package cn.staitech.fr.service.strategy.json.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.ImageMapper;
import cn.staitech.fr.mapper.PathologicalIndicatorCategoryMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.service.strategy.json.CommonParserStrategy;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;


/**
 * 骨骼肌
 */
@Slf4j
@Service("Muscle")
public class MuscleParserStrategyImpl extends AbstractCustomParserStrategy {
    @Resource
    private SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private PathologicalIndicatorCategoryMapper pathologicalIndicatorCategoryMapper;
    @Resource
    private AnnotationMapper annotationMapper;
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private ImageMapper imageMapper;

    @Resource
    private CommonParserStrategy commonParserStrategy;
    @Resource
    private CommonJsonParser commonJsonParser;
    
    @PostConstruct
    public void init() {
    	setCommonJsonParser(commonJsonParser);
        log.info("MuscleParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        /*
        indicatorResultsMap.put("肌纤维面积（单个）", new IndicatorAddIn("Muscle fiber area (per)", "", ""));
        indicatorResultsMap.put("间质面积占比", new IndicatorAddIn("Mesenchyme area %", "", ""));
        indicatorResultsMap.put("血管面积占比", new IndicatorAddIn("Vessel area%", "", ""));
        indicatorResultsMap.put("血管内红细胞面积占比", new IndicatorAddIn("Intravascular erythrocyte area%", "", ""));
        indicatorResultsMap.put("血管外红细胞面积占比", new IndicatorAddIn("Extravascular erythrocyte area%", "", ""));
        */
        AreaUtils areaUtils = new AreaUtils();

        // F精细轮廓总面积-平方毫米
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());

        // 单位换算
        String result = areaUtils.convertToSquareMicrometer(slideArea);

        indicatorResultsMap.put("骨骼肌面积", new IndicatorAddIn("Skeletal muscle area", result, "10³平方微米"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Muscle";
    }
}
