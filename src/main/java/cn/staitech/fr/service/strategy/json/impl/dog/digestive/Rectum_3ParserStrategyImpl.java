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
 * 犬 消化系统-消化道 - 直肠指标计算
 * @author zhangy
 */
@Slf4j
@Component("Rectum_3")
public class Rectum_3ParserStrategyImpl extends AbstractCustomParserStrategy {

    /** 黏膜上皮+固有层 */
    private static final String STRUCTURE_EPITHELIUM_LAMINA = "31601E";
    /** 黏膜上皮+黏膜下层 */
    private static final String STRUCTURE_EPITHELIUM_SUBMUCOSA = "31601F";
    /** 肌层 */
    private static final String STRUCTURE_MUSCULAR = "31600C";
    /** 淋巴组织 */
    private static final String STRUCTURE_LYMPHATIC = "316049";
    /** 组织轮廓 */
    private static final String STRUCTURE_OUTLINE = "316111";

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
        log.info("Dog Rectum_3ParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("犬直肠指标计算开始 singleId={}", jsonTask.getSingleId());
        Map<String, IndicatorAddIn> indicatorResultsMap = buildRectumIndicators(jsonTask);
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
        log.info("犬直肠指标计算完成");
    }

    @Override
    public String getAlgorithmCode() {
        return "Rectum_3";
    }

    /**
     * A 黏膜上皮+固有层面积 mm² - 31601E
     * B 黏膜上皮+黏膜下层面积 mm² - 31601F
     * C 肌层面积 mm² - 31600C
     * D 淋巴组织面积 mm² - 316049（若多个数据则相加输出）
     * E 组织轮廓面积 mm² - 316111（辅助指标5，不单独展示）
     * 1 黏膜上皮+固有层面积占比 % = A/E
     * 2 黏膜上皮+黏膜下层面积占比 % = B/E
     * 3 肌层面积占比 % = C/E
     * 4 淋巴组织面积占比 % = D/E
     * 5 直肠面积 mm² = E
     */
    private Map<String, IndicatorAddIn> buildRectumIndicators(JsonTask jsonTask) {
        BigDecimal areaA = getAreaOrZero(commonJsonParser.getOrganArea(jsonTask, STRUCTURE_EPITHELIUM_LAMINA));
        BigDecimal areaB = getAreaOrZero(commonJsonParser.getOrganArea(jsonTask, STRUCTURE_EPITHELIUM_SUBMUCOSA));
        BigDecimal areaC = getAreaOrZero(commonJsonParser.getOrganArea(jsonTask, STRUCTURE_MUSCULAR));
        BigDecimal areaD = getAreaOrZero(commonJsonParser.getOrganArea(jsonTask, STRUCTURE_LYMPHATIC));

        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal areaE = ObjectUtil.isNotEmpty(singleSlide) && StringUtils.isNotEmpty(singleSlide.getArea())
                ? new BigDecimal(singleSlide.getArea()) : BigDecimal.ZERO;

        Map<String, IndicatorAddIn> result = new HashMap<>();

        // 算法输出指标 A、B、C、D
        result.put("黏膜上皮+固有层面积", createIndicator(areaA.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, STRUCTURE_EPITHELIUM_LAMINA));
        result.put("黏膜上皮+黏膜下层面积", createIndicator(areaB.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, STRUCTURE_EPITHELIUM_SUBMUCOSA));
        result.put("肌层面积", createIndicator(areaC.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, STRUCTURE_MUSCULAR));
        result.put("淋巴组织面积", createIndicator(areaD.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, STRUCTURE_LYMPHATIC));

        // 产品呈现指标：1~4 占比，5 直肠面积
        if (areaE.compareTo(BigDecimal.ZERO) > 0) {
            result.put("黏膜上皮+固有层面积占比", createNameIndicator("Mucosa epithelium+lamina propria mucosa area%",
                    getProportion(areaA, areaE).toString(), PERCENTAGE, STRUCTURE_EPITHELIUM_LAMINA + "," + STRUCTURE_OUTLINE));
            result.put("黏膜上皮+黏膜下层面积占比", createNameIndicator("Mucosal muscular layer + submucosal area ratio%",
                    getProportion(areaB, areaE).toString(), PERCENTAGE, STRUCTURE_EPITHELIUM_SUBMUCOSA + "," + STRUCTURE_OUTLINE));
            result.put("肌层面积占比", createNameIndicator("Muscular area%",
                    getProportion(areaC, areaE).toString(), PERCENTAGE, STRUCTURE_MUSCULAR + "," + STRUCTURE_OUTLINE));
            result.put("淋巴组织面积占比", createNameIndicator("Lymphatic nodule area%",
                    getProportion(areaD, areaE).toString(), PERCENTAGE, STRUCTURE_LYMPHATIC + "," + STRUCTURE_OUTLINE));
        } else {
            result.put("黏膜上皮+固有层面积占比", createNameIndicator("Mucosa epithelium+lamina propria mucosa area%", "0.000", PERCENTAGE, STRUCTURE_EPITHELIUM_LAMINA + "," + STRUCTURE_OUTLINE));
            result.put("黏膜上皮+黏膜下层面积占比", createNameIndicator("Mucosal muscular layer + submucosal area ratio%", "0.000", PERCENTAGE, STRUCTURE_EPITHELIUM_SUBMUCOSA + "," + STRUCTURE_OUTLINE));
            result.put("肌层面积占比", createNameIndicator("Muscular area%", "0.000", PERCENTAGE, STRUCTURE_MUSCULAR + "," + STRUCTURE_OUTLINE));
            result.put("淋巴组织面积占比", createNameIndicator("Lymphatic nodule area%", "0.000", PERCENTAGE, STRUCTURE_LYMPHATIC + "," + STRUCTURE_OUTLINE));
        }

        // 5 直肠面积 mm² = E
        result.put("直肠面积", createNameIndicator("Rectum area", areaE.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, STRUCTURE_OUTLINE));
        return result;
    }

    private BigDecimal getAreaOrZero(Annotation annotation) {
        return ObjectUtil.isNotEmpty(annotation) && annotation.getStructureAreaNum() != null ? annotation.getStructureAreaNum() : BigDecimal.ZERO;
    }
}

