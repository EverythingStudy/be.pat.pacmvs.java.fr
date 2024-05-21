package cn.staitech.fr.service.strategy.json.impl.intestines;

import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.service.strategy.json.ParserStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * @author:
 * @create: 2024-05-10 14:18:48
 * @Description: Coagulating_glang Json Parser 大鼠凝固腺
 */
@Slf4j
@Component("Coagulating_glang")
public class CoagulatingGlangParserStrategyImpl implements ParserStrategy {

    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;


    @Override
    public void parseJson(JsonTask jsonTask, JsonFile jsonFileS) {
        commonJsonParser.parseJson(jsonTask, jsonFileS);
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("大鼠凝固腺结构指标计算开始");
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        // 组织轮廓面积
        String area = ObjectUtil.isNotEmpty(singleSlide) ? singleSlide.getArea() : "0";
        area = ObjectUtil.isEmpty(area) ? "0" : area;
        Map<String, IndicatorAddIn> resultMap = new HashMap<>();
        // 腺上皮面积（全片）
        BigDecimal colonArea = commonJsonParser.getOrganArea(jsonTask, "12B074").getStructureAreaNum();
        // 腺腔面积（单个）
//        BigDecimal areaNum = commonJsonParser.getOrganArea(jsonTask, "12B0E9").getStructureAreaNum();
        // 腺上皮细胞核数量（单个）
//        Integer areaCount = commonJsonParser.getOrganAreaCount(jsonTask, "12B0ED");
        // 腺腔面积（全片）
        BigDecimal areaNum2 = commonJsonParser.getInsideOrOutside(jsonTask, "12B074", "12B0E9", true).getStructureAreaNum();
        // 组织轮廓
        BigDecimal areaNum4 = new BigDecimal(area);
        // 腺上皮细胞核数量（单个）
//        resultMap.put("腺上皮细胞核数量（单个）", new IndicatorAddIn("Acinar epithelial cell number (individual)", areaCount.toString(), "个", CommonConstant.NUMBER_1));
        // 腺腔面积（全片）
        resultMap.put("腺腔面积（全片）", new IndicatorAddIn("Gland cavity area (all)", areaNum2.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        // 腺腔面积（单个）
//        resultMap.put("腺腔面积（单个）", new IndicatorAddIn("Gland cavity area (individual)", areaNum.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        resultMap.put("腺腔面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1));
        // 腺上皮面积（全片）
        resultMap.put("腺上皮面积（全片）", new IndicatorAddIn("Acinar epithelial area (all)", colonArea.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_0));
        // 组织轮廓的面积
        resultMap.put("凝固腺面积", new IndicatorAddIn("Coagulating gland area", areaNum4.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_0));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), resultMap);
        log.info("大鼠凝固腺结构指标计算结束");
    }
}
