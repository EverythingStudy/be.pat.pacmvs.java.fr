package cn.staitech.fr.service.strategy.json.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
 * 颌下腺-MD
 */
@Slf4j
@Service("Mangbular_gland")
public class MangbularGlandParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.info("MangbularGlandParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        /*
        indicatorResultsMap.put("颗粒管细胞核密度(单个颗粒管)", new IndicatorAddIn("Nucleus density of granular convoluted tubule (per)", "", "个/平方毫米"));
        indicatorResultsMap.put("血管面积占比", new IndicatorAddIn("Vessel area%", "", "%"));
        indicatorResultsMap.put("红细胞面积占比", new IndicatorAddIn("Erythrocyte area%", "", "%"));
        indicatorResultsMap.put("颗粒管面积占比（全片）", new IndicatorAddIn("Granular convoluted tubules area% (all)", "", "%"));
        */
        AreaUtils areaUtils = new AreaUtils();

        // I组织轮廓-平方毫米
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());
        // A颗粒管（红色）数量
        Integer organAreaCountA = areaUtils.getOrganAreaCount(jsonTask, "10B125");
        // B黏液腺细胞核密度
        Integer organAreaCountB = areaUtils.getOrganAreaCount(jsonTask, "10B128");

        // A/I颗粒管（红色）密度
        BigDecimal densityResult = (null == organAreaCountA) ? BigDecimal.ZERO
                : new BigDecimal(organAreaCountA).divide(new BigDecimal(slideArea), 3, RoundingMode.HALF_UP);
        // B/I黏液腺细胞核密度
        BigDecimal nucleusResult = (null == organAreaCountB) ? BigDecimal.ZERO
                : new BigDecimal(organAreaCountB).divide(new BigDecimal(slideArea), 3, RoundingMode.HALF_UP);

        indicatorResultsMap.put("颌下腺面积", new IndicatorAddIn("Submadibular gland area", slideArea, "平方毫米"));
        indicatorResultsMap.put("颗粒管（红色）密度", new IndicatorAddIn("Density of granular convoluted tubules (eosinophilic)", densityResult.toString(), "个/平方毫米"));
        indicatorResultsMap.put("黏液腺细胞核密度", new IndicatorAddIn("Nucleus density of mucous gland", nucleusResult.toString(), "个/平方毫米"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Mangbular_gland";
    }
}
