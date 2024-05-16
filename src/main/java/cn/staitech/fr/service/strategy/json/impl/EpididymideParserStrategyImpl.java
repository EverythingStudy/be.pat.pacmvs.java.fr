package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;


/**
 * 附睾-EP
 */
@Slf4j
@Service("Epididymide")
public class EpididymideParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.info("EpididymideParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        /*
        indicatorResultsMap.put("输出小管和附睾管面积占比（全片）", new IndicatorAddIn("Efferent ducts and epididymal ducts area%（all）", "", "%"));
        indicatorResultsMap.put("间质面积占比", new IndicatorAddIn("Mesenchyme area%", "", "%"));
        indicatorResultsMap.put("黏膜上皮面积占比（单个）", new IndicatorAddIn("Mucosal epithelium area% (per)", "", "%"));
        indicatorResultsMap.put("精子面积占比（单个）", new IndicatorAddIn("Sperm area% (per)", "", "%"));
        indicatorResultsMap.put("精子面积占比（全片）", new IndicatorAddIn("Sperm area% (all)", "", "%"));
        indicatorResultsMap.put("黏膜上皮细胞核密度（单个）", new IndicatorAddIn("Mucosal epithelial nucleus% (per)", "", "个/毫米"));
        indicatorResultsMap.put("血管相对面积", new IndicatorAddIn("Vessel area%", "", "%"));
        indicatorResultsMap.put("黏膜上皮厚度（单个）", new IndicatorAddIn("Average thickness of mucosal epithelium (per)", "", "微米"));
        */
        AreaUtils areaUtils = new AreaUtils();

        // J组织轮廓面积-平方毫米
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());

        indicatorResultsMap.put("附睾面积", new IndicatorAddIn("Epididymal area", slideArea, "平方毫米"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Epididymide";
    }
}
