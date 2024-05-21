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
import java.util.HashMap;
import java.util.Map;

/**
 * @author mugw
 * @version 1.0
 * @description
 * @date 2024/5/13 10:06:53
 */
@Slf4j
@Service("Kidney")
public class KidneyParserStrategyImpl extends AbstractCustomParserStrategy {
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        log.info("KidneyParserStrategyImpl init");
    }
    /**
     * 结构	编码
     * 髓质	11B03E
     * 肾小球	11B02D
     * 球内细胞核	11B02E
     * 球内红细胞	11B02F
     * 肾小管	11B031
     * 组织轮廓	11B111
     * 算法输出指标	指标代码（仅限本文档）	单位（保留小数点后三位）	备注
     * 肾皮质面积	A	平方毫米	无
     * 肾小球面积（单个）	B	103平方微米	无
     * 球内细胞核数量（单个）	C	个	单个=单个肾小球
     * 球内红细胞面积（单个）	D	103平方微米	单个=单个肾小球
     * 肾小管数量	E	个	无
     * 肾小管面积（单个）	F	103平方微米	无
     * 组织轮廓面积	G	平方毫米	无
     * @param jsonTask
     */
    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        Integer count = getOrganAreaCount(jsonTask, "11B031");
        indicatorResultsMap.put("肾小管数量", new IndicatorAddIn("", String.valueOf(count), "个", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("组织轮廓", new IndicatorAddIn("", singleSlide.getArea(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("肾脏面积", new IndicatorAddIn("Renal area%", singleSlide.getArea(), "平方毫米", CommonConstant.NUMBER_0));

        indicatorResultsMap.put("肾小球面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT,CommonConstant.NUMBER_1));
        indicatorResultsMap.put("球内红细胞面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT,CommonConstant.NUMBER_1));
        indicatorResultsMap.put("肾小管面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT,CommonConstant.NUMBER_1));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Kidney";
    }
}
