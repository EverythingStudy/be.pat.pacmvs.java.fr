package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
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
 * 睾丸-TE
 */
@Slf4j
@Service("Testis")
public class TestisParserStrategyImpl extends AbstractCustomParserStrategy {
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private AreaUtils areaUtils;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        log.info("TestisParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> resultsMap = new HashMap<>();

        // 获取各种指标
        BigDecimal organAreaB = areaUtils.getOrganArea(jsonTask, "12E0FA");// B生精小管面积（全片）
        Annotation annotation = commonJsonParser.getOrganArea(jsonTask, "12E0FA");// C生精小管周长（单个）
        BigDecimal perimeterC = annotation.getStructurePerimeterNum();
        Integer areaCountD = areaUtils.getOrganAreaCount(jsonTask, "12E0FA");// D生精小管数量
        Integer areaCountH = areaUtils.getOrganAreaCount(jsonTask, "12E0FE");// H间质细胞核数量
        BigDecimal organAreaI = areaUtils.getOrganArea(jsonTask, "12E003");// I血管面积
        String slideAreaJ = areaUtils.getFineContourArea(jsonTask.getSingleId());// J组织轮廓
        // todo F生精细胞核数量（单个）
        // todo G支持细胞核数量（单个）

        // 算法输出指标
        resultsMap.put("生精小管面积（全片）", createIndicator(organAreaB, SQ_MM));
        resultsMap.put("生精小管周长（单个）", createIndicator(perimeterC, MM));
        resultsMap.put("生精小管数量", createIndicator(areaCountD, PIECE));
        resultsMap.put("间质细胞核数量", createIndicator(areaCountH, PIECE));
        resultsMap.put("血管面积", createIndicator(organAreaI, SQ_MM));
        resultsMap.put("生精小管面积（单个）", createDefaultIndicator());// A生精小管面积（单个）
        resultsMap.put("生精小管内腔面积（单个）", createDefaultIndicator());// E生精小管内腔面积（单个）

        // 计算指标
        BigDecimal densityResult = getDensityResult(areaCountD, slideAreaJ);

        // 产品呈现指标
        resultsMap.put("睾丸面积", createNameIndicator("Testicular area", slideAreaJ, SQ_MM));
        resultsMap.put("生精小管密度", createNameIndicator("Density of seminiferous tubules", densityResult, SQ_UM_THOUSAND));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), resultsMap);
    }

    /**
     * 生精小管密度计算
     */
    private BigDecimal getDensityResult(Integer areaCountD, String slideAreaJ) {
        BigDecimal areaCountBD = new BigDecimal(areaCountD);
        BigDecimal slideAreaBD = new BigDecimal(slideAreaJ);
        BigDecimal densityResult;
        if (areaCountBD.compareTo(BigDecimal.ZERO) == 0 || slideAreaBD.compareTo(BigDecimal.ZERO) == 0) {
            densityResult = BigDecimal.ZERO;
        } else {
            densityResult = areaCountBD.divide(slideAreaBD, 3, RoundingMode.HALF_UP);// D/J
        }
        return densityResult;
    }

    @Override
    public String getAlgorithmCode() {
        return "Testis";
    }
}
