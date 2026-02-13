package cn.staitech.fr.service.strategy.json.impl.dog.immune;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.DecimalUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>犬类-免疫系统-脾脏</p>
 * <a href="https://docs.staitech.cn/i/nodes/7gNKMlbrq3WXA1ddAE2f4Xv5p6Ad9nL1">文档</a>
 *
 * @author wangtc
 */
@Component
@Slf4j
public class SpleenParserStrategyImpl_345 extends AbstractCustomParserStrategy {
    private final SingleSlideMapper singleSlideMapper;
    private final AiForecastService aiForecastService;

    public SpleenParserStrategyImpl_345(
            CommonJsonParser commonJsonParser,
            CommonJsonCheck commonJsonCheck,
            SingleSlideMapper singleSlideMapper,
            AiForecastService aiForecastService
    ) {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        this.singleSlideMapper = singleSlideMapper;
        this.aiForecastService = aiForecastService;
    }

    @Override
    public String getAlgorithmCode() {
        return "Spleen_3";
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> map = new HashMap<>();
        // 红髓面积 A
        BigDecimal areaA = getOrganArea(jsonTask, "345046").getStructureAreaNum();
        map.put("红髓面积", new IndicatorAddIn("", DecimalUtils.setScale3(areaA), SQ_MM, CommonConstant.NUMBER_0, "345046"));
        // 白髓面积 B
        BigDecimal areaB = getOrganArea(jsonTask, "345047").getStructureAreaNum();
        map.put("白髓面积", new IndicatorAddIn("", DecimalUtils.setScale3(areaB), SQ_MM, CommonConstant.NUMBER_0, "345047"));
        // 中央动脉面积 C
        BigDecimal areaC = getOrganArea(jsonTask, "345048").getStructureAreaNum();
        map.put("中央动脉面积", new IndicatorAddIn("", DecimalUtils.setScale3(areaC), SQ_MM, CommonConstant.NUMBER_0, "345048"));
        // 生发中心面积 D
        BigDecimal areaD = getOrganArea(jsonTask, "345051").getStructureAreaNum();
        map.put("生发中心面积", new IndicatorAddIn("", DecimalUtils.setScale3(areaD), SQ_MM, CommonConstant.NUMBER_0, "345051"));
        // 红细胞面积 E
        BigDecimal areaE = getOrganArea(jsonTask, "345004").getStructureAreaNum();
        map.put("红细胞面积", new IndicatorAddIn("", DecimalUtils.setScale3(areaE), SQ_MM, CommonConstant.NUMBER_0, "345004"));
        // 组织轮廓 F
        BigDecimal areaF = new BigDecimal(singleSlideMapper.selectById(jsonTask.getSingleId()).getArea());
//        map.put("组织轮廓面积", new IndicatorAddIn("", DecimalUtils.setScale3(areaF), SQ_MM, CommonConstant.NUMBER_0, "345111"));
        // 小梁面积 G
        BigDecimal areaG = getOrganArea(jsonTask, "34505B").getStructureAreaNum();
        map.put("小梁面积", new IndicatorAddIn("", DecimalUtils.setScale3(areaG), SQ_MM, CommonConstant.NUMBER_0, "34505B"));
        // 边缘区面积 H
        BigDecimal areaH = getOrganArea(jsonTask, "34504A").getStructureAreaNum();
        map.put("边缘区面积", new IndicatorAddIn("", DecimalUtils.setScale3(areaH), SQ_MM, CommonConstant.NUMBER_0, "34504A"));
        // 红髓面积占比 1=(A-G)/F
        BigDecimal proportion1 = getProportion(areaA.subtract(areaG), areaF);
        map.put("红髓面积占比", new IndicatorAddIn("Red pulp area%", DecimalUtils.setScale3(proportion1), PERCENTAGE, CommonConstant.NUMBER_1, "345046,34505B,345111"));
        // 白髓面积占比 2=B/F
        BigDecimal proportion2 = getProportion(areaB, areaF);
        map.put("白髓面积占比", new IndicatorAddIn("White pulp area%", DecimalUtils.setScale3(proportion2), PERCENTAGE, CommonConstant.NUMBER_1, "345047,345111"));
        // 红细胞面积占比 3=E/F
        BigDecimal proportion3 = getProportion(areaE, areaF);
        map.put("红细胞面积占比", new IndicatorAddIn("Erythrocyte area%", DecimalUtils.setScale3(proportion3), PERCENTAGE, CommonConstant.NUMBER_1, "345004,345111"));
        // 脾脏面积 4=F
        map.put("脾脏面积", new IndicatorAddIn("Spleen area", DecimalUtils.setScale3(areaF), SQ_MM, CommonConstant.NUMBER_0, "345111"));
        // 边缘区面积占比 5=H/F
        BigDecimal proportion5 = getProportion(areaH, areaF);
        map.put("边缘区面积占比", new IndicatorAddIn("Marginal area %", DecimalUtils.setScale3(proportion5), PERCENTAGE, CommonConstant.NUMBER_1, "34504A,345111"));
        // 保存结果
        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
    }
}
