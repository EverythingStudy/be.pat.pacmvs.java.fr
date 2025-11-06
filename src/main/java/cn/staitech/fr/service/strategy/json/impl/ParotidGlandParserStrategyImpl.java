package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
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
 * @author wanglibei
 * @version V1.0
 * @ClassName: ParotidGlandParserStrategyImpl
 * @Description-d:腮腺
 * @date 2025年7月21日
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
    @Resource
    private CommonJsonCheck commonJsonCheck;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("ParotidGlandParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> resultsMap = new HashMap<>();

        // 获取各种指标
        // A腺泡细胞核数量
        Integer areaCountA = areaUtils.getOrganAreaCount(jsonTask, "10906E");
        // B导管面积
        BigDecimal organAreaB = areaUtils.getOrganArea(jsonTask, "10906F");
        // C血管面积
        BigDecimal organAreaC = areaUtils.getOrganArea(jsonTask, "109003");
        // D组织轮廓
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());

        // 算法输出指标
        resultsMap.put("腺泡细胞核数量", createIndicator(areaCountA, PIECE, "10906E"));
        resultsMap.put("导管面积", createIndicator(areaUtils.convertToSquareMicrometer(organAreaB.toString()), SQ_UM_THOUSAND, "10906F"));
        resultsMap.put("血管面积", createIndicator(areaUtils.convertToSquareMicrometer(organAreaC.toString()), SQ_UM_THOUSAND, "109003"));

        // 计算指标
        //1 腺泡细胞核密度 个/mm2 1=A/D
        BigDecimal nucleusResult = getNucleusResult(areaCountA, slideArea);

        BigDecimal ares = BigDecimal.valueOf(Double.parseDouble(slideArea));

        // 产品呈现指标
        resultsMap.put("腺泡细胞核密度", createNameIndicator("Nucleus density of acinar cell", nucleusResult, SQ_MM_PIECE, areaUtils.getStructureIds("10906E", "109111")));
        resultsMap.put("血管面积占比", createNameIndicator("Vessel area%", getProportion(organAreaC, ares), PERCENTAGE, areaUtils.getStructureIds("109003", "109111")));
        resultsMap.put("导管面积占比", createNameIndicator("Ducts area%", getProportion(organAreaB, ares), PERCENTAGE, areaUtils.getStructureIds("10906F", "109111")));
        resultsMap.put("腮腺面积", createNameIndicator("Parotid gland area", String.valueOf(ares.setScale(3, BigDecimal.ROUND_HALF_UP)), SQ_MM, "109111"));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), resultsMap);
    }

    /**
     * 计算指标
     */
    private static BigDecimal getNucleusResult(Integer areaCountA, String slideArea) {
        return (0 == areaCountA) ? BigDecimal.ZERO : new BigDecimal(areaCountA).divide(new BigDecimal(slideArea), 3, RoundingMode.HALF_UP);
    }

    @Override
    public String getAlgorithmCode() {
        return "Parotid_gland";
    }
}
