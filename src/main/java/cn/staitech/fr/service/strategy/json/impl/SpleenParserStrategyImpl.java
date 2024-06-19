package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.service.strategy.json.ParserStrategy;
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
        log.info("大鼠脾脏指标计算开始……{}", jsonTask);
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        //        脾脏
        //
        //        结构	编码
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
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        String accurateArea = singleSlide.getArea();
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
        indicatorResultsMap.put("白髓面积", new IndicatorAddIn("White pulp area", whitePulpArea.toString(), "平方毫米", CommonConstant.NUMBER_1));
        // B
        indicatorResultsMap.put("动脉周围淋巴鞘面积", new IndicatorAddIn("Periarterial lymphatic sheath area", periarterialLymphaticSheathArea.toString(), "平方毫米", CommonConstant.NUMBER_1));
        // C
        indicatorResultsMap.put("中央动脉面积", new IndicatorAddIn("Central artery area", centralArteryArea.toString(), "平方毫米", CommonConstant.NUMBER_1));
        // D
        indicatorResultsMap.put("含铁血黄素面积", new IndicatorAddIn("Hemosiderin area", hemosiderinArea.toString(), "平方毫米", CommonConstant.NUMBER_1));
        // E
        indicatorResultsMap.put("红细胞面积", new IndicatorAddIn("Marginal zone area", erythrocyteArea.toString(), "平方毫米", CommonConstant.NUMBER_1));
        // G
        indicatorResultsMap.put("边缘区面积", new IndicatorAddIn("Marginal zone area", marginalZoneArea.toString(), "平方毫米", CommonConstant.NUMBER_1));
        // H
        indicatorResultsMap.put("红髓面积", new IndicatorAddIn("Red pulp", redPulpArea.toString(), "平方毫米", CommonConstant.NUMBER_1));

        // 产品呈现指标 -------------------------------------------------------------
        // 白髓面积占比	1	%	White pulp area%	1=A/F
        String whitePulpAreaRate = whitePulpArea.divide(accurateAreaDecimal).setScale(3, RoundingMode.HALF_UP).toString();
        indicatorResultsMap.put("白髓面积占比", new IndicatorAddIn("White pulp area%", whitePulpAreaRate, "%"));

        // 含铁血黄素面积占比	2	%	White pulp area%%	2=D/F
        String hemosiderinAreaRate = hemosiderinArea.divide(accurateAreaDecimal).setScale(3, RoundingMode.HALF_UP).toString();
        indicatorResultsMap.put("含铁血黄素面积占比", new IndicatorAddIn("Hemosiderin area%", hemosiderinAreaRate, "%"));

        // 红髓面积占比	3	%	Red pulp area%	3=H/F
        String redPulpAreaRate = redPulpArea.divide(accurateAreaDecimal).setScale(3, RoundingMode.HALF_UP).toString();
        indicatorResultsMap.put("红髓面积占比", new IndicatorAddIn("Red pulp area%", redPulpAreaRate, "%"));

        // 红细胞面积占比	4	%	Erythrocyte area%	4=E/F
        String erythrocyteAreaRate = erythrocyteArea.divide(accurateAreaDecimal).setScale(3, RoundingMode.HALF_UP).toString();
        indicatorResultsMap.put("红细胞面积占比", new IndicatorAddIn("Erythrocyte area%", erythrocyteAreaRate, "%"));

        // 边缘区面积占比	5	%	Marginal zone area%	5=G/F
        String marginalZoneAreaRate = marginalZoneArea.divide(accurateAreaDecimal).setScale(3, RoundingMode.HALF_UP).toString();
        indicatorResultsMap.put("边缘区面积占比", new IndicatorAddIn("Marginal zone area", marginalZoneAreaRate, "%"));

        // 动脉周围淋巴鞘面积占比	6	%	Periarterial lymphatic sheath area%	6=（B-C）/F	包含淋巴滤泡
        String periarterialLymphaticSheathAreaRate = periarterialLymphaticSheathArea.subtract(centralArteryArea).divide(accurateAreaDecimal).setScale(3, RoundingMode.HALF_UP).toString();
        indicatorResultsMap.put("动脉周围淋巴鞘面积占比", new IndicatorAddIn("Periarterial lymphatic sheath area%", periarterialLymphaticSheathAreaRate, "%"));

        // F
        indicatorResultsMap.put("脾脏面积", new IndicatorAddIn("Spleen area", accurateArea, "平方毫米"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
