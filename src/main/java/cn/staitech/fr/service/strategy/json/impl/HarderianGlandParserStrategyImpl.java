package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.service.strategy.json.ParserStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: wangfeng
 * @create: 2024-05-10 14:18:48
 * @Description: Harderian_gland Json Parser 哈氏腺 Harderian_gland
 */
@Slf4j
@Component("Harderian_gland")
public class HarderianGlandParserStrategyImpl implements ParserStrategy {

    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;

    @Override
    public void parseJson(JsonTask jsonTask, JsonFile jsonFileS) {
        commonJsonParser.parseJson(jsonTask, jsonFileS);
    }


    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();

        //        哈氏腺
        //
        //        结构	编码
        //        腺泡	10206D
        //        腺泡细胞核	10206E
        //        色素	102071
        //        组织轮廓	102111
        //        10206D.json  10206E.json

        //        算法输出指标	指标代码（仅限本文档）	单位（保留三位小数点）	备注
        //        腺泡面积（单个）	A	103平方微米
        BigDecimal acinusAreaPer = commonJsonParser.getOrganAreaMicron(jsonTask, "10206D");
        //        腺泡细胞核数量（单个）	B	个	单个腺泡内数据相加输出
        Integer nucleusCountPer = commonJsonParser.getOrganAreaCount(jsonTask, "10206E");
        //        色素面积	C	平方毫米	数据相加输出
        BigDecimal pigmentArea = commonJsonParser.getOrganArea(jsonTask, "102071").getStructureAreaNum();
        //        组织轮廓面积	D	平方毫米
        //        腺泡面积（全片）	E	平方毫米	数据相加输出
        BigDecimal acinusArea = commonJsonParser.getOrganArea(jsonTask, "10206D").getStructureAreaNum();
        //        腺泡细胞核数量（全片）	F	个	数据相加输出
        Integer nucleusCount = commonJsonParser.getOrganAreaCount(jsonTask, "10206E");

        //        产品呈现指标	指标代码（仅限本文档）	单位（保留三位小数点）	English	计算方式	备注
        //        腺泡面积占比（全片）	1	%	Acinus area%（all）	1=E/D
        //        腺泡细胞核密度(单个)	2	个/103平方微米	Nucleus density of acinus (per)	2=B/A	95%置信区间和均数±标准差
        //        色素面积占比	3	%	Pigment area%	3=C/D
        //        腺泡细胞核密度（全片）	4	个/平方毫米	Nucleus density of acinus (all)	4=F/E
        //        哈氏腺面积	5	平方毫米	Harderian gland
        //        area	5=D

        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        String accurateArea = singleSlide.getArea();

        indicatorResultsMap.put("腺泡面积（单个）", new IndicatorAddIn("Acinus area (per)", acinusAreaPer.setScale(3, RoundingMode.HALF_UP).toString(), "10³平方微米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("腺泡细胞核数量（单个）", new IndicatorAddIn("Nucleus counts of acinus (per)", nucleusCountPer.toString(), "个", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("色素面积", new IndicatorAddIn("Pigment area", pigmentArea.toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("腺泡面积（全片）", new IndicatorAddIn("Acinus area (all)", acinusArea.toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("腺泡细胞核数量（全片）", new IndicatorAddIn("Nucleus counts of acinus (all)", nucleusCount.toString(), "个", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("哈氏腺面积", new IndicatorAddIn("Acinus area", accurateArea, "平方毫米"));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
