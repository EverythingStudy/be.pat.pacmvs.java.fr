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
 * <p>犬类-免疫系统-肠系膜淋巴结</p>
 * <a href="https://docs.staitech.cn/i/nodes/7gNKMlbrq3WXA1eROy0u4Xv5p6Ad9nL1">文档</a>
 *
 * @author wangtc
 */
@Component
@Slf4j
public class LymphNodeMesenteric_3ParserStrategyImpl_346 extends AbstractCustomParserStrategy {
    private final SingleSlideMapper singleSlideMapper;
    private final AiForecastService aiForecastService;

    public LymphNodeMesenteric_3ParserStrategyImpl_346(
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
        return "Lymph_node_mesenteric_3";
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> map = new HashMap<>();
        // 生发中心数量 A
        Integer countA = getOrganAreaCount(jsonTask, "346051");
        map.put("生发中心数量", new IndicatorAddIn("", countA.toString(), PIECE, CommonConstant.NUMBER_0, "346054,346051"));
        // 生发中心面积 B
        BigDecimal areaB = getOrganArea(jsonTask, "346051").getStructureAreaNum();
        map.put("生发中心面积", new IndicatorAddIn("", DecimalUtils.setScale3(areaB), SQ_MM, CommonConstant.NUMBER_0, "346054,346051"));
        // 皮质+副皮质区面积 C
        BigDecimal areaC = getOrganArea(jsonTask, "346052").getStructureAreaNum();
        map.put("皮质+副皮质区面积", new IndicatorAddIn("", DecimalUtils.setScale3(areaC), SQ_MM, CommonConstant.NUMBER_0, "346052"));
        // 髓质面积 D
        BigDecimal areaD = getOrganArea(jsonTask, "34603E").getStructureAreaNum();
        map.put("髓质面积", new IndicatorAddIn("", DecimalUtils.setScale3(areaD), SQ_MM, CommonConstant.NUMBER_0, "34603E"));
        // 组织轮廓面积 E
        BigDecimal areaE = new BigDecimal(singleSlideMapper.selectById(jsonTask.getSingleId()).getArea());
//        map.put("组织轮廓面积", new IndicatorAddIn("", DecimalUtils.setScale3(areaE), SQ_MM, CommonConstant.NUMBER_0, "346111"));
        // 生发中心数量 1=A
        map.put("生发中心数量", new IndicatorAddIn("Number of germinal center", countA.toString(), PIECE, CommonConstant.NUMBER_1, "346054,346051"));
        // 生发中心面积占比 2=B/E
        BigDecimal proportion2 = getProportion(areaB, areaE);
        map.put("生发中心面积占比", new IndicatorAddIn("Germinal center area%", DecimalUtils.setScale3(proportion2), PERCENTAGE, CommonConstant.NUMBER_1, "346054,346051,346111"));
        // 皮质+副皮质区占比 3=C/E
        BigDecimal proportion3 = getProportion(areaC, areaE);
        map.put("皮质+副皮质区占比", new IndicatorAddIn("Cortex and paracortex area%", DecimalUtils.setScale3(proportion3), PERCENTAGE, CommonConstant.NUMBER_1, "346052,346111"));
        // 髓质占比 4=D/E
        BigDecimal proportion4 = getProportion(areaD, areaE);
        map.put("髓质占比", new IndicatorAddIn("Medulla area%", DecimalUtils.setScale3(proportion4), PERCENTAGE, CommonConstant.NUMBER_1, "34603E,346111"));
        // 淋巴结面积 5=E
        map.put("淋巴结面积", new IndicatorAddIn("Lymph node area", DecimalUtils.setScale3(areaE), SQ_MM, CommonConstant.NUMBER_1, "346111"));
        // 保存结果
        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
    }
}
