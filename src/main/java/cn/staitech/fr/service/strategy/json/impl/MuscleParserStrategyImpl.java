package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;


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

    @PostConstruct
    public void init() {
        setAiForecastService(aiForecastService);
        setAnnotationMapper(annotationMapper);
        setPathologicalIndicatorCategoryMapper(pathologicalIndicatorCategoryMapper);
        setSingleSlideMapper(singleSlideMapper);
        setSpecialAnnotationRelMapper(specialAnnotationRelMapper);
        setImageMapper(imageMapper);
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

        // F精细轮廓总面积-平方毫米
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        String slideArea = singleSlide.getArea();

        // 单位换算
        BigDecimal areaNum = new BigDecimal(slideArea).multiply(new BigDecimal(1000));
        String result = areaNum.setScale(3, RoundingMode.HALF_UP).toString();

        indicatorResultsMap.put("骨骼肌面积", new IndicatorAddIn("Skeletal muscle area", result, "10³平方微米"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Muscle";
    }
}
