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
    @Resource
    private CommonJsonCheck commonJsonCheck;
    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("MesentericLymphNodeParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> resultsMap = new HashMap<>();

        // 获取各种指标
        Integer areaCountA = areaUtils.getOrganAreaCount(jsonTask, "146051");// A生发中心数量
        BigDecimal organAreaB = areaUtils.getOrganArea(jsonTask, "146051");// B生发中心面积（全片）
        BigDecimal organAreaC = areaUtils.getOrganArea(jsonTask, "14603E");// C髓质面积
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());// D组织轮廓

        // 算法输出指标
        resultsMap.put("生发中心面积（全片）", createIndicator(organAreaB, SQ_MM));
        resultsMap.put("髓质面积", createIndicator(organAreaC, SQ_MM));

        // 产品呈现指标
        resultsMap.put("淋巴结面积", createNameIndicator("Submadibular gland area", slideArea, SQ_MM));
        resultsMap.put("生发中心数量", createNameIndicator("Number of germinal center", areaCountA, PIECE));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), resultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Mesenteric_lymph_node";
    }
}
