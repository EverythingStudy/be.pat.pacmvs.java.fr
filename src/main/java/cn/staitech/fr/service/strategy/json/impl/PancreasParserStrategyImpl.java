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
 * @description
 * @date 2024/5/13 10:06:53
 */
@Slf4j
@Service("Pancreas")
public class PancreasParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.debug("PancreasParserStrategyImpl init");
    }

    /**
     * 上皮细胞核	105075
     * 酶原颗粒	105076
     * 胰岛	105077
     * 胰岛细胞核  105078
     * 间质	105027
     * 导管	10506F
     * 导管细胞核	10507B
     * 血管	105003
     * 红细胞	105004
     * 组织轮廓	105111
     * 算法输出指标	指标代码（仅限本文档）	单位（保留小数点后三位）	备注
     * 上皮细胞核数量	A	个	无
     * 酶原颗粒面积	B	平方毫米	数据相加输出
     * 胰岛数量	C	个	无
     * 胰岛面积（单个）	D	103平方微米	单个胰岛面积输出
     * 胰岛面积（全片）	E	平方毫米	数据相加输出
     * 胰岛细胞核数量（单个）	F	个	单个胰岛内胰岛细胞核数量输出
     * 间质面积	G	平方毫米	数据相加输出
     * 导管数量	H	个	无
     * 导管面积（单个）	I	103平方微米	单个导管面积输出
     * 导管面积（全片）	J	平方毫米	数据相加输出
     * 导管细胞核数量（单个）	K	个	单个导管内导管细胞核数量输出
     * 血管面积	L	平方毫米	无
     * 血管内红细胞面积
     * 	M	平方毫米	数据相加输出
     * 血管外红细胞面积
     * 	N	平方毫米	数据相加输出
     * 组织轮廓面积	O	平方毫米	数据相加输出
     * 胰岛细胞核数量（全片）	P	个	无
     * @param jsonTask
     */
    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal organArea = getOrganArea(jsonTask, "105076").getStructureAreaNum();
        BigDecimal organArea1 = getOrganArea(jsonTask, "105077").getStructureAreaNum();
        BigDecimal organArea2 = getOrganArea(jsonTask, "105027").getStructureAreaNum();
        BigDecimal organArea3 = getOrganArea(jsonTask, "10506F").getStructureAreaNum();
        BigDecimal organArea4 = getOrganArea(jsonTask, "105003").getStructureAreaNum();
        Integer count = getOrganAreaCount(jsonTask, "105075");
        Integer count1 = getOrganAreaCount(jsonTask, "105077");
        Integer count2 = getOrganAreaCount(jsonTask, "10506F");
        Integer count3 = getOrganAreaCount(jsonTask, "105078");
            indicatorResultsMap.put("酶原颗粒面积", new IndicatorAddIn("", organArea.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
            indicatorResultsMap.put("胰岛面积（全片）", new IndicatorAddIn("", organArea1.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));

            indicatorResultsMap.put("间质面积", new IndicatorAddIn("", organArea2.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));

            indicatorResultsMap.put("导管面积（全片）", new IndicatorAddIn("", organArea3.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));

            indicatorResultsMap.put("血管面积", new IndicatorAddIn("", organArea4.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));

            indicatorResultsMap.put("上皮细胞核数量", new IndicatorAddIn("", String.valueOf(count), "个", CommonConstant.NUMBER_1));

            indicatorResultsMap.put("胰岛数量", new IndicatorAddIn("", String.valueOf(count1), "个", CommonConstant.NUMBER_1));

            indicatorResultsMap.put("导管数量", new IndicatorAddIn("", String.valueOf(count2), "个", CommonConstant.NUMBER_1));

            indicatorResultsMap.put("胰岛细胞核数量（全片）", new IndicatorAddIn("", String.valueOf(count3), "个", CommonConstant.NUMBER_1));
        //indicatorResultsMap.put("组织轮廓面积", new IndicatorAddIn("", singleSlide.getArea(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("胰腺面积", new IndicatorAddIn("Pancreas area%", singleSlide.getArea(), "平方毫米", CommonConstant.NUMBER_0));

        indicatorResultsMap.put("胰岛面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT,CommonConstant.NUMBER_1));
        indicatorResultsMap.put("导管面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT,CommonConstant.NUMBER_1));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Pancreas";
    }
}
