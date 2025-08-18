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
 * @author: wangfeng
 * @create: 2024-05-10 14:18:48
 * @Description: Json Parser 大鼠-免疫系统-腹股沟淋巴结 Inguinal lymph node
 */
@Slf4j
@Component("Inguinal_lymph_node")
public class InguinalLymphNodeParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.info("指标计算开始-腹股沟淋巴结");
        Map<String, IndicatorAddIn> map = new HashMap<>();

        //        腹股沟淋巴结
        //        淋巴滤泡	147050
        //        生发中心	147051
        //        髓质	14703E
        //        组织轮廓	147111
        //        14703E.json  147050.json  147051.json  147052.json (皮质与副皮质区域)

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
        Integer areaCountA = commonJsonParser.getOrganAreaCount(jsonTask, "147051");
        // 生发中心面积（全片）	B	平方毫米	数据相加输出
        BigDecimal organAreaB = commonJsonParser.getOrganArea(jsonTask, "147051").getStructureAreaNum();
        // 髓质面积	C	平方毫米
        BigDecimal organAreaC = commonJsonParser.getOrganArea(jsonTask, "14703E").getStructureAreaNum();
        // 5=D:淋巴结面积-平方毫米
//        String accurateArea = singleSlideMapper.selectById(jsonTask.getSingleId()).getArea();
//        BigDecimal accurateAreaDecimal = new BigDecimal(accurateArea);
        BigDecimal organAreaD = commonJsonParser.getOrganArea(jsonTask, "147111").getStructureAreaNum();

        // 算法输出指标 -------------------------------------------------------------
        // B
        map.put("生发中心面积（全片）", createIndicator(DecimalUtils.setScale3(organAreaB), SQ_MM, "147051"));
        // C
        map.put("髓质面积", createIndicator(DecimalUtils.setScale3(organAreaC), SQ_MM, "14703E"));

        // 产品呈现指标 -------------------------------------------------------------
        // A 生发中心数量	1	个	 Number of germinal center	1=A
        map.put("生发中心数量", createNameIndicator("Number of germinal center", areaCountA.toString(), "个", "147051"));

        if (organAreaD.compareTo(BigDecimal.ZERO) != 0) {
            // 生发中心占比	2	%	Germinal center area%	2=B/D
            BigDecimal germinalCenterAreaRate = organAreaB.divide(organAreaD, 7, RoundingMode.HALF_UP);
            map.put("生发中心占比", createNameIndicator("Germinal center area%", DecimalUtils.percentScale3(germinalCenterAreaRate), PERCENTAGE, "147051,147111"));

            // 髓质占比	3	%	Medulla area%	3=C/D
            BigDecimal medullaAreaRate = organAreaC.divide(organAreaD, 7, RoundingMode.HALF_UP);
            map.put("髓质占比", createNameIndicator("Medulla area%", DecimalUtils.percentScale3(medullaAreaRate), PERCENTAGE, "14703E,147111"));

            // 皮质和副皮质占比	4	%	Cortex and paracortex area%	4=（D-C）/D
            BigDecimal cortexAndParacortexAreaRate = organAreaD.subtract(organAreaC).divide(organAreaD, 7, RoundingMode.HALF_UP);
            map.put("皮质和副皮质占比", createNameIndicator("Cortex and paracortex area%", DecimalUtils.percentScale3(cortexAndParacortexAreaRate), PERCENTAGE, "14703E,147111"));
        } else {
            map.put("生发中心占比", createNameIndicator("Germinal center area%", "0.000", PERCENTAGE, "147051,147111"));
            map.put("髓质占比", createNameIndicator("Medulla area%", "0.000", PERCENTAGE, "14703E,147111"));
            map.put("皮质和副皮质占比", createNameIndicator("Cortex and paracortex area%", "0.000", PERCENTAGE, "14703E,147111"));
        }
        // D 淋巴结面积	5	平方毫米	Lymph node area	5=D
        map.put("淋巴结面积", createNameIndicator("Lymph node area", DecimalUtils.setScale3(organAreaD), SQ_MM, "147111"));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
        log.info("指标计算结束-腹股沟淋巴结");
    }

    @Override
    public String getAlgorithmCode() {
        return "Inguinal lymph node";
    }
}
