package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wanglibei
 * @version V1.0
 * @ClassName: OvariesOviductParserStrategyImpl
 * @Description-d:卵巢与输卵管
 * @date 2025年7月22日
 */
@Slf4j
@Component("OvariesOviduct")
public class OvariesOviductParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.info("OvariesOviductParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("大鼠卵巢与输卵管构指标计算开始");
        // A 黄体数量 A 个
        Integer mucosaCountA = commonJsonParser.getOrganAreaCount(jsonTask, "17C0CA");
        mucosaCountA = commonJsonParser.getIntegerValue(mucosaCountA);
        // B 黄体面积（全片） 平方毫米
        BigDecimal bigDecimalC = getOrganArea(jsonTask, "17C0CA").getStructureAreaNum();
        bigDecimalC = bigDecimalC.setScale(3, RoundingMode.HALF_UP);
        //C 卵巢卵泡数量
        Integer mucosaCountC = commonJsonParser.getOrganAreaCount(jsonTask, "17C0CB");
        //D 卵巢卵泡面积（全片） mm2
        BigDecimal organAreaD = commonJsonParser.getOrganArea(jsonTask, "17C0CB").getStructureAreaNum();
        //E 卵巢血管面积 μm2
        BigDecimal organAreaE = commonJsonParser.getOrganArea(jsonTask, "17C003").getStructureAreaNum();
        //F 卵巢血管外红细胞面积 μm2
        BigDecimal organAreaF = commonJsonParser.getInsideOrOutside(jsonTask, "17C003", "17C004", false).getStructureAreaNum();
        // G 卵巢血管内红细胞面积	μm2
        BigDecimal organAreaG = commonJsonParser.getInsideOrOutside(jsonTask, "17C003", "17C004", true).getStructureAreaNum();


        //算法保存
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        if (mucosaCountA > 0) {
            indicatorResultsMap.put("黄体数量", createIndicator(String.valueOf(mucosaCountA), PIECE, "17C0CA"));
        }
        if (bigDecimalC.compareTo(BigDecimal.ZERO) != 0) {
            indicatorResultsMap.put("黄体面积（全片）", createIndicator(String.valueOf(bigDecimalC.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "17C0CA"));
        }
        indicatorResultsMap.put("卵巢黄体数量", createIndicator(String.valueOf(mucosaCountC), PIECE, "17C0CB"));
        indicatorResultsMap.put("卵巢卵泡面积（全片）", createIndicator(String.valueOf(organAreaD), SQ_MM, "17C0CB"));
        indicatorResultsMap.put("卵巢血管面积", createIndicator(areaUtils.convertToMicrometer(organAreaE.toString()), SQ_MM, "17C003"));
        indicatorResultsMap.put("卵巢血管外红细胞面积", createIndicator(areaUtils.convertToMicrometer(organAreaF.toString()), SQ_MM, "17C003,17C004"));
        indicatorResultsMap.put("卵巢血管外红细胞面积", createIndicator(areaUtils.convertToMicrometer(organAreaG.toString()), SQ_MM, "17C003,17C004"));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);

    }

    @Override
    public String getAlgorithmCode() {
        return "OvariesOviduct";
    }
}
