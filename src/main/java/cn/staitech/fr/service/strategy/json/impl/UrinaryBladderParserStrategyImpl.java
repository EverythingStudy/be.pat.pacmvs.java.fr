package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.*;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.utils.AreaUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @PostConstruct
    public void init() {
        setAiForecastService(aiForecastService);
        setAnnotationMapper(annotationMapper);
        setPathologicalIndicatorCategoryMapper(pathologicalIndicatorCategoryMapper);
        setSingleSlideMapper(singleSlideMapper);
        setSpecialAnnotationRelMapper(specialAnnotationRelMapper);
        setImageMapper(imageMapper);
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

        // B 精细轮廓总面积-平方毫米
        String accurateArea = new AreaUtils().getFineContourArea(jsonTask.getSingleId());

        // A 膀胱腔面积-平方毫米
        BigDecimal organArea = new AreaUtils().getOrganArea(jsonTask,"11E034");

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
