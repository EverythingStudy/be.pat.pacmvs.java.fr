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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: wangfeng
 * @create: 2024-05-10 14:18:48
 * @Description: Lacrimal_gland Json Parser 泪腺
 */
@Slf4j
@Component("Lacrimal_gland")
public class LacrimalGlandParserStrategyImpl implements ParserStrategy {
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
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();

        //        泪腺
        //
        //        结构	编码
        //        导管	16906F
        //        腺泡细胞核	16906E
        //        上皮顶部胞质	16906A
        //        间质	169027
        //        组织轮廓	169111
        //        169027.json  16906A.json  16906E.json  16906F.json

        //        算法输出指标	指标代码（仅限本文档）	单位（保留小数点后三位）	备注
        //        导管面积	A	平方毫米	数据相加输出
        BigDecimal ductArea = commonJsonParser.getOrganArea(jsonTask, "16906F").getStructureAreaNum();
        //        腺泡细胞核数量	B	个	无
        Integer nucleusCount = commonJsonParser.getOrganAreaCount(jsonTask, "16906E");
        //        腺泡细胞核面积（单个）	C	平方微米	单个数值输出
        //        上皮顶部胞质面积	D	平方毫米	数据相加输出
        BigDecimal epithelialApexCytoplasmArea = commonJsonParser.getOrganArea(jsonTask, "16906A").getStructureAreaNum();
        //        间质面积	E	平方毫米	数据相加输出
        BigDecimal mesenchymeArea = commonJsonParser.getOrganArea(jsonTask, "169027").getStructureAreaNum();
        //        组织轮廓面积	F	平方毫米	无
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());

        //        产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后三位）	English	计算方式	备注
        //        导管占比	1	无	Duct area%	1=A/F
        //        腺泡细胞核密度	2	个/平方毫米	Nucleus density of acinus	2=B/(F-E)
        //        上皮顶部胞质占比	3	无	Epithelial apex cytoplasm area%	3=D/F
        //        间质占比	4	无	Mesenchyme area%	4=E/F
        //        腺泡占比	5	无	Acinus area%	5=(F-E)/F
        //        腺泡细胞核面积（单个）	6	平方微米	Acinar nucleus area (per)	6=C	以95%置信区间和均数±标准差呈现
        //        泪腺面积	7	平方毫米	Lacrimal gland area	7=F

        // A
        indicatorResultsMap.put("导管面积", new IndicatorAddIn("Duct area", ductArea.toString(), "平方毫米", CommonConstant.NUMBER_1));
        // B
        indicatorResultsMap.put("腺泡细胞核数量", new IndicatorAddIn("Nucleus counts of acinus", nucleusCount.toString(), "个", CommonConstant.NUMBER_1));
        // C
        indicatorResultsMap.put("腺泡细胞核面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1));
        // D
        indicatorResultsMap.put("上皮顶部胞质面积", new IndicatorAddIn("Epithelial apex cytoplasm area", epithelialApexCytoplasmArea.toString(), "平方毫米", CommonConstant.NUMBER_1));
        // E
        indicatorResultsMap.put("间质面积", new IndicatorAddIn("Mesenchyme area", mesenchymeArea.toString(), "平方毫米", CommonConstant.NUMBER_1));

        // 产品呈现指标 -------------------------------------------------------------
        //         导管占比	1	无	Duct area%	1=A/F
        BigDecimal ductDivideSingleSlideArea = ductArea.divide(new BigDecimal(singleSlide.getArea()), 3, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        indicatorResultsMap.put("导管占比", new IndicatorAddIn("Duct area%", ductDivideSingleSlideArea.toString(), "无"));

        // 腺泡细胞核密度 2 个 / 平方毫米 Nucleus density of acinus 2 = B / (F - E)
        BigDecimal nucleusDensityOfAcinus = new BigDecimal(nucleusCount).divide(new BigDecimal(singleSlide.getArea()).subtract(mesenchymeArea), 3, BigDecimal.ROUND_HALF_UP);
        indicatorResultsMap.put("腺泡细胞核密度", new IndicatorAddIn("Nucleus density of acinus", nucleusDensityOfAcinus.toString(), "个/平方毫米"));

        // 上皮顶部胞质占比 3 无 Epithelial apex cytoplasm area % 3 = D / F
        BigDecimal epithelialApexCytoplasmRate = epithelialApexCytoplasmArea.divide(new BigDecimal(singleSlide.getArea()), 3, BigDecimal.ROUND_HALF_UP);
        indicatorResultsMap.put("上皮顶部胞质占比", new IndicatorAddIn("Epithelial apex cytoplasm area %", epithelialApexCytoplasmRate.toString(), "无"));

        // 间质占比 4 无 Mesenchyme area % 4 = E / F
        BigDecimal mesenchymeAreaRate = mesenchymeArea.divide(new BigDecimal(singleSlide.getArea()), 3, BigDecimal.ROUND_HALF_UP);
        indicatorResultsMap.put("间质占比", new IndicatorAddIn("Mesenchyme area %", mesenchymeAreaRate.toString(), "无"));

        // 腺泡占比 5 无 Acinus area % 5 = (F - E) / F
        BigDecimal acinusAreaRate = new BigDecimal(singleSlide.getArea()).subtract(mesenchymeArea).divide(new BigDecimal(singleSlide.getArea()), 3, BigDecimal.ROUND_HALF_UP);
        indicatorResultsMap.put("腺泡占比", new IndicatorAddIn("Acinus area %", acinusAreaRate.toString(), "无"));

        // 腺泡细胞核面积（单个）6 平方微米 Acinar nucleus area (per) 6 = C 以95 % 置信区间和均数±标准差呈现 TODO
        indicatorResultsMap.put("腺泡细胞核面积（单个）", new IndicatorAddIn("Acinar nucleus area (per) ", mesenchymeAreaRate.toString(), "无"));

        // F 泪腺面积 7 平方毫米 Lacrimal gland area 7 = F
        indicatorResultsMap.put("泪腺面积", new IndicatorAddIn("Lacrimal gland area", singleSlide.getArea(), "平方毫米"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
