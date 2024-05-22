package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


/**
 *肠系膜淋巴结-MN
 */
@Slf4j
@Service("Mesenteric_lymph_node")
public class MesentericLymphNodeParserStrategyImpl extends AbstractCustomParserStrategy {
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private AreaUtils areaUtils;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        log.info("MesentericLymphNodeParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();

        // 获取各种指标
        Integer areaCount = areaUtils.getOrganAreaCount(jsonTask, "146051");// A生发中心数量
        BigDecimal organAreaB = areaUtils.getOrganArea(jsonTask, "146051");// B生发中心面积（全片）
        BigDecimal organAreaC = areaUtils.getOrganArea(jsonTask, "14603E");// C髓质面积
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());// D组织轮廓

        // 算法输出指标
        indicatorResultsMap.put("生发中心面积（全片）", new IndicatorAddIn("", organAreaB.toString(), "平方毫米", "1"));
        indicatorResultsMap.put("髓质面积", new IndicatorAddIn("", organAreaC.toString(), "平方毫米", "1"));

        // 产品呈现指标
        indicatorResultsMap.put("淋巴结面积", new IndicatorAddIn("Submadibular gland area", slideArea, "平方毫米"));
        indicatorResultsMap.put("生发中心数量", new IndicatorAddIn("Number of germinal center", areaCount.toString(), "个"));
        /*
        indicatorResultsMap.put("生发中心占比", new IndicatorAddIn("Germinal center area%", "", "%"));
        indicatorResultsMap.put("髓质占比", new IndicatorAddIn("Medulla area%", "", "%"));
        indicatorResultsMap.put("皮质和副皮质占比", new IndicatorAddIn("Cortex and paracortex area%", "", "%"));
        */

        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Mesenteric_lymph_node";
    }
}
