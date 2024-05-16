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
 * 腮腺-PG
 */
@Slf4j
@Service("Adrenal_gland")
public class ParotidGlandParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.info("ParotidGlandParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        /*
        indicatorResultsMap.put("血管面积占比", new IndicatorAddIn("Vessel area%", "", "%"));
        indicatorResultsMap.put("导管面积占比", new IndicatorAddIn("Ducts area%", "", "%"));
        */

        AreaUtils areaUtils = new AreaUtils();

        // D组织轮廓-平方毫米
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());
        // A腺泡细胞核数量
        Integer areaCount = areaUtils.getOrganAreaCount(jsonTask, "10906E");
        // A/D腺泡细胞核密度
        BigDecimal nucleusResult = (null == areaCount) ? BigDecimal.ZERO
                : new BigDecimal(areaCount).divide(new BigDecimal(slideArea), 3, RoundingMode.HALF_UP);

        indicatorResultsMap.put("腮腺面积", new IndicatorAddIn("Parotid gland area", slideArea, "平方毫米"));
        indicatorResultsMap.put("腺泡细胞核密度", new IndicatorAddIn("Nucleus density of acinar cell", nucleusResult.toString(), "个/平方毫米"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Adrenal_gland";
    }
}
