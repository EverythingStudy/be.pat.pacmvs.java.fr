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
 * 颌下腺-MN
 */
@Slf4j
@Service("Mesenteric_lymph_node")
public class MesentericLymphNodeParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.info("MesentericLymphNodeParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        /*
        indicatorResultsMap.put("生发中心占比", new IndicatorAddIn("Germinal center area%", "", "%"));
        indicatorResultsMap.put("髓质占比", new IndicatorAddIn("Medulla area%", "", "%"));
        indicatorResultsMap.put("皮质和副皮质占比", new IndicatorAddIn("Cortex and paracortex area%", "", "%"));
        */
        AreaUtils areaUtils = new AreaUtils();

        // D组织轮廓-平方毫米
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());
        // A生发中心数量
        Integer areaCount = areaUtils.getOrganAreaCount(jsonTask, "146051");

        indicatorResultsMap.put("淋巴结面积", new IndicatorAddIn("Submadibular gland area", slideArea, "平方毫米"));
        indicatorResultsMap.put("生发中心数量", new IndicatorAddIn("Number of germinal center", areaCount.toString(), "个"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Mesenteric_lymph_node";
    }
}
