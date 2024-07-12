package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


/**
 * @author mugw
 * @version 1.0
 * @description
 * @date 2024/5/13 10:06:53
 */
@Slf4j
@Service("Sublingual_gland")
public class SublingualGlandParserStrategyImpl extends AbstractCustomParserStrategy {

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
        log.debug("SublingualGlandParserStrategyImpl init");
    }

    /**
     * 导管	10A06F
     * 导管内腔	10A121
     * 腺泡	10A06D
     * 组织轮廓	10A111
     * 算法输出指标	指标代码（仅限本文档）	单位（保留小数点后三位）	备注
     * 导管面积（单个）	A	103平方微米	单个导管面积
     * 导管面积（全片）	B	103平方微米	若多个数据则相加输出
     * 导管内腔面积（单个）	C	平方微米	若单个导管内有多个导管内腔，则相加输出
     * 腺泡面积	D	平方毫米	若多个数据则相加输出
     * 腺泡数量	E	个
     * 组织轮廓	F	平方毫米	若多个数据则相加输出
     *
     * 产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后三位）	English	计算方式	备注
     * 舌下腺面积	1	平方毫米	Sublingual gland area	1=F
     * 导管上皮面积占比（单个）	2	%	Duct epithelium area% (per)	2=(A-C)/A	以95%置信区间和均数±标准差呈现；运算前注意统一单位
     * 导管面积占比（全片）	3	%	Ducts area% (all)	3=B/F	运算前注意统一单位
     * 腺泡面积占比	4	%	Acinus area%	4=D/F
     * 腺泡平均面积	5	103平方微米	Average area of acinus	5=D/E
     *
     * @param jsonTask
     */
    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        BigDecimal unit = new BigDecimal(1000);
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal organArea = getOrganArea(jsonTask, "10A06F", unit).getStructureAreaNum();
        BigDecimal organArea2 = getOrganArea(jsonTask, "10A06D").getStructureAreaNum();
        Integer count = getOrganAreaCount(jsonTask, "10A06D");
        indicatorResultsMap.put("导管面积（全片）", new IndicatorAddIn("", organArea.setScale(3, RoundingMode.HALF_UP).toString(), "×10³平方微米", CommonConstant.NUMBER_1));

        indicatorResultsMap.put("腺泡面积", new IndicatorAddIn("", organArea2.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));

        indicatorResultsMap.put("腺泡数量", new IndicatorAddIn("", String.valueOf(count), "个", CommonConstant.NUMBER_1));
        //indicatorResultsMap.put("组织轮廓", new IndicatorAddIn("", singleSlide.getArea(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("舌下腺面积", new IndicatorAddIn("Sublingual Gland area%", new BigDecimal(singleSlide.getArea()).setScale(3, RoundingMode.DOWN).toString(), "平方毫米", CommonConstant.NUMBER_0));
        indicatorResultsMap.put("导管面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1));
        indicatorResultsMap.put("导管内腔面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1));

        indicatorResultsMap.put("导管面积占比（全片）", new IndicatorAddIn("Ducts area% (all)", commonJsonParser.getProportion(organArea, new BigDecimal(singleSlide.getArea()).multiply(BigDecimal.valueOf(1000L))).toString(), "%", CommonConstant.NUMBER_0));
        indicatorResultsMap.put("腺泡面积占比", new IndicatorAddIn("Acinus area%", commonJsonParser.getProportion(organArea2, new BigDecimal(singleSlide.getArea())).toString(), "%", CommonConstant.NUMBER_0));
        indicatorResultsMap.put("腺泡平均面积", new IndicatorAddIn("Average area of acinus", commonJsonParser.getProportionMultiply(organArea2.multiply(BigDecimal.valueOf(1000L)), new BigDecimal(count)).toString(), "×10³平方微米", CommonConstant.NUMBER_0));

        List<Annotation> as = getStructureContourList(jsonTask, "10A06F");
        List<BigDecimal> dataList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(as)){
            for (Annotation a : as){
                Annotation temp = getContourInsideOrOutside(jsonTask,a.getContour(),"10A121",true);
                BigDecimal sub = a.getStructureAreaNum().subtract(temp.getStructureAreaNum());
                dataList.add(commonJsonParser.getProportion(sub, a.getStructureAreaNum()));
            }
        }
        indicatorResultsMap.put("导管上皮面积占比（单个）", createComplexIndicator(dataList, "Duct epithelium area% (per)", "%", CommonConstant.NUMBER_0));
        Annotation annotationBy = new Annotation();
        annotationBy.setAreaName("导管面积（单个）");
        annotationBy.setAreaUnit("×10³平方微米");
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask,"10A06F",annotationBy,1);
        annotationBy.setAreaName("导管内腔面积（单个）");
        annotationBy.setAreaUnit("平方微米");
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask,"10A121",annotationBy,2);
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Sublingual_gland";
    }
}
