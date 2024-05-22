package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
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
@Service("Thymus")
public class ThymusParserStrategyImpl extends AbstractCustomParserStrategy {
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        log.debug("ThymusParserStrategyImpl init");
    }


    /**
     *     皮质	14403D
     *     髓质	14403E
     *     结缔组织	14403F
     *     红细胞	145004
     *     组织轮廓	144111
     *
     *
     *     皮质面积	A	平方毫米	数据相加输出
     *     髓质面积	B	平方毫米	数据相加输出
     *     髓质内结缔组织面积	C	平方毫米
     *     髓质外结缔组织面积	D	平方毫米
     *     红细胞面积	E	平方毫米	数据相加输出
     *     组织轮廓	F	平方毫米
     * @param jsonTask
     */

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        BigDecimal organArea = getOrganArea(jsonTask, "14403D").getStructureAreaNum();
        BigDecimal organArea2 = getOrganArea(jsonTask, "14403E").getStructureAreaNum();
        BigDecimal organArea3 = getOrganArea(jsonTask, "145004").getStructureAreaNum();
        indicatorResultsMap.put("皮质面积", new IndicatorAddIn("", organArea.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("髓质面积", new IndicatorAddIn("", organArea2.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("红细胞", new IndicatorAddIn("", organArea3.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("组织轮廓", new IndicatorAddIn("", singleSlide.getArea(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("胸腺面积", new IndicatorAddIn("Thymus Gland area%", singleSlide.getArea(), "平方毫米",CommonConstant.NUMBER_0));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Thymus";
    }
}
