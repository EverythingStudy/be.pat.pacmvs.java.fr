package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
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
import cn.staitech.fr.utils.MathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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


    /**
     * 指标计算
     *
     * @param jsonTask
     */
    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("指标计算开始-哈氏腺");
        Map<String, IndicatorAddIn> map = new HashMap<>();

        //        哈氏腺
        //        腺泡	10206D
        //        腺泡细胞核	10206E
        //        色素	102071
        //        组织轮廓	102111
        //        10206D.json  10206E.json

        //        算法输出指标	指标代码（仅限本文档）	单位（保留三位小数点）	备注
        //        腺泡面积（单个）	A	103平方微米

        //        腺泡细胞核数量（单个）	B	个	单个腺泡内数据相加输出
        //        色素面积	C	平方毫米	数据相加输出
        // BigDecimal pigmentArea = commonJsonParser.getOrganArea(jsonTask, "102071").getStructureAreaNum();
        //        组织轮廓面积	D	平方毫米
        //        腺泡面积（全片）	E	平方毫米	数据相加输出
        BigDecimal acinusArea = commonJsonParser.getOrganArea(jsonTask, "10206D").getStructureAreaNum();
        //        腺泡细胞核数量（全片）	F	个	数据相加输出
        Integer nucleusCount = commonJsonParser.getOrganAreaCount(jsonTask, "10206E");

        //  产品呈现指标	指标代码（仅限本文档）	单位（保留三位小数点）	English	计算方式	备注
        //  腺泡面积占比（全片）	1	%	Acinus area%（all）	1=E/D
        //  腺泡细胞核密度(单个)	2	个/103平方微米	Nucleus density of acinus (per)	2=B/A	95%置信区间和均数±标准差
        //  色素面积占比	3	%	Pigment area%	3=C/D
        //  腺泡细胞核密度（全片）	4	个/平方毫米	Nucleus density of acinus (all)	4=F/E
        //  哈氏腺面积	5	平方毫米	Harderian gland
        //  area	5=D

        String accurateArea = singleSlideMapper.selectById(jsonTask.getSingleId()).getArea();
        BigDecimal accurateAreaBigDecimal = new BigDecimal(accurateArea);

        // 腺泡列表
        List<Annotation> structureContourList = commonJsonParser.getStructureContourList(jsonTask, "10206D");
        List<BigDecimal> listNum = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(structureContourList)) {
            for (Annotation annotation : structureContourList) {
                // A 腺泡面积（单个）	A	103平方微米
                BigDecimal structureAreaNum = annotation.getStructureAreaNum().multiply(new BigDecimal(1000));

                // B 腺泡细胞核数量（单个）	B	个	单个腺泡内数据相加输出
                Annotation contourInsideOrOutside2 = commonJsonParser.getContourInsideOrOutside(jsonTask, annotation.getContour(), "10206E", true);
                Integer count = contourInsideOrOutside2.getCount();

                // 2=B/A
                if (structureAreaNum.compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal divide = new BigDecimal(count).divide(structureAreaNum, 7, RoundingMode.HALF_UP);
                    listNum.add(divide);
                }
            }
        }

        String confidenceInterval = MathUtils.getConfidenceInterval(listNum);

        // B 腺泡细胞核数量（单个）	B	个	单个腺泡内数据相加输出
        Annotation annotationB = new Annotation();
        annotationB.setCountName("腺泡细胞核数量（单个）");
        annotationB.setCountUnit("个");
        commonJsonParser.putAnnotationDynamicData(jsonTask, "10206D", "10206E", annotationB);

        //  1：面积转10（3）平方微米  2:平方微米 （默认平方毫米）
        Annotation annotationC = new Annotation();
        annotationC.setAreaName("腺泡面积（单个）");
        annotationC.setAreaUnit("×10³平方微米");
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "10206D", annotationC, 1);

        // 算法输出指标 -------------------------------------------------------------
        // A
        map.put("腺泡面积（单个）", new IndicatorAddIn());
        // B
        map.put("腺泡细胞核数量（单个）", new IndicatorAddIn());
        // C
        // map.put("色素面积", new IndicatorAddIn("Pigment area", pigmentArea.toString(), "平方毫米", CommonConstant.NUMBER_1));
        // E
        map.put("腺泡面积（全片）", new IndicatorAddIn("Acinus area (all)", DecimalUtils.setScale3(acinusArea), "平方毫米", CommonConstant.NUMBER_1));
        // F
        map.put("腺泡细胞核数量（全片）", new IndicatorAddIn("Nucleus counts of acinus (all)", nucleusCount.toString(), "个", CommonConstant.NUMBER_1));

        // 产品呈现指标 -------------------------------------------------------------
        if (accurateAreaBigDecimal.compareTo(BigDecimal.ZERO) != 0) {
            //   腺泡面积占比（全片）	1	%	Acinus area%（all）	1=E/D
            BigDecimal acinusDivideArea = acinusArea.divide(accurateAreaBigDecimal, 7, BigDecimal.ROUND_HALF_UP);
            map.put("腺泡面积占比（全片）", new IndicatorAddIn("Acinus area %（all）", DecimalUtils.percentScale3(acinusDivideArea), "%"));

            // 色素面积占比 3 % Pigment area % 3 = C / D
            // BigDecimal pigmentDivideArea = pigmentArea.divide(accurateAreaBigDecimal, 7, BigDecimal.ROUND_HALF_UP);
            // map.put("色素面积占比", new IndicatorAddIn("Pigment area %", DecimalUtils.percentScale3(pigmentDivideArea), "%"));
        } else {
            map.put("腺泡面积占比（全片）", new IndicatorAddIn("Acinus area %（all）", "0.000", "%"));
            // map.put("色素面积占比", new IndicatorAddIn("Pigment area %", "0.000", "%"));
        }

        // 腺泡细胞核密度(单个) 2 个 / 103 平方微米 Nucleus density of acinus(per) 2 = B / A 95 % 置信区间和均数±标准差
        map.put("腺泡细胞核密度(单个)", new IndicatorAddIn("Nucleus density of acinus(per)", confidenceInterval, "个/10³平方微米"));

        // 腺泡细胞核密度（全片）4 个 / 平方毫米 Nucleus density of acinus (all) 4 = F / E
        BigDecimal nucleusCountDivideacinusArea = new BigDecimal(nucleusCount).divide(acinusArea, 7, BigDecimal.ROUND_HALF_UP);
        map.put("腺泡细胞核密度（全片）", new IndicatorAddIn("Nucleus density of acinus (all)", DecimalUtils.setScale3(nucleusCountDivideacinusArea), "个/平方毫米"));

        // D
        map.put("哈氏腺面积", new IndicatorAddIn("Acinus area", DecimalUtils.setScale3(accurateAreaBigDecimal), "平方毫米"));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
        log.info("指标计算结束-哈氏腺");
    }
}
