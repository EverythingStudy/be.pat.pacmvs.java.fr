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
 * 犬 消化系统-消化道 - 小肠指标计算
 * @author zhangy
 */
@Slf4j
@Component("Small_intestine_3")
public class SmallIntestine_3ParserStrategyImpl extends AbstractCustomParserStrategy {

    /** 黏膜上皮+固有层 */
    private static final String STRUCTURE_EPITHELIUM_LAMINA = "38201E";
    /** 黏膜肌层+黏膜下层 */
    private static final String STRUCTURE_MUSCULAR_SUBMUCOSA = "38201F";
    /** 肌层 */
    private static final String STRUCTURE_MUSCULAR = "38200C";
    /** 淋巴小结 */
    private static final String STRUCTURE_LYMPH_NODULE = "382064";
    /** 淋巴组织 */
    private static final String STRUCTURE_LYMPHATIC = "382049";
    /** 组织轮廓 */
    private static final String STRUCTURE_OUTLINE = "382111";

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
        log.info("Dog SmallIntestineParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("犬小肠指标计算开始 singleId={}", jsonTask.getSingleId());
        Map<String, IndicatorAddIn> indicatorResultsMap = buildSmallIntestineIndicators(jsonTask);
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
        log.info("犬小肠指标计算完成");
    }

    @Override
    public String getAlgorithmCode() {
        return "Small_intestine_3";
    }

    /**
     * B 黏膜上皮+固有层面积 mm² - 38201E
     * C 黏膜肌层+黏膜下层面积 mm² - 38201F
     * D 肌层面积 mm² - 38200C
     * E 淋巴组织面积 mm² - 382049（若多个数据则相加输出）
     * F 组织轮廓面积 mm² - 382111（辅助指标5，不单独展示）
     * G 淋巴小结数量 个 - 382064
     * 1 黏膜上皮+固有层面积占比 % = B/F
     * 2 黏膜肌层+黏膜下层面积占比 % = C/F
     * 3 肌层面积占比 % = D/F
     * 4 淋巴组织面积占比 % = E/F
     * 5 组织面积 mm² = F
     */
    private Map<String, IndicatorAddIn> buildSmallIntestineIndicators(JsonTask jsonTask) {
        BigDecimal areaB = getAreaOrZero(commonJsonParser.getOrganArea(jsonTask, STRUCTURE_EPITHELIUM_LAMINA));
        BigDecimal areaC = getAreaOrZero(commonJsonParser.getOrganArea(jsonTask, STRUCTURE_MUSCULAR_SUBMUCOSA));
        BigDecimal areaD = getAreaOrZero(commonJsonParser.getOrganArea(jsonTask, STRUCTURE_MUSCULAR));
        BigDecimal areaE = getAreaOrZero(commonJsonParser.getOrganArea(jsonTask, STRUCTURE_LYMPHATIC));

        Integer countG = commonJsonParser.getOrganAreaCount(jsonTask, STRUCTURE_LYMPH_NODULE);

        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal areaF = ObjectUtil.isNotEmpty(singleSlide) && StringUtils.isNotEmpty(singleSlide.getArea())
                ? new BigDecimal(singleSlide.getArea()) : BigDecimal.ZERO;

        Map<String, IndicatorAddIn> result = new HashMap<>();

        // 算法输出指标 B、C、D、E、G
        result.put("黏膜上皮+固有层面积", createIndicator(areaB.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, STRUCTURE_EPITHELIUM_LAMINA));
        result.put("黏膜肌层+黏膜下层面积", createIndicator(areaC.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, STRUCTURE_MUSCULAR_SUBMUCOSA));
        result.put("肌层面积", createIndicator(areaD.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, STRUCTURE_MUSCULAR));
        result.put("淋巴组织面积", createIndicator(areaE.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, STRUCTURE_LYMPHATIC));
        result.put("淋巴小结数量", createIndicator(String.valueOf(countG != null ? countG : 0), PIECE, STRUCTURE_LYMPH_NODULE));

        // 产品呈现指标：1~4 占比，5 组织面积
        if (areaF.compareTo(BigDecimal.ZERO) > 0) {
            result.put("黏膜上皮+固有层面积占比", createNameIndicator("Mucosa epithelium+lamina propria mucosa area%",
                    getProportion(areaB, areaF).toString(), PERCENTAGE, STRUCTURE_EPITHELIUM_LAMINA + "," + STRUCTURE_OUTLINE));
            result.put("黏膜肌层+黏膜下层面积占比", createNameIndicator("Mucosal muscular layer+submucosal area ratio%",
                    getProportion(areaC, areaF).toString(), PERCENTAGE, STRUCTURE_MUSCULAR_SUBMUCOSA + "," + STRUCTURE_OUTLINE));
            result.put("肌层面积占比", createNameIndicator("Muscular area%",
                    getProportion(areaD, areaF).toString(), PERCENTAGE, STRUCTURE_MUSCULAR + "," + STRUCTURE_OUTLINE));
            result.put("淋巴组织面积占比", createNameIndicator("Lymphatic nodule area%",
                    getProportion(areaE, areaF).toString(), PERCENTAGE, STRUCTURE_LYMPHATIC + "," + STRUCTURE_OUTLINE));
        } else {
            result.put("黏膜上皮+固有层面积占比", createNameIndicator("Mucosa epithelium+lamina propria mucosa area%", "0.000", PERCENTAGE, STRUCTURE_EPITHELIUM_LAMINA + "," + STRUCTURE_OUTLINE));
            result.put("黏膜肌层+黏膜下层面积占比", createNameIndicator("Mucosal muscular layer+submucosal area ratio%", "0.000", PERCENTAGE, STRUCTURE_MUSCULAR_SUBMUCOSA + "," + STRUCTURE_OUTLINE));
            result.put("肌层面积占比", createNameIndicator("Muscular area%", "0.000", PERCENTAGE, STRUCTURE_MUSCULAR + "," + STRUCTURE_OUTLINE));
            result.put("淋巴组织面积占比", createNameIndicator("Lymphatic nodule area%", "0.000", PERCENTAGE, STRUCTURE_LYMPHATIC + "," + STRUCTURE_OUTLINE));
        }

        // 5 组织面积 mm² = F（文档英文名 Ileum area）
        result.put("组织面积", createNameIndicator("Ileum area", areaF.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, STRUCTURE_OUTLINE));

        return result;
    }

    private BigDecimal getAreaOrZero(Annotation annotation) {
        return ObjectUtil.isNotEmpty(annotation) && annotation.getStructureAreaNum() != null
                ? annotation.getStructureAreaNum() : BigDecimal.ZERO;
    }
}
