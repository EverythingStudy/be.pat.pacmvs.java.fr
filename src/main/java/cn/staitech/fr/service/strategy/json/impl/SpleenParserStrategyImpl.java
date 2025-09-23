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
 * @Description: Json Parser 大鼠-免疫系统-脾脏 Spleen SP
 */
@Slf4j
@Component("Spleen")
public class SpleenParserStrategyImpl extends AbstractCustomParserStrategy {

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
        log.info("SpleenParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("指标计算开始-大鼠脾脏");
        Map<String, IndicatorAddIn> map = new HashMap<>();

        //        脾脏
        //        白髓	145047
        //        动脉周围淋巴鞘	145045
        //        中央动脉	145048
        //        含铁血黄素	14504D(无数据！！！！)
        //        红细胞	145004
        //        组织轮廓	145111
        //        145004.json  145045.json  145046.json（红髓）  145047.json  145048.json  14504A.json（边缘区）

        //        算法输出指标	指标代码（仅限本文档）	单位（（保留小数点后三位））	备注
        //        白髓面积	A	平方毫米	数据相加输出
        BigDecimal whitePulpArea = getOrganArea(jsonTask, "145047").getStructureAreaNum();
        //        动脉周围淋巴鞘面积	B	平方毫米	数据相加输出
        BigDecimal periarterialLymphaticSheathArea = getOrganArea(jsonTask, "145045").getStructureAreaNum();
        //        中央动脉面积	C	平方毫米	数据相加输出
        BigDecimal centralArteryArea = getOrganArea(jsonTask, "145048").getStructureAreaNum();
        //        含铁血黄素面积	D	平方毫米	数据相加输出（无Json数据）
        BigDecimal hemosiderinArea = getOrganArea(jsonTask, "14504D").getStructureAreaNum();
        //        红细胞面积	E	平方毫米	数据相加输出
        BigDecimal erythrocyteArea = getOrganArea(jsonTask, "145004").getStructureAreaNum();
        //        组织轮廓面积	F	平方毫米(H:精细轮廓总面积（脾脏）-平方毫米)
        String accurateArea = singleSlideMapper.selectById(jsonTask.getSingleId()).getArea();
        BigDecimal accurateAreaDecimal = new BigDecimal(accurateArea);
        //BigDecimal accurateAreaDecimal = getOrganArea(jsonTask, "145111").getStructureAreaNum();
        //        边缘区面积	G	平方毫米	数据相加输出（算法直接输出）(WORD无数据，JSON有数据！)
        BigDecimal marginalZoneArea = getOrganArea(jsonTask, "14504A").getStructureAreaNum();
        //        红髓	H	平方毫米	算法直接输出 (WORD无数据，JSON有数据！)
        BigDecimal redPulpArea = getOrganArea(jsonTask, "145046").getStructureAreaNum();

        //        产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后三位）	English	计算方式	备注
        //        白髓面积占比	1	%	White pulp area%	1=A/F
        //        含铁血黄素面积占比	2	%	Hemosiderin area%	2=D/F
        //        红髓面积占比	3	%	Red pulp area%	3=H/F
        //        红细胞面积占比	4	%	Erythrocyte area%	4=E/F
        //        边缘区面积占比	5	%	Marginal zone area%	5=G/F
        //        动脉周围淋巴鞘面积占比	6	%	Periarterial lymphatic sheath area%	6=（B-C）/F	包含淋巴滤泡
        //        脾脏面积	7	平方毫米	Spleen area	7=F

        // 算法输出指标 -------------------------------------------------------------
        // A
        map.put("白髓面积", createIndicator(DecimalUtils.setScale3(whitePulpArea), SQ_MM, "145047"));
        // B
        map.put("动脉周围淋巴鞘面积", createIndicator(DecimalUtils.setScale3(periarterialLymphaticSheathArea), SQ_MM, "145045"));
        // C
        map.put("中央动脉面积", createIndicator(DecimalUtils.setScale3(centralArteryArea), SQ_MM, "145048"));
        // D
        map.put("含铁血黄素面积", createIndicator(DecimalUtils.setScale3(hemosiderinArea), SQ_MM, "14504D"));
        // E
        map.put("红细胞面积", createIndicator(DecimalUtils.setScale3(erythrocyteArea), SQ_MM, "145004"));
        // G
        //map.put("边缘区面积", createIndicator(DecimalUtils.setScale3(marginalZoneArea), SQ_MM, "14504A"));
        // H
        //map.put("红髓面积", createIndicator(DecimalUtils.setScale3(redPulpArea), SQ_MM, "145046"));

        // 产品呈现指标 -------------------------------------------------------------
        if (accurateAreaDecimal.compareTo(BigDecimal.ZERO) != 0) {
            // 白髓面积占比	1	%	White pulp area%	1=A/F
            BigDecimal whitePulpAreaRate = whitePulpArea.divide(accurateAreaDecimal, 7, RoundingMode.HALF_UP);
            map.put("白髓面积占比", createNameIndicator("White pulp area%", DecimalUtils.percentScale3(whitePulpAreaRate), PERCENTAGE, "145047,145111"));

            // 含铁血黄素面积占比	2	%	White pulp area%%	2=D/F
            BigDecimal hemosiderinAreaRate = hemosiderinArea.divide(accurateAreaDecimal, 7, RoundingMode.HALF_UP);
            map.put("含铁血黄素面积占比", createNameIndicator("Hemosiderin area%", DecimalUtils.percentScale3(hemosiderinAreaRate), PERCENTAGE, "14504D,145111"));

            //红髓面积占比 Red pulp area% 计算公式=3=(F-A)/F
            BigDecimal redPulpAreaRate = accurateAreaDecimal.subtract(redPulpArea).divide(accurateAreaDecimal, 7, RoundingMode.HALF_UP);
            map.put("红髓面积占比", createNameIndicator("Red pulp area%", DecimalUtils.percentScale3(redPulpAreaRate), PERCENTAGE, "145047,145111"));

            // 红细胞面积占比	4	%	Erythrocyte area%	4=E/F
            BigDecimal erythrocyteAreaRate = erythrocyteArea.divide(accurateAreaDecimal, 7, RoundingMode.HALF_UP);
            map.put("红细胞面积占比", createNameIndicator("Erythrocyte area%", DecimalUtils.percentScale3(erythrocyteAreaRate), PERCENTAGE, "145004,145111"));

            // 边缘区面积占比	5	%	Marginal zone area%	5=(A-B)/F
            BigDecimal marginalZoneAreaRate = whitePulpArea.subtract(periarterialLymphaticSheathArea).divide(accurateAreaDecimal, 7, RoundingMode.HALF_UP);
            map.put("边缘区面积占比", createNameIndicator("Marginal zone area", DecimalUtils.percentScale3(marginalZoneAreaRate), PERCENTAGE, "145047,145045,145111"));

            // 动脉周围淋巴鞘面积占比	6	%	Periarterial lymphatic sheath area%	6=（B-C）/F	包含淋巴滤泡
            BigDecimal periarterialLymphaticSheathAreaRate = periarterialLymphaticSheathArea.subtract(centralArteryArea).divide(accurateAreaDecimal, 7, RoundingMode.HALF_UP);
            map.put("动脉周围淋巴鞘面积占比", createNameIndicator("Periarterial lymphatic sheath area%", DecimalUtils.percentScale3(periarterialLymphaticSheathAreaRate), PERCENTAGE, "145045,145048,145111"));

        } else {
            map.put("白髓面积占比", createNameIndicator("White pulp area%", "0.000", PERCENTAGE, "145047,145111"));
            map.put("含铁血黄素面积占比", createNameIndicator("Hemosiderin area%", "0.000", PERCENTAGE, "14504D,145111"));
            map.put("红髓面积占比", createNameIndicator("Red pulp area%", "0.000", PERCENTAGE, "145047,145111"));
            map.put("红细胞面积占比", createNameIndicator("Erythrocyte area%", "0.000", PERCENTAGE, "145004,145111"));
            map.put("边缘区面积占比", createNameIndicator("Marginal zone area", "0.000", PERCENTAGE, "145047,145045,145111"));
            map.put("动脉周围淋巴鞘面积占比", createNameIndicator("Periarterial lymphatic sheath area%", "0.000", PERCENTAGE, "145045,145048,145111"));
        }

        // F
        map.put("脾脏面积", createNameIndicator("Spleen area", DecimalUtils.setScale3(accurateAreaDecimal), SQ_MM, "145111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
        log.info("指标计算结束-大鼠脾脏");
    }

    @Override
    public String getAlgorithmCode() {
        return "Spleen";
    }
}
