package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
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
 * 附睾-EP
 */
@Slf4j
@Service("Epididymide")
public class EpididymideParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.info("EpididymideParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn>  resultsMap = new HashMap<>();

        // 获取各种指标
        BigDecimal organAreaB = areaUtils.getOrganArea(jsonTask, "12F0F5");// B输出小管/附睾管黏膜上皮面积（全片）
        Annotation annotation = commonJsonParser.getOrganArea(jsonTask, "12F0F5");// C输出小管/附睾管黏膜上皮周长（单个）
        BigDecimal perimeterC = annotation.getStructurePerimeterNum();
        BigDecimal organAreaE = areaUtils.getOrganArea(jsonTask, "12F0F4");// E输出小管/附睾管管腔面积（全片）
        BigDecimal organAreaG = areaUtils.getOrganArea(jsonTask, "12F0F7");// G精子面积（全片）
        BigDecimal organAreaI = areaUtils.getOrganArea(jsonTask, "12F003");// I血管面积
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());// J组织轮廓面积
        // todo H黏膜上皮细胞核数量（单个）

        // 算法输出指标
        resultsMap.put("输出小管/附睾管黏膜上皮面积（全片）", createIndicator(organAreaB, SQ_MM));
        resultsMap.put("输出小管/附睾管管腔面积（全片）", createIndicator(organAreaE, SQ_MM));
        resultsMap.put("精子面积（全片）", createIndicator(organAreaG, SQ_MM));
        resultsMap.put("血管面积", createIndicator(organAreaI, SQ_MM));
        resultsMap.put("输出小管/附睾管黏膜上皮周长（单个）", createDefaultIndicator());
        resultsMap.put("输出小管/附睾管黏膜上皮面积（单个）", createDefaultIndicator());// A输出小管/附睾管黏膜上皮面积（单个）
        resultsMap.put("输出小管/附睾管管腔面积（单个）", createDefaultIndicator());// D输出小管/附睾管管腔面积（单个）
        resultsMap.put("精子面积（单个）", createDefaultIndicator());// F精子面积（单个）

        // 产品呈现指标
        resultsMap.put("附睾面积", createNameIndicator("Epididymal area", new BigDecimal(slideArea), SQ_MM));

        aiForecastService.addAiForecast(jsonTask.getSingleId(),  resultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Epididymide";
    }
}
