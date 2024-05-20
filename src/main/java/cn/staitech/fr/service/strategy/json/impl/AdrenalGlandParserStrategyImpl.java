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
import java.util.HashMap;
import java.util.Map;

/**
 * @author mugw
 * @version 1.0
 * @description
 * @date 2024/5/13 10:06:53
 */
@Slf4j
@Service("AdrenalGland")
public class AdrenalGlandParserStrategyImpl extends AbstractCustomParserStrategy {
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        log.info("AdrenalGlandParserStrategyImpl init");
    }

    /**
     * 皮质	10103D
     * 髓质	10103E
     * 肾上腺实质细胞核	101068
     * 红细胞	101004
     * 组织轮廓	101111
     * 算法输出指标	指标代码（仅限本文档）	单位（保留小数点后三位）	备注
     * 皮质面积	A	平方毫米
     * 髓质面积	B	平方毫米
     * 皮质细胞核数量	C	个
     * 髓质细胞核数量	D	个
     * 红细胞面积（全片）	E	平方毫米
     * 组织轮廓面积	F	平方毫米
     */

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        BigDecimal unit = new BigDecimal(1000000);
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal organArea = getOrganArea(jsonTask, "10103D",unit).getStructureAreaNum();
        BigDecimal organArea1 = getOrganArea(jsonTask, "101004",unit).getStructureAreaNum();
        BigDecimal organArea2 = getOrganArea(jsonTask, "10103E").getStructureAreaNum();
        Integer count = getOrganAreaCount(jsonTask, "10103D");
        Integer count1 = getOrganAreaCount(jsonTask, "10103E");
        indicatorResultsMap.put("皮质面积", new IndicatorAddIn("", organArea.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("红细胞面积（全片）", new IndicatorAddIn("", organArea1.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("髓质面积", new IndicatorAddIn("", organArea2.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("皮质细胞核数量", new IndicatorAddIn("", String.valueOf(count), "个", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("髓质细胞核数量", new IndicatorAddIn("", String.valueOf(count1), "个", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("组织轮廓面积", new IndicatorAddIn("", singleSlide.getArea(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("肾上腺面积", new IndicatorAddIn("Adrenal gland area%", singleSlide.getArea(), "平方毫米"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Adrenal_gland";
    }
}
