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
 * 犬 消化系统-消化道 - 食管指标计算
 * @author zhangy
 */
@Slf4j
@Component("Esophagus_3")
public class Esophagus_3ParserStrategyImpl extends AbstractCustomParserStrategy implements OutlineCustom {

    /** 食管腔 */
    private static final String STRUCTURE_LUMEN = "30F120";
    /** 黏膜上皮 */
    private static final String STRUCTURE_EPITHELIUM = "30F035";
    /** 黏膜固有层+黏膜肌层+黏膜下层 */
    private static final String STRUCTURE_LAMINA_MUSCULAR_SUBMUCOSA = "30F13B";
    /** 肌层 */
    private static final String STRUCTURE_MUSCULAR = "30F00C";
    /** 组织轮廓 */
    private static final String STRUCTURE_OUTLINE = "30F111";

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
        log.info("Dog EsophagusParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("犬食管指标计算开始 singleId={}", jsonTask.getSingleId());
        Map<String, IndicatorAddIn> indicatorResultsMap = buildEsophagusIndicators(jsonTask);
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
        log.info("犬食管指标计算完成");
    }

    @Override
    public String getAlgorithmCode() {
        return "Esophagus_3";
    }

    @Override
    public void getCustomOutLine(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = buildEsophagusIndicators(jsonTask);
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    /**
     * A 食管腔面积 mm² - 30F120（若C型则为空，数值取0；辅助指标5，不显示）
     * B 黏膜上皮面积 mm² - 30F035
     * C 黏膜固有层+黏膜肌层+黏膜下层面积 mm² - 30F13B
     * D 肌层面积 mm² - 30F00C
     * E 组织轮廓面积 mm² - 30F111（辅助指标5，不显示）
     * 食管壁面积 = E - A
     * 1 黏膜上皮面积占比 % = B/(E-A)
     * 2 黏膜固有层+黏膜肌层+黏膜下层面积占比 % = C/(E-A)
     * 4 肌层面积占比 % = D/(E-A)
     * 5 食管面积 mm² = E-A
     */
    private Map<String, IndicatorAddIn> buildEsophagusIndicators(JsonTask jsonTask) {
        // A 食管腔面积 mm² - 若C型则为空json，数值取0
        BigDecimal areaA = getAreaOrZero(commonJsonParser.getOrganArea(jsonTask, STRUCTURE_LUMEN));

        // B 黏膜上皮面积 mm²
        BigDecimal areaB = getAreaOrZero(commonJsonParser.getOrganArea(jsonTask, STRUCTURE_EPITHELIUM));

        // C 黏膜固有层+黏膜肌层+黏膜下层面积 mm² - 若空json取0
        BigDecimal areaC = getAreaOrZero(commonJsonParser.getOrganArea(jsonTask, STRUCTURE_LAMINA_MUSCULAR_SUBMUCOSA));

        // D 肌层面积 mm²
        BigDecimal areaD = getAreaOrZero(commonJsonParser.getOrganArea(jsonTask, STRUCTURE_MUSCULAR));

        // E 组织轮廓面积 mm²
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal areaE = ObjectUtil.isNotEmpty(singleSlide) && StringUtils.isNotEmpty(singleSlide.getArea())
                ? new BigDecimal(singleSlide.getArea()) : BigDecimal.ZERO;

        // 食管壁面积 = E - A（占比计算公分母）
        BigDecimal wallArea = areaE.subtract(areaA).setScale(7, RoundingMode.HALF_UP);

        Map<String, IndicatorAddIn> result = new HashMap<>();

        // 算法输出指标 B、C、D
        result.put("黏膜上皮面积", createIndicator(areaB.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, STRUCTURE_EPITHELIUM));
        result.put("黏膜固有层+黏膜肌层+黏膜下层面积", createIndicator(areaC.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, STRUCTURE_LAMINA_MUSCULAR_SUBMUCOSA));
        result.put("肌层面积", createIndicator(areaD.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, STRUCTURE_MUSCULAR));

        // 指标：1、2、4 占比，5 食管面积
        if (wallArea.compareTo(BigDecimal.ZERO) > 0) {
            result.put("黏膜上皮面积占比", createNameIndicator("Stratum corneum area%",
                    getProportion(areaB, wallArea).toString(), PERCENTAGE, STRUCTURE_EPITHELIUM + "," + STRUCTURE_OUTLINE + "," + STRUCTURE_LUMEN));
            result.put("黏膜固有层+黏膜肌层+黏膜下层面积占比", createNameIndicator("Subepithelium area%",
                    getProportion(areaC, wallArea).toString(), PERCENTAGE, STRUCTURE_LAMINA_MUSCULAR_SUBMUCOSA + "," + STRUCTURE_OUTLINE + "," + STRUCTURE_LUMEN));
            result.put("肌层面积占比", createNameIndicator("Muscularis area%",
                    getProportion(areaD, wallArea).toString(), PERCENTAGE, STRUCTURE_MUSCULAR + "," + STRUCTURE_OUTLINE + "," + STRUCTURE_LUMEN));
        } else {
            result.put("黏膜上皮面积占比", createNameIndicator("Stratum corneum area%", "0.000", PERCENTAGE, STRUCTURE_EPITHELIUM + "," + STRUCTURE_OUTLINE + "," + STRUCTURE_LUMEN));
            result.put("黏膜固有层+黏膜肌层+黏膜下层面积占比", createNameIndicator("Subepithelium area%", "0.000", PERCENTAGE, STRUCTURE_LAMINA_MUSCULAR_SUBMUCOSA + "," + STRUCTURE_OUTLINE + "," + STRUCTURE_LUMEN));
            result.put("肌层面积占比", createNameIndicator("Muscularis area%", "0.000", PERCENTAGE, STRUCTURE_MUSCULAR + "," + STRUCTURE_OUTLINE + "," + STRUCTURE_LUMEN));
        }

        // 5 食管面积 mm² = E - A
        result.put("食管面积", createNameIndicator("Esophagus area", wallArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, STRUCTURE_OUTLINE + "," + STRUCTURE_LUMEN));

        return result;
    }

    private BigDecimal getAreaOrZero(Annotation annotation) {
        return ObjectUtil.isNotEmpty(annotation) && annotation.getStructureAreaNum() != null ? annotation.getStructureAreaNum() : BigDecimal.ZERO;
    }
}
