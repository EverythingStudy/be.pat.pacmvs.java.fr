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
 * 睾丸-TE
 */
@Slf4j
@Service("Testis")
public class TestisParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.info("TestisParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        /*
        indicatorResultsMap.put("生精小管面积（全片）", new IndicatorAddIn("Seminiferous tubules area (all)", "", "平方毫米"));
        indicatorResultsMap.put("生精小管面积占比", new IndicatorAddIn("Seminiferous tubules area%", "", "%"));
        indicatorResultsMap.put("生精小管面积（单个）", new IndicatorAddIn("Seminiferous tubules area (per)", "", "10³平方微米"));
        indicatorResultsMap.put("生精小管厚度（单个）", new IndicatorAddIn("Average thickness of spermatogenic tubules (per)", "", "微米"));
        indicatorResultsMap.put("生精细胞核密度（单个）", new IndicatorAddIn("Nucleus density of Spermatogenic cells (per)", "", "个/毫米"));
        indicatorResultsMap.put("支持细胞核密度（单个）", new IndicatorAddIn("Nucleus density of Sertoli (per) ", "", "个/毫米"));
        indicatorResultsMap.put("生精细胞核：支持细胞核（单个）", new IndicatorAddIn("Spermatogenic nucleus:  Sertoli nucleus ratio (per)", "", ""));
        indicatorResultsMap.put("血管面积占比", new IndicatorAddIn("Vessel area%", "", "%"));
        indicatorResultsMap.put("间质细胞核：生精小管", new IndicatorAddIn("Leydig nucleus: seminiferous tubules ratio", "", ""));
        indicatorResultsMap.put("间质面积占比", new IndicatorAddIn("Mesenchyme area%", "", "%"));
        indicatorResultsMap.put("间质细胞核密度", new IndicatorAddIn("Nucleus density of leydig cells", "", "个/平方毫米"));
        */
        AreaUtils areaUtils = new AreaUtils();

        // J组织轮廓-平方毫米
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());
        // D生精小管数量
        Integer areaCount = areaUtils.getOrganAreaCount(jsonTask, "12E0FA");
        // D/J生精小管密度
        BigDecimal densityResult = (null == areaCount) ? BigDecimal.ZERO
                : new BigDecimal(areaCount).divide(new BigDecimal(slideArea), 3, RoundingMode.HALF_UP);

        indicatorResultsMap.put("睾丸面积", new IndicatorAddIn("Testicular area", slideArea, "平方毫米"));
        indicatorResultsMap.put("生精小管密度", new IndicatorAddIn("Density of seminiferous tubules", densityResult.toString(), "个/平方毫米"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Testis";
    }
}
