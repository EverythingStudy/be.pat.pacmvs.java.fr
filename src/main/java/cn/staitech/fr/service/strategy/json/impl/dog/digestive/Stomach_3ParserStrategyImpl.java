package cn.staitech.fr.service.strategy.json.impl.dog.digestive;

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
 * 犬 消化系统-消化道 - 胃指标计算
 * @author zhangy
 */
@Slf4j
@Component("Stomach_3")
public class Stomach_3ParserStrategyImpl extends AbstractCustomParserStrategy {

    /** 黏膜上皮+固有层 */
    private static final String STRUCTURE_EPITHELIUM_LAMINA = "31001E";
    /** 黏膜肌层+黏膜下层 */
    private static final String STRUCTURE_MUSCULAR_SUBMUCOSA = "31001F";
    /** 肌层 */
    private static final String STRUCTURE_MUSCULAR = "31000C";
    /** 组织轮廓 */
    private static final String STRUCTURE_OUTLINE = "310111";

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
        log.info("Dog StomachParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("犬胃指标计算开始 singleId={}", jsonTask.getSingleId());
        Map<String, IndicatorAddIn> indicatorResultsMap = buildStomachIndicators(jsonTask);
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
        log.info("犬胃指标计算完成");
    }

    @Override
    public String getAlgorithmCode() {
        return "Stomach_3";
    }

    /**
     * A 黏膜上皮+固有层面积 mm² - 31001E
     * B 黏膜肌层+黏膜下层面积 mm² - 31001F
     * C 肌层面积 mm² - 31000C
     * D 组织轮廓面积 mm² - 310111（辅助指标4，不单独展示）
     * 1 黏膜上皮+固有层面积占比 % = A/D
     * 2 黏膜肌层+黏膜下层面积占比 % = B/D
     * 3 肌层面积占比 % = C/D
     * 4 胃面积 mm² = D
     */
    private Map<String, IndicatorAddIn> buildStomachIndicators(JsonTask jsonTask) {
        BigDecimal areaA = getAreaOrZero(commonJsonParser.getOrganArea(jsonTask, STRUCTURE_EPITHELIUM_LAMINA));
        BigDecimal areaB = getAreaOrZero(commonJsonParser.getOrganArea(jsonTask, STRUCTURE_MUSCULAR_SUBMUCOSA));
        BigDecimal areaC = getAreaOrZero(commonJsonParser.getOrganArea(jsonTask, STRUCTURE_MUSCULAR));

        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal areaD = ObjectUtil.isNotEmpty(singleSlide) && StringUtils.isNotEmpty(singleSlide.getArea())
                ? new BigDecimal(singleSlide.getArea()) : BigDecimal.ZERO;

        Map<String, IndicatorAddIn> result = new HashMap<>();

        // 算法输出指标 A、B、C
        result.put("黏膜上皮+固有层面积", createIndicator(areaA.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, STRUCTURE_EPITHELIUM_LAMINA));
        result.put("黏膜肌层+黏膜下层面积", createIndicator(areaB.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, STRUCTURE_MUSCULAR_SUBMUCOSA));
        result.put("肌层面积", createIndicator(areaC.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, STRUCTURE_MUSCULAR));

        // 产品呈现指标：1~3 占比，4 胃面积
        if (areaD.compareTo(BigDecimal.ZERO) > 0) {
            result.put("黏膜上皮+固有层面积占比", createNameIndicator("Mucosa epithelium+lamina propria mucosa area%",
                    getProportion(areaA, areaD).toString(), PERCENTAGE, STRUCTURE_EPITHELIUM_LAMINA + "," + STRUCTURE_OUTLINE));
            result.put("黏膜肌层+黏膜下层面积占比", createNameIndicator("Mucosal muscular layer+submucosal area ratio%",
                    getProportion(areaB, areaD).toString(), PERCENTAGE, STRUCTURE_MUSCULAR_SUBMUCOSA + "," + STRUCTURE_OUTLINE));
            result.put("肌层面积占比", createNameIndicator("Muscular area%",
                    getProportion(areaC, areaD).toString(), PERCENTAGE, STRUCTURE_MUSCULAR + "," + STRUCTURE_OUTLINE));
        } else {
            result.put("黏膜上皮+固有层面积占比", createNameIndicator("Mucosa epithelium+lamina propria mucosa area%", "0.000", PERCENTAGE, STRUCTURE_EPITHELIUM_LAMINA + "," + STRUCTURE_OUTLINE));
            result.put("黏膜肌层+黏膜下层面积占比", createNameIndicator("Mucosal muscular layer+submucosal area ratio%", "0.000", PERCENTAGE, STRUCTURE_MUSCULAR_SUBMUCOSA + "," + STRUCTURE_OUTLINE));
            result.put("肌层面积占比", createNameIndicator("Muscular area%", "0.000", PERCENTAGE, STRUCTURE_MUSCULAR + "," + STRUCTURE_OUTLINE));
        }

        // 4 胃面积 mm² = D（组织轮廓面积）
        result.put("胃面积", createNameIndicator("Stomach area", areaD.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, STRUCTURE_OUTLINE));

        return result;
    }

    private BigDecimal getAreaOrZero(Annotation annotation) {
        return ObjectUtil.isNotEmpty(annotation) && annotation.getStructureAreaNum() != null
                ? annotation.getStructureAreaNum() : BigDecimal.ZERO;
    }
}
