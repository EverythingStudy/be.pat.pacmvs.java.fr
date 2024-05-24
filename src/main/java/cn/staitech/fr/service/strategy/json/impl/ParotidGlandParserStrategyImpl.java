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
 * 腮腺-PG
 */
@Slf4j
@Service("Parotid_gland")
public class ParotidGlandParserStrategyImpl extends AbstractCustomParserStrategy {
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private AreaUtils areaUtils;
    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        log.info("ParotidGlandParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> resultsMap = new HashMap<>();

        // 获取各种指标
        Integer areaCountA = areaUtils.getOrganAreaCount(jsonTask, "10906E");// A腺泡细胞核数量
        BigDecimal organAreaB = areaUtils.getOrganArea(jsonTask, "10906F");// B导管面积
        BigDecimal organAreaC = areaUtils.getOrganArea(jsonTask, "109003");// C血管面积
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());// D组织轮廓

        // 算法输出指标
        resultsMap.put("腺泡细胞核数量", createIndicator(areaCountA.toString(), PIECE));
        resultsMap.put("导管面积", createIndicator(areaUtils.convertToSquareMicrometer(organAreaB.toString()), SQ_UM_THOUSAND));
        resultsMap.put("髓质面积", createIndicator(areaUtils.convertToSquareMicrometer(organAreaC.toString()), SQ_UM_THOUSAND));

        // 计算指标
        BigDecimal nucleusResult = getNucleusResult(areaCountA, slideArea);// A/D

        // 产品呈现指标
        resultsMap.put("腮腺面积", createNameIndicator("Parotid gland area", slideArea, SQ_MM));
        resultsMap.put("腺泡细胞核密度", createNameIndicator("Nucleus density of acinar cell", nucleusResult, SQ_MM_PIECE));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), resultsMap);
    }

    /**
     * 计算指标
     */
    private static BigDecimal getNucleusResult(Integer areaCountA, String slideArea) {
        return (0 == areaCountA) ? BigDecimal.ZERO
                : new BigDecimal(areaCountA).divide(new BigDecimal(slideArea), 3, RoundingMode.HALF_UP);
    }

    @Override
    public String getAlgorithmCode() {
        return "Parotid_gland";
    }
}
