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
        Map<String, IndicatorAddIn> resultsMap = new HashMap<>();

        // 获取各种指标
        Integer organAreaCountA = areaUtils.getOrganAreaCount(jsonTask, "10B125");// A颗粒管（红色）数量
        Integer organAreaCountB = areaUtils.getOrganAreaCount(jsonTask, "10B128");// B黏液腺细胞核数量
        BigDecimal organAreaD = areaUtils.getOrganArea(jsonTask, "10B003");// D有血管壁的血管面积
        Integer organAreaCountE = areaUtils.getOrganAreaCount(jsonTask, "10B003");// E有血管壁的血管数量
        BigDecimal organAreaF = areaUtils.getOrganArea(jsonTask, "10B004");// F红细胞面积
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId()); // H组织轮廓
        BigDecimal organAreaI = areaUtils.getOrganArea(jsonTask, "10B125");// I颗粒管（红色）面积（全片）
        // todo C颗粒管细胞核数量（单个）10B126

        // 算法输出指标
        resultsMap.put("颗粒管（红色）数量", createIndicator(organAreaCountA, PIECE));
        resultsMap.put("黏液腺细胞核数量", createIndicator(organAreaCountB, PIECE));
        resultsMap.put("有血管壁的血管面积", createIndicator(organAreaD, SQ_MM));
        resultsMap.put("有血管壁的血管数量", createIndicator(organAreaCountE, PIECE));
        resultsMap.put("红细胞面积", createIndicator(organAreaF, SQ_MM));
        resultsMap.put("颗粒管（红色）面积（单个）", createDefaultIndicator());// G颗粒管（红色）面积（单个）
        resultsMap.put("颗粒管（红色）面积（全片）", createIndicator(organAreaI, SQ_MM));// I颗粒管（红色）面积（全片）

        // 计算指标
        BigDecimal densityResult = getDensityResult(organAreaCountA, slideArea);// A/H
        BigDecimal nucleusResult = getDensityResult(organAreaCountB, slideArea);// B/H

        // 产品呈现指标
        resultsMap.put("颌下腺面积", createNameIndicator("Submadibular gland area", slideArea, SQ_MM));
        resultsMap.put("颗粒管（红色）密度", createNameIndicator("Density of granular convoluted tubules (eosinophilic)", densityResult, SQ_MM_PIECE));
        resultsMap.put("黏液腺细胞核密度", createNameIndicator("Nucleus density of mucous gland", nucleusResult, SQ_MM_PIECE));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), resultsMap);
    }

    /**
     * 计算指标
     * @return organAreaCount/slideArea结果
     */
    private BigDecimal getDensityResult(Integer organAreaCount, String slideArea) {
        return (0 == organAreaCount) ? BigDecimal.ZERO
                : new BigDecimal(organAreaCount).divide(new BigDecimal(slideArea), 3, RoundingMode.HALF_UP);
    }

    @Override
    public String getAlgorithmCode() {
        return "Mangbular_gland";
    }
}
