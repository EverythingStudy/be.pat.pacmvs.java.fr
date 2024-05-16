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
 * 膀胱
 */
@Slf4j
@Service("Urinary_bladder")
public class UrinaryBladderParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.info("UrinaryBladderParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        /*
        indicatorResultsMap.put("黏膜上皮面积占比", new IndicatorAddIn("Mucosa epithelium area %", "", ""));
        indicatorResultsMap.put("黏膜固有层和黏膜下层面积占比", new IndicatorAddIn("Lamina propria and submucosa area %", "", ""));
        indicatorResultsMap.put("黏膜上皮细胞核密度", new IndicatorAddIn("Nucleus density of mucosal epithelial nucleus", "", ""));
        indicatorResultsMap.put("血管面积占比", new IndicatorAddIn("Vessel area %", "", ""));
        indicatorResultsMap.put("血管外红细胞面积占比", new IndicatorAddIn("Extravascular erythrocyte area%", "", ""));
        indicatorResultsMap.put("血管内红细胞面积占比", new IndicatorAddIn("Intravascular erythrocyte area%", "", ""));
        */
        AreaUtils areaUtils = new AreaUtils();

        // B 精细轮廓总面积-平方毫米
        String accurateArea = areaUtils.getFineContourArea(jsonTask.getSingleId());
        // A 膀胱腔面积-平方毫米
        BigDecimal organArea = areaUtils.getOrganArea(jsonTask,"11E034");
        // 膀胱面积 B-A
        BigDecimal areaNum = new BigDecimal(accurateArea).subtract(organArea);
        String result = areaNum.setScale(3, RoundingMode.HALF_UP).toString();

        indicatorResultsMap.put("膀胱面积", new IndicatorAddIn("Urinary bladder area", result, "平方毫米"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Urinary_bladder";
    }
}
