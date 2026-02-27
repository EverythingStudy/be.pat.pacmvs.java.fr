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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>犬类-免疫系统-胸腺</p>
 * <a href="https://docs.staitech.cn/i/nodes/7gNKMlbrq3WXA1LPrL2f4Xv5p6Ad9nL1">文档</a>
 *
 * @author wangtc
 */
@Component
@Slf4j
public class Thymus_3ParserStrategyImpl_344 extends AbstractCustomParserStrategy {
    private final SingleSlideMapper singleSlideMapper;
    private final AiForecastService aiForecastService;

    public Thymus_3ParserStrategyImpl_344(
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
        return "Thymus_3";
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> map = new LinkedHashMap<>();
        // 皮质面积 A
        BigDecimal areaA = getOrganArea(jsonTask, "34403D").getStructureAreaNum();
        map.put("皮质面积", new IndicatorAddIn("", DecimalUtils.setScale3(areaA), SQ_MM, CommonConstant.NUMBER_0, "34403D"));
        // 髓质面积 B
        BigDecimal areaB = getOrganArea(jsonTask, "34403E").getStructureAreaNum();
        map.put("髓质面积", new IndicatorAddIn("", DecimalUtils.setScale3(areaB), SQ_MM, CommonConstant.NUMBER_0, "34403E"));
        // 红细胞面积 C
        BigDecimal areaC = getOrganArea(jsonTask, "344004").getStructureAreaNum();
        map.put("红细胞面积", new IndicatorAddIn("", DecimalUtils.setScale3(areaC), SQ_MM, CommonConstant.NUMBER_0, "344004"));
        // 组织轮廓 D
        BigDecimal areaD = new BigDecimal(singleSlideMapper.selectById(jsonTask.getSingleId()).getArea());
//        map.put("组织轮廓面积", new IndicatorAddIn("", DecimalUtils.setScale3(areaD), SQ_MM, CommonConstant.NUMBER_0, "344111"));
        // 皮质占比 1=A/D
        BigDecimal proportion1 = getProportion(areaA, areaD);
        map.put("皮质占比", new IndicatorAddIn("Cortex area%", DecimalUtils.setScale3(proportion1), PERCENTAGE, CommonConstant.NUMBER_1, "34403D,344111"));
        // 髓质占比 2=B/D
        BigDecimal proportion2 = getProportion(areaB, areaD);
        map.put("髓质占比", new IndicatorAddIn("Medulla area%", DecimalUtils.setScale3(proportion2), PERCENTAGE, CommonConstant.NUMBER_1, "34403E,344111"));
        // 皮髓比 3=A/B
        BigDecimal divide3 = bigDecimalDivideCheck(areaA, areaB);
        map.put("皮髓比", new IndicatorAddIn("Corticomedullary ratio", divide3.toPlainString() + ":1", NOT, CommonConstant.NUMBER_1, "34403D,34403E"));
        // 红细胞面积占比 4=C/D
        BigDecimal proportion4 = getProportion(areaC, areaD);
        map.put("红细胞占比", new IndicatorAddIn("Erythrocyte area%", DecimalUtils.setScale3(proportion4), PERCENTAGE, CommonConstant.NUMBER_1, "344004,344111"));
        // 胸腺面积 5=D
        map.put("胸腺面积", new IndicatorAddIn("Thymus area", DecimalUtils.setScale3(areaD), SQ_MM, CommonConstant.NUMBER_1, "344111"));
        // 保存结果
        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
    }
}
