package cn.staitech.fr.service.strategy.json.impl.dog.sensory;

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
 * <p>犬类-特殊感觉系统-泪腺</p>
 * <a href="https://docs.staitech.cn/i/nodes/7gNKMlbrq3WXA1LOYv2C4Xv5p6Ad9nL1">文档</a>
 *
 * @author wangtc
 */
@Component
@Slf4j
public class LacrimalGland_3ParserStrategyImpl_369 extends AbstractCustomParserStrategy {
    private final SingleSlideMapper singleSlideMapper;
    private final AiForecastService aiForecastService;

    public LacrimalGland_3ParserStrategyImpl_369(
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
        return "Lacrimal_gland_3";
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> map = new HashMap<>();
        // 腺泡面积 A
        BigDecimal areaA = getOrganArea(jsonTask, "36906D").getStructureAreaNum();
        map.put("腺泡面积", new IndicatorAddIn("", DecimalUtils.setScale3(areaA), SQ_MM, CommonConstant.NUMBER_0, "36906D"));
        // 腺泡细胞核数量 B
//        Integer countB = getOrganAreaCount(jsonTask, "36906E");
//        map.put("腺泡细胞核数量", new IndicatorAddIn("", countB.toString(), PIECE, CommonConstant.NUMBER_0, "36906E"));
        // 腺泡细胞核面积（单个） C -> 产品呈现指标
//        Annotation ann = new Annotation();
//        ann.setAreaName("腺泡细胞核面积（单个）");
//        ann.setAreaUnit(SQ_UM);
//        getCommonJsonParser().putSingleAnnotationDynamicData(jsonTask, "36906E", ann, 2);
        // 组织轮廓面积 D
        BigDecimal areaD = new BigDecimal(singleSlideMapper.selectById(jsonTask.getSingleId()).getArea());
//        map.put("组织轮廓面积", new IndicatorAddIn("", DecimalUtils.setScale3(areaD), SQ_MM, CommonConstant.NUMBER_0, "369111"));
        // 腺泡面积占比 1=A/D
        BigDecimal proportion1 = getProportion(areaA, areaD);
        map.put("腺泡面积占比", new IndicatorAddIn("Acinus area%", DecimalUtils.setScale3(proportion1), PERCENTAGE, CommonConstant.NUMBER_1, "36906D,369111"));
        // 腺泡细胞核密度 2=B/A
//        BigDecimal density2 = bigDecimalDivideCheck(BigDecimal.valueOf(countB), areaA);
//        map.put("腺泡细胞核密度", new IndicatorAddIn("Nucleus density of acinus", DecimalUtils.setScale3(density2), SQ_MM_PIECE, CommonConstant.NUMBER_1, "36906E"));
        // 腺泡细胞核面积（单个） 3=C 95%置信区间
//        List<Annotation> listC = getCommonJsonParser().getStructureContourList(jsonTask, "36906E");
//        List<BigDecimal> statC = listC.stream().map(a -> new BigDecimal(getCommonJsonParser().convertToMicrometer(a.getStructureAreaNum().toPlainString()))).collect(Collectors.toList());
//        map.put("腺泡细胞核面积（单个）", new IndicatorAddIn("Acinar nucleus area (per)", MathUtils.getConfidenceInterval(statC), SQ_UM, CommonConstant.NUMBER_1, "36906E"));
        // 泪腺面积 4=D
        map.put("泪腺面积", new IndicatorAddIn("Lacrimal gland area", DecimalUtils.setScale3(areaD), SQ_MM, CommonConstant.NUMBER_1, "369111"));
        // 保存结果
        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
    }
}
