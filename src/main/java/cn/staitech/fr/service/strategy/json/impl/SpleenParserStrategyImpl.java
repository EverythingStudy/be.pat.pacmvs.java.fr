package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.service.strategy.json.ParserStrategy;
import cn.staitech.fr.utils.DecimalUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: wangfeng
 * @create: 2024-05-10 14:18:48
 * @Description: Json Parser 大鼠脾脏 Spleen SP
 */
@Slf4j
@Component("Spleen")
public class SpleenParserStrategyImpl implements ParserStrategy {

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

    @Override
    public void parseJson(JsonTask jsonTask, JsonFile jsonFileS) {
        commonJsonParser.parseJson(jsonTask, jsonFileS);
    }

    @Override
    public boolean checkJson(JsonTask jsonTask, List<JsonFile> jsonFileList) {
        return commonJsonCheck.checkJson(jsonTask, jsonFileList);
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
        BigDecimal whitePulpArea = commonJsonParser.getOrganArea(jsonTask, "145047").getStructureAreaNum();
        //        动脉周围淋巴鞘面积	B	平方毫米	数据相加输出
        BigDecimal periarterialLymphaticSheathArea = commonJsonParser.getOrganArea(jsonTask, "145045").getStructureAreaNum();
        //        中央动脉面积	C	平方毫米	数据相加输出
        BigDecimal centralArteryArea = commonJsonParser.getOrganArea(jsonTask, "145048").getStructureAreaNum();
        //        含铁血黄素面积	D	平方毫米	数据相加输出（无Json数据）
        BigDecimal hemosiderinArea = commonJsonParser.getOrganArea(jsonTask, "14504D").getStructureAreaNum();
        //        红细胞面积	E	平方毫米	数据相加输出
        BigDecimal erythrocyteArea = commonJsonParser.getOrganArea(jsonTask, "145004").getStructureAreaNum();
        //        组织轮廓面积	F	平方毫米(H:精细轮廓总面积（脾脏）-平方毫米)
        String accurateArea = singleSlideMapper.selectById(jsonTask.getSingleId()).getArea();
        BigDecimal accurateAreaDecimal = new BigDecimal(accurateArea);
        //        边缘区面积	G	平方毫米	数据相加输出（算法直接输出）(WORD无数据，JSON有数据！)
        BigDecimal marginalZoneArea = commonJsonParser.getOrganArea(jsonTask, "14504A").getStructureAreaNum();
        //        红髓	H	平方毫米	算法直接输出 (WORD无数据，JSON有数据！)
        BigDecimal redPulpArea = commonJsonParser.getOrganArea(jsonTask, "145046").getStructureAreaNum();

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
        map.put("白髓面积", new IndicatorAddIn("White pulp area", DecimalUtils.setScale3(whitePulpArea), "平方毫米", CommonConstant.NUMBER_1));
        // B
        map.put("动脉周围淋巴鞘面积", new IndicatorAddIn("Periarterial lymphatic sheath area", DecimalUtils.setScale3(periarterialLymphaticSheathArea), "平方毫米", CommonConstant.NUMBER_1));
        // C
        map.put("中央动脉面积", new IndicatorAddIn("Central artery area", DecimalUtils.setScale3(centralArteryArea), "平方毫米", CommonConstant.NUMBER_1));
        // D
        map.put("含铁血黄素面积", new IndicatorAddIn("Hemosiderin area", DecimalUtils.setScale3(hemosiderinArea), "平方毫米", CommonConstant.NUMBER_1));
        // E
        map.put("红细胞面积", new IndicatorAddIn("Marginal zone area", DecimalUtils.setScale3(erythrocyteArea), "平方毫米", CommonConstant.NUMBER_1));
        // G
        map.put("边缘区面积", new IndicatorAddIn("Marginal zone area", DecimalUtils.setScale3(marginalZoneArea), "平方毫米", CommonConstant.NUMBER_1));
        // H
        map.put("红髓面积", new IndicatorAddIn("Red pulp", DecimalUtils.setScale3(redPulpArea), "平方毫米", CommonConstant.NUMBER_1));

        // 产品呈现指标 -------------------------------------------------------------
        if (accurateAreaDecimal.compareTo(BigDecimal.ZERO) != 0) {
            // 白髓面积占比	1	%	White pulp area%	1=A/F
            BigDecimal whitePulpAreaRate = whitePulpArea.divide(accurateAreaDecimal, 7, RoundingMode.HALF_UP);
            map.put("白髓面积占比", new IndicatorAddIn("White pulp area%", DecimalUtils.percentScale3(whitePulpAreaRate), "%"));

            // 含铁血黄素面积占比	2	%	White pulp area%%	2=D/F
            BigDecimal hemosiderinAreaRate = hemosiderinArea.divide(accurateAreaDecimal, 7, RoundingMode.HALF_UP);
            map.put("含铁血黄素面积占比", new IndicatorAddIn("Hemosiderin area%", DecimalUtils.percentScale3(hemosiderinAreaRate), "%"));

            // 红髓面积占比	3	%	Red pulp area%	3=H/F
            BigDecimal redPulpAreaRate = redPulpArea.divide(accurateAreaDecimal, 7, RoundingMode.HALF_UP);
            map.put("红髓面积占比", new IndicatorAddIn("Red pulp area%", DecimalUtils.percentScale3(redPulpAreaRate), "%"));

            // 红细胞面积占比	4	%	Erythrocyte area%	4=E/F
            BigDecimal erythrocyteAreaRate = erythrocyteArea.divide(accurateAreaDecimal, 7, RoundingMode.HALF_UP);
            map.put("红细胞面积占比", new IndicatorAddIn("Erythrocyte area%", DecimalUtils.percentScale3(erythrocyteAreaRate), "%"));

            // 边缘区面积占比	5	%	Marginal zone area%	5=G/F
            BigDecimal marginalZoneAreaRate = marginalZoneArea.divide(accurateAreaDecimal, 7, RoundingMode.HALF_UP);
            map.put("边缘区面积占比", new IndicatorAddIn("Marginal zone area", DecimalUtils.percentScale3(marginalZoneAreaRate), "%"));

            // 动脉周围淋巴鞘面积占比	6	%	Periarterial lymphatic sheath area%	6=（B-C）/F	包含淋巴滤泡
            BigDecimal periarterialLymphaticSheathAreaRate = periarterialLymphaticSheathArea.subtract(centralArteryArea).divide(accurateAreaDecimal, 7, RoundingMode.HALF_UP);
            map.put("动脉周围淋巴鞘面积占比", new IndicatorAddIn("Periarterial lymphatic sheath area%", DecimalUtils.percentScale3(periarterialLymphaticSheathAreaRate), "%"));
        } else {
            map.put("白髓面积占比", new IndicatorAddIn("White pulp area%", "0.000", "%"));
            map.put("含铁血黄素面积占比", new IndicatorAddIn("Hemosiderin area%", "0.000", "%"));
            map.put("红髓面积占比", new IndicatorAddIn("Red pulp area%", "0.000", "%"));
            map.put("红细胞面积占比", new IndicatorAddIn("Erythrocyte area%", "0.000", "%"));
            map.put("边缘区面积占比", new IndicatorAddIn("Marginal zone area", "0.000", "%"));
            map.put("动脉周围淋巴鞘面积占比", new IndicatorAddIn("Periarterial lymphatic sheath area%", "0.000", "%"));
        }

        // F
        map.put("脾脏面积", new IndicatorAddIn("Spleen area", DecimalUtils.setScale3(accurateAreaDecimal), "平方毫米"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
        log.info("指标计算结束-大鼠脾脏");
    }
}
