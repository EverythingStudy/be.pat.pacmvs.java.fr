package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
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
 * @author: wangfeng
 * @create: 2024-05-10 14:18:48
 * @Description: Json Parser 大鼠-免疫系统-颌下淋巴结 Mandibular_lymph_node
 */
@Slf4j
@Component("Mandibular_lymph_node")
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

        //        颌下淋巴结
        //
        //        结构	编码
        //        淋巴滤泡	148050
        //        生发中心	148051
        //        髓质	14803E
        //        组织轮廓	148111
        //        14803E.json  148050.json  148051.json  148052.json

        //        算法输出指标	指标代码（仅限本文档）	单位（保留3位小数）	备注
        //        生发中心数量	A	个
        //        生发中心面积（全片）	B	平方毫米	数据相加输出
        //        髓质面积	C	平方毫米
        //        组织轮廓面积	D	平方毫米
        //
        //        产品呈现指标	指标代码（仅限本文档）	单位（保留3位小数）	English	计算方式	备注
        //        生发中心数量	1	个	 Number of germinal center	1=A
        //        生发中心占比	2	%	Germinal center area%	2=B/D
        //        髓质占比	3	%	Medulla area%	3=C/D
        //        皮质和副皮质占比	4	%	Cortex and paracortex area%	4=（D-C）/D
        //        淋巴结面积	5	平方毫米	Lymph node area	5=D

        // 生发中心数量	1	个	 Number of germinal center	1=A  147051
        Integer germinalCenterCount = commonJsonParser.getOrganAreaCount(jsonTask, "148051");
        // 生发中心面积（全片）	B	平方毫米	数据相加输出
        BigDecimal germinalCenterArea = commonJsonParser.getOrganArea(jsonTask, "148051").getStructureAreaNum();
        // 髓质面积	C	平方毫米
        BigDecimal medullaArea = commonJsonParser.getOrganArea(jsonTask, "14803E").getStructureAreaNum();
        // 5=D:淋巴结面积-平方毫米
//        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
//        String accurateArea = singleSlide.getArea();
//        BigDecimal accurateAreaDecimal = new BigDecimal(accurateArea);
        BigDecimal accurateAreaDecimal = commonJsonParser.getOrganArea(jsonTask, "148111").getStructureAreaNum();

        // 算法输出指标 -------------------------------------------------------------
        // B
        map.put("生发中心面积（全片）", new IndicatorAddIn("Number of germinal center", DecimalUtils.setScale3(germinalCenterArea), SQ_UM, CommonConstant.NUMBER_1));
        // C
        map.put("髓质面积", new IndicatorAddIn("Medulla area", DecimalUtils.setScale3(medullaArea), SQ_UM, CommonConstant.NUMBER_1));
        // 产品呈现指标 -------------------------------------------------------------

        // A
        map.put("生发中心数量", new IndicatorAddIn("Number of germinal center", germinalCenterCount.toString(), "个"));

        if (accurateAreaDecimal.compareTo(BigDecimal.ZERO) != 0) {
            //  生发中心占比	2	%	Germinal center area%	2=B/D
            BigDecimal germinalCenterAreaRateDecimal = germinalCenterArea.divide(accurateAreaDecimal, 6, RoundingMode.HALF_UP);
            map.put("生发中心占比", new IndicatorAddIn("Germinal center area%", DecimalUtils.percentScale3(germinalCenterAreaRateDecimal), PERCENTAGE));

            //  髓质占比	3	%	Medulla area%	3=C/D
            BigDecimal medullaAreaRateDecimal = medullaArea.divide(accurateAreaDecimal, 6, RoundingMode.HALF_UP);
            map.put("髓质占比", new IndicatorAddIn("Medulla area%", DecimalUtils.percentScale3(medullaAreaRateDecimal), PERCENTAGE));

            //  皮质和副皮质占比	4	%	Cortex and paracortex area%	4=（D-C）/D
            BigDecimal cortexAndParacortexAreaRateDecimal = accurateAreaDecimal.subtract(medullaArea).divide(accurateAreaDecimal, 6, RoundingMode.HALF_UP);
            map.put("皮质和副皮质占比", new IndicatorAddIn("Cortex and paracortex area%", DecimalUtils.percentScale3(cortexAndParacortexAreaRateDecimal), PERCENTAGE));
        } else {
            map.put("生发中心占比", new IndicatorAddIn("Germinal center area%", "0.000", PERCENTAGE));
            map.put("髓质占比", new IndicatorAddIn("Medulla area%", "0.000", PERCENTAGE));
            map.put("皮质和副皮质占比", new IndicatorAddIn("Cortex and paracortex area%", "0.000", PERCENTAGE));
        }

        // D
        map.put("淋巴结面积", new IndicatorAddIn("Lymph node area", DecimalUtils.setScale3(accurateAreaDecimal), SQ_UM));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
        log.info("指标计算结束-颌下淋巴结");
    }

    @Override
    public String getAlgorithmCode() {
        return "Mandibular_lymph_node";
    }
}
