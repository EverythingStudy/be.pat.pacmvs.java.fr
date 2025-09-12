package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.service.strategy.json.OutlineCustom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mugw
 * @version 1.0
 * @description 胸腺
 * @date 2024/5/13 10:06:53
 */
@Slf4j
@Service("Thymus")
public class ThymusParserStrategyImpl extends AbstractCustomParserStrategy implements OutlineCustom {
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
        log.debug("ThymusParserStrategyImpl init");
    }


    /**
     * 皮质	14403D
     * 髓质	14403E
     * 结缔组织	14403F
     * 红细胞	145004
     * 组织轮廓	144111
     * <p>
     * <p>
     * 皮质面积	A	平方毫米	数据相加输出
     * 髓质面积	B	平方毫米	数据相加输出
     * 髓质内结缔组织面积	C	平方毫米
     * 髓质外结缔组织面积	D	平方毫米
     * 红细胞面积	E	平方毫米	数据相加输出
     * 组织轮廓	F	平方毫米
     * <p>
     * 产品呈现指标	指标代码（仅限本文档）	单位(保留3位小数)	English	计算方式	备注
     * 皮质占比	1	%	Cortex area%	1=（F-B-D）/F
     * 髓质占比	2	%	Medulla area%	2=B/F
     * 皮髓比	3	%	Cortex:medulla ratio	3=（F-B-D）/B
     * 红细胞面积占比	4	%	Erythrocyte area%	4=E/F
     * 结缔组织面积占比	5	%	Connective tissue area%	5=（C+D）/F
     * 胸腺面积	6	平方毫米	Thymus area	6=F
     *
     * @param jsonTask
     */

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        BigDecimal organArea = getOrganArea(jsonTask, "14403D").getStructureAreaNum();
        BigDecimal organArea1 = getOrganArea(jsonTask, "14403F").getStructureAreaNum();
        BigDecimal organArea2 = getOrganArea(jsonTask, "14403E").getStructureAreaNum();
        //F
        BigDecimal organArea3 = getOrganArea(jsonTask, "145004").getStructureAreaNum();
        BigDecimal outLine = new BigDecimal(singleSlide.getArea());
        //A
        //indicatorResultsMap.put("皮质外轮廓面积", createIndicator(organArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "14403D"));
        //B
        //indicatorResultsMap.put("髓质外轮廓面积", createIndicator(organArea2.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "14403E"));
        //C
        indicatorResultsMap.put("结缔组织面积", createIndicator(organArea1.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "14403F"));
        //D
        Annotation annotation = commonJsonParser.getInsideOrOutside(jsonTask, "14403E", "14403F", true);
        BigDecimal organArea4 = annotation.getStructureAreaNum();
        //indicatorResultsMap.put("髓质内结缔组织面积", createIndicator(organArea4.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "14403E,14403F"));
        //E
        Annotation annotation1 = commonJsonParser.getInsideOrOutside(jsonTask, "14403E", "14403F", false);
        BigDecimal organArea5 = annotation1.getStructureAreaNum();
        //indicatorResultsMap.put("髓质外结缔组织面积", createIndicator(organArea5.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "14403E,14403F"));
        //F
        indicatorResultsMap.put("红细胞面积", createIndicator(organArea3.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "145004"));
        //G
        //indicatorResultsMap.put("组织轮廓", createIndicator(outLine, "平方毫米", "144111"));

        //indicatorResultsMap.put("皮质面积", new IndicatorAddIn("", F.subtract(A).setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_0));
        //indicatorResultsMap.put("髓质面积", createIndicator(organArea2.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_0));
        //3=(A-B-E)/A
        BigDecimal b3 = getProportion(organArea.subtract(organArea2).subtract(organArea4), organArea);
        //indicatorResultsMap.put("皮质占比", createNameIndicator("Cortex area%", String.valueOf(b3), "%", "14403D,14403E,14403F"));
        //4=(B-D)/A
        BigDecimal b4 = getProportion(organArea2.subtract(organArea4), organArea);
        //indicatorResultsMap.put("髓质占比", createNameIndicator("Medulla area%", String.valueOf(b4), "%", "14403D,14403E,14403F"));
        //5=(A-B-E)/(B-D)
        BigDecimal b5 = getProportion(organArea.subtract(organArea2).subtract(organArea4), organArea2.subtract(organArea4));
        //indicatorResultsMap.put("皮髓比", createNameIndicator("Cortex:medulla ratio", String.valueOf(b5), "%", "14403D,14403E,14403F"));
        //6=F/A
        BigDecimal F = outLine;
        BigDecimal b6 = getProportion(F, organArea);
        indicatorResultsMap.put("红细胞面积占比", createNameIndicator("Erythrocyte area%", String.valueOf(b6), PERCENTAGE, "145004,14403D"));
        //7=C/A
        BigDecimal b7 = getProportion(organArea1, organArea);
        indicatorResultsMap.put("结缔组织面积占比", createNameIndicator("Connective tissue area%", String.valueOf(b7), PERCENTAGE, "14403D,14403F"));
        //8=G
        indicatorResultsMap.put("胸腺面积", createNameIndicator("Thymus area", outLine.setScale(3, RoundingMode.DOWN).toString(), SQ_MM, "144111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Thymus";
    }

    @Override
    public void getCustomOutLine(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal pituitaryH = new BigDecimal(singleSlide.getArea());
        indicatorResultsMap.put("胸腺面积", createNameIndicator("Thymus area", String.valueOf(pituitaryH.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "144111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
