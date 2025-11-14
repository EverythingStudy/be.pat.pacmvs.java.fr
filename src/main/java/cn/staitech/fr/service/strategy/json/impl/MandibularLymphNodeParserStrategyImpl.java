package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.DecimalUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: chenly
 * @create: 2025-11-04 09:07
 * @Description: Json Parser 大鼠-免疫系统-颌下淋巴结 Mandibular_lymph_node
 */
@Slf4j
@Component("Lymph_node_mandibular")
public class MandibularLymphNodeParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
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
        log.info("指标计算开始-颌下淋巴结");
        Map<String, IndicatorAddIn> map = new HashMap<>();

        // A 生发中心数量	1	个	 Number of germinal center	1=A  147051
        Integer areaCountA = commonJsonParser.getOrganAreaCount(jsonTask, "148051");
        // B 生发中心面积（全片）	B	平方毫米	数据相加输出
        BigDecimal organAreaB = commonJsonParser.getOrganArea(jsonTask, "148051").getStructureAreaNum();
        // C 髓质面积	C	平方毫米
        BigDecimal organAreaC = commonJsonParser.getOrganArea(jsonTask, "14803E").getStructureAreaNum();
        // D:淋巴结面积-平方毫米
        String accurateArea = singleSlideMapper.selectById(jsonTask.getSingleId()).getArea();
        BigDecimal organAreaD = new BigDecimal(accurateArea);
        //BigDecimal organAreaD = commonJsonParser.getOrganArea(jsonTask, "148111").getStructureAreaNum();

        // 算法输出指标 -------------------------------------------------------------
        // B 生发中心面积（全片）
        map.put("生发中心面积（全片）", createIndicator(DecimalUtils.setScale3(organAreaB), SQ_UM, "148051"));
        // C 髓质面积
        map.put("髓质面积", createIndicator(DecimalUtils.setScale3(organAreaC), SQ_UM, "14803E"));
        // 产品呈现指标 -------------------------------------------------------------
        // 1 生发中心数量
        map.put("生发中心数量", createNameIndicator("Number of germinal center", areaCountA.toString(), PIECE, "148051"));

        if (organAreaD.compareTo(BigDecimal.ZERO) != 0) {
            // 2 生发中心占比		%	Germinal center area%	2=B/D
            BigDecimal germinalCenterAreaRateDecimal = organAreaB.divide(organAreaD, 6, RoundingMode.HALF_UP);
            map.put("生发中心面积占比", createNameIndicator("Germinal center area%", DecimalUtils.percentScale3(germinalCenterAreaRateDecimal), PERCENTAGE, "148051,148111"));

            // 3 髓质占比		%	Medulla area%	3=C/D
            BigDecimal medullaAreaRateDecimal = organAreaC.divide(organAreaD, 6, RoundingMode.HALF_UP);
            map.put("髓质占比", createNameIndicator("Medulla area%", DecimalUtils.percentScale3(medullaAreaRateDecimal), PERCENTAGE, "14803E,148111"));

            // 4 皮质和副皮质占比		%	Cortex and paracortex area%	4=（D-C）/D
            BigDecimal cortexAndParacortexAreaRateDecimal = organAreaD.subtract(organAreaC).divide(organAreaD, 6, RoundingMode.HALF_UP);
            map.put("皮质和副皮质占比", createNameIndicator("Cortex and paracortex area%", DecimalUtils.percentScale3(cortexAndParacortexAreaRateDecimal), PERCENTAGE, "14803E,148111"));
        }

        // 5 淋巴结面积 mm2  5=D
        map.put("淋巴结面积", createNameIndicator("Lymph node area", DecimalUtils.setScale3(organAreaD), SQ_UM, "148111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
        log.info("指标计算结束-颌下淋巴结");
    }

    @Override
    public String getAlgorithmCode() {
        return "Lymph_node_mandibular";
    }
}
