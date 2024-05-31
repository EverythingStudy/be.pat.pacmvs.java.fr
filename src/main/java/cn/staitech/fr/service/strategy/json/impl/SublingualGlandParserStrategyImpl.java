package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import lombok.extern.slf4j.Slf4j;
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

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
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
        indicatorResultsMap.put("导管面积（全片）", new IndicatorAddIn("", organArea.setScale(3, RoundingMode.HALF_UP).toString(), "10³平方微米", CommonConstant.NUMBER_1));

        indicatorResultsMap.put("腺泡面积", new IndicatorAddIn("", organArea2.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));

        indicatorResultsMap.put("腺泡数量", new IndicatorAddIn("", String.valueOf(count), "个", CommonConstant.NUMBER_1));
        //indicatorResultsMap.put("组织轮廓", new IndicatorAddIn("", singleSlide.getArea(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("舌下腺面积", new IndicatorAddIn("Sublingual Gland area%", singleSlide.getArea(), "平方毫米", CommonConstant.NUMBER_0));
        indicatorResultsMap.put("导管面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1));
        indicatorResultsMap.put("导管内腔面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Sublingual_gland";
    }
}
