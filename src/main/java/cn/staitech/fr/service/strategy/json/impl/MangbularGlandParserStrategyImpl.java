package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
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

        // 获取各种指标
        Integer organAreaCountA = areaUtils.getOrganAreaCount(jsonTask, "10B125");// A颗粒管（红色）数量
        Integer organAreaCountB = areaUtils.getOrganAreaCount(jsonTask, "10B128");// B黏液腺细胞核数量
        // todo C颗粒管细胞核数量（单个）10B126
        BigDecimal organAreaD = areaUtils.getOrganArea(jsonTask, "10B003");// D有血管壁的血管面积
        Integer organAreaCountE = areaUtils.getOrganAreaCount(jsonTask, "10B003");// E有血管壁的血管数量
        BigDecimal organAreaF = areaUtils.getOrganArea(jsonTask, "10B004");// F红细胞面积
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId()); // H组织轮廓
        BigDecimal organAreaI = areaUtils.getOrganArea(jsonTask, "10B125");// I颗粒管（红色）面积（全片）

        // 算法输出指标
        indicatorResultsMap.put("颗粒管（红色）数量", new IndicatorAddIn("", organAreaCountA.toString(), "个", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("黏液腺细胞核数量", new IndicatorAddIn("", organAreaCountB.toString(), "个", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("有血管壁的血管面积", new IndicatorAddIn("", organAreaD.toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("有血管壁的血管数量", new IndicatorAddIn("", organAreaCountE.toString(), "个", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("红细胞面积", new IndicatorAddIn("", organAreaF.toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("颗粒管（红色）面积（全片）", new IndicatorAddIn("", organAreaI.toString(), "平方毫米", CommonConstant.NUMBER_1));
        //indicatorResultsMap.put("颗粒管细胞核数量（单个）", new IndicatorAddIn("", "", "个", "1"));
        indicatorResultsMap.put("颗粒管（红色）面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1));

        // 计算指标
        BigDecimal densityResult = (0 == organAreaCountA) ? BigDecimal.ZERO
                : new BigDecimal(organAreaCountA).divide(new BigDecimal(slideArea), 3, RoundingMode.HALF_UP);// A/H

        BigDecimal nucleusResult = (0 == organAreaCountB) ? BigDecimal.ZERO
                : new BigDecimal(organAreaCountB).divide(new BigDecimal(slideArea), 3, RoundingMode.HALF_UP); // B/H

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
