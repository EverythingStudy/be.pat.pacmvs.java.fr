package cn.staitech.fr.service.strategy.json.impl.dog.circulatory;

import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonFile;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 犬 血液循环系统 - 心脏指标计算
 * @author zhangy
 */
@Slf4j
@Component("Heart_3")
public class HeartParserStrategyImpl extends AbstractCustomParserStrategy implements OutlineCustom {

    /** 犬心脏 - 血管结构ID */
    private static final String STRUCTURE_VESSEL = "35E003";
    /** 犬心脏 - 组织轮廓结构ID */
    private static final String STRUCTURE_OUTLINE = "35E111";

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

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("Dog HeartParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("犬心脏指标计算开始 singleId={}", jsonTask.getSingleId());
        Map<String, IndicatorAddIn> indicatorResultsMap = buildHeartIndicators(jsonTask);
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
        log.info("犬心脏指标计算完成");
    }

    @Override
    public String getAlgorithmCode() {
        return "Heart_3";
    }

    @Override
    public void getCustomOutLine(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = buildHeartIndicators(jsonTask);
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    /**
     * A 血管面积 mm² - 结构35E003数据相加输出
     * B 组织轮廓 mm² - 结构35E111/单切片轮廓面积
     * 1 血管面积占比 % = A/B，保留三位小数
     * 2 心脏面积 mm² = B，保留三位小数
     */
    private Map<String, IndicatorAddIn> buildHeartIndicators(JsonTask jsonTask) {
        // A 血管面积 mm² - 结构35E003，数据相加输出
        Annotation vesselAnnotation = commonJsonParser.getOrganArea(jsonTask, STRUCTURE_VESSEL);
        BigDecimal vesselAreaA = ObjectUtil.isNotEmpty(vesselAnnotation) && vesselAnnotation.getStructureAreaNum() != null
                ? vesselAnnotation.getStructureAreaNum() : BigDecimal.ZERO;

        // B 组织轮廓 mm² - 单切片轮廓面积
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal tissueOutlineB = ObjectUtil.isNotEmpty(singleSlide) && StringUtils.isNotEmpty(singleSlide.getArea())
                ? new BigDecimal(singleSlide.getArea()) : BigDecimal.ZERO;

        Map<String, IndicatorAddIn> result = new HashMap<>();

        // 算法输出指标：血管面积 A
        result.put("血管面积", createIndicator(vesselAreaA.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, STRUCTURE_VESSEL));

        // 血管面积占比 % = A/B
        result.put("血管面积占比", createNameIndicator("Vessel area%", getProportion(vesselAreaA, tissueOutlineB).toString(),
                        PERCENTAGE, STRUCTURE_VESSEL + "," + STRUCTURE_OUTLINE));

        // 心脏面积 mm² = B
        result.put("心脏面积", createNameIndicator("Heart area", String.valueOf(tissueOutlineB.setScale(3, RoundingMode.HALF_UP)),
                        SQ_MM, STRUCTURE_OUTLINE));
        return result;
    }
}
