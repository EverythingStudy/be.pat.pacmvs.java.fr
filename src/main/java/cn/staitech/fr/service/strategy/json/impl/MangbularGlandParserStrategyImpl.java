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
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;


/**
 * 颌下腺-MD
 */
@Slf4j
@Service("Mangbular_gland")
public class MangbularGlandParserStrategyImpl extends AbstractCustomParserStrategy {
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private AreaUtils areaUtils;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        log.info("MangbularGlandParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();

        // A颗粒管（红色）数量-个
        Integer organAreaCountA = areaUtils.getOrganAreaCount(jsonTask, "10B125");
        // B黏液腺细胞核数量-个
        Integer organAreaCountB = areaUtils.getOrganAreaCount(jsonTask, "10B128");
        // todo C颗粒管细胞核数量（单个）10B126
        // D有血管壁的血管面积-平方毫米
        BigDecimal organAreaD = areaUtils.getOrganArea(jsonTask, "10B003");
        // E有血管壁的血管数量-个
        Integer organAreaCountE = areaUtils.getOrganAreaCount(jsonTask, "10B003");
        // F红细胞面积-平方毫米
        BigDecimal organAreaF = areaUtils.getOrganArea(jsonTask, "10B004");
        // todo G颗粒管（红色）面积（单个）10B125
        // H组织轮廓-平方毫米
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());
        // I颗粒管（红色）面积（全片）-平方毫米
        BigDecimal organAreaI = areaUtils.getOrganArea(jsonTask, "10B125");

        // 算法输出指标
        indicatorResultsMap.put("颗粒管（红色）数量", new IndicatorAddIn("", organAreaCountA.toString(), "个", "1"));
        indicatorResultsMap.put("黏液腺细胞核数量", new IndicatorAddIn("", organAreaCountB.toString(), "个", "1"));
        indicatorResultsMap.put("有血管壁的血管面积", new IndicatorAddIn("", organAreaD.toString(), "平方毫米", "1"));
        indicatorResultsMap.put("有血管壁的血管数量", new IndicatorAddIn("", organAreaCountE.toString(), "个", "1"));
        indicatorResultsMap.put("红细胞面积", new IndicatorAddIn("", organAreaF.toString(), "平方毫米", "1"));
        indicatorResultsMap.put("颗粒管（红色）面积（全片）", new IndicatorAddIn("", organAreaI.toString(), "平方毫米", "1"));
        /*
        indicatorResultsMap.put("颗粒管细胞核数量（单个）", new IndicatorAddIn("", "", "个", "1"));
        indicatorResultsMap.put("颗粒管（红色）面积（单个）", new IndicatorAddIn("", "", "10³平方微米", "1"));
         */

        // A/H颗粒管（红色）密度
        BigDecimal densityResult = (0 == organAreaCountA) ? BigDecimal.ZERO
                : new BigDecimal(organAreaCountA).divide(new BigDecimal(slideArea), 3, RoundingMode.HALF_UP);
        // B/H黏液腺细胞核密度
        BigDecimal nucleusResult = (0 == organAreaCountB) ? BigDecimal.ZERO
                : new BigDecimal(organAreaCountB).divide(new BigDecimal(slideArea), 3, RoundingMode.HALF_UP);

        // 产品呈现指标
        indicatorResultsMap.put("颌下腺面积", new IndicatorAddIn("Submadibular gland area", slideArea, "平方毫米"));
        indicatorResultsMap.put("颗粒管（红色）密度", new IndicatorAddIn("Density of granular convoluted tubules (eosinophilic)", densityResult.toString(), "个/平方毫米"));
        indicatorResultsMap.put("黏液腺细胞核密度", new IndicatorAddIn("Nucleus density of mucous gland", nucleusResult.toString(), "个/平方毫米"));
         /*
        indicatorResultsMap.put("颗粒管细胞核密度(单个颗粒管)", new IndicatorAddIn("Nucleus density of granular convoluted tubule (per)", "", "个/平方毫米"));
        indicatorResultsMap.put("血管面积占比", new IndicatorAddIn("Vessel area%", "", "%"));
        indicatorResultsMap.put("红细胞面积占比", new IndicatorAddIn("Erythrocyte area%", "", "%"));
        indicatorResultsMap.put("颗粒管面积占比（全片）", new IndicatorAddIn("Granular convoluted tubules area% (all)", "", "%"));
        */

        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Mangbular_gland";
    }
}
