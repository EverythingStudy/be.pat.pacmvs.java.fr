package cn.staitech.fr.service.strategy.json.impl.mouse;

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
 * 小鼠  食管指标计算
 * @author jiazx
 */
@Slf4j
@Component("Esophagus_2")
public class Esophagus_2ParserStrategyImpl extends AbstractCustomParserStrategy {

    /** 食管腔 */
    private static final String STRUCTURE_LUMEN = "20F120";
    /** 角质层面积 */
    private static final String STRATUM_CORNEUM = "22F12E";
    /**颗粒层+棘层+基底细胞层面积 */
    private static final String GRANULAR_SPINOUS_BASAL = "20F12F";
    /** 黏膜固有层+黏膜肌层+黏膜下层 */
    private static final String STRUCTURE_LAMINA_MUSCULAR_SUBMUCOSA = "20F13B";
    /** 肌层 */
    private static final String STRUCTURE_MUSCULAR = "20F00C";
    /** 组织轮廓 */
    private static final String STRUCTURE_OUTLINE = "20F111";

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
        log.info("Mouse Esophagus_2ParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("小鼠食管指标计算开始 singleId={}", jsonTask.getSingleId());
        Map<String, IndicatorAddIn> indicatorResultsMap = buildEsophagusIndicators(jsonTask);
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
        log.info("小鼠管指标计算完成");
    }

    @Override
    public String getAlgorithmCode() {
        return "Esophagus_2";
    }


    private Map<String, IndicatorAddIn> buildEsophagusIndicators(JsonTask jsonTask) {
        // A 食管腔面积 mm² - 若C型则为空json，数值取0
        BigDecimal areaA = getAreaOrZero(commonJsonParser.getOrganArea(jsonTask, STRUCTURE_LUMEN));

        //B 角质层面积
        BigDecimal areaB = getAreaOrZero(commonJsonParser.getOrganArea(jsonTask, STRATUM_CORNEUM));

        // C 颗粒层+棘层+基底细胞层面积
        BigDecimal areaC = getAreaOrZero(commonJsonParser.getOrganArea(jsonTask, GRANULAR_SPINOUS_BASAL));

        // D 黏膜固有层+黏膜肌层+黏膜下层面积 mm² - 若空json取0
        BigDecimal areaD = getAreaOrZero(commonJsonParser.getOrganArea(jsonTask, STRUCTURE_LAMINA_MUSCULAR_SUBMUCOSA));

        // E 肌层面积 mm²
        BigDecimal areaE = getAreaOrZero(commonJsonParser.getOrganArea(jsonTask, STRUCTURE_MUSCULAR));

        // F 组织轮廓面积 mm²
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal areaF = ObjectUtil.isNotEmpty(singleSlide) && StringUtils.isNotEmpty(singleSlide.getArea()) ? new BigDecimal(singleSlide.getArea()) : BigDecimal.ZERO;

        // 食管壁面积 = F - A（占比计算公分母）
        BigDecimal wallArea = areaF.subtract(areaA).setScale(7, RoundingMode.HALF_UP);

        Map<String, IndicatorAddIn> result = new HashMap<>();

        // 算法输出指标 B、C、D E
        result.put("角质层面积", createIndicator(areaB.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, STRATUM_CORNEUM));
        result.put("颗粒层+棘层+基底细胞层面积", createIndicator(areaC.setScale(3, RoundingMode.HALF_UP).toString(), MULTIPLIED_SQ_UM_THOUSAND, GRANULAR_SPINOUS_BASAL));
        result.put("黏膜固有层+黏膜肌层+黏膜下层面积", createIndicator(areaD.setScale(3, RoundingMode.HALF_UP).toString(), MULTIPLIED_SQ_UM_THOUSAND, STRUCTURE_LAMINA_MUSCULAR_SUBMUCOSA));
        result.put("肌层面积", createIndicator(areaE.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, STRUCTURE_MUSCULAR));

        // 指标：1、2、4 占比，5 食管面积
        if (wallArea.compareTo(BigDecimal.ZERO) > 0) {
            result.put("角质层面积占比", createNameIndicator("Stratum corneum area%",getProportion(areaB.divide(new BigDecimal(1000)).setScale(3, RoundingMode.HALF_UP), wallArea).toString(), PERCENTAGE,STRATUM_CORNEUM + "," + STRUCTURE_OUTLINE + "," + STRUCTURE_LUMEN));
            result.put("颗粒层+棘层+基底细胞层面积占比", createNameIndicator("Nucleated cell layer area% ",getProportion(areaC.divide(new BigDecimal(1000)).setScale(3, RoundingMode.HALF_UP), wallArea).toString(), PERCENTAGE,GRANULAR_SPINOUS_BASAL + "," + STRUCTURE_OUTLINE + "," + STRUCTURE_LUMEN));
            result.put("黏膜固有层+黏膜肌层+黏膜下层面积占比", createNameIndicator("Subepithelium area %",getProportion(areaD.divide(new BigDecimal(1000)), wallArea).toString(), PERCENTAGE,STRUCTURE_LAMINA_MUSCULAR_SUBMUCOSA + "," + STRUCTURE_OUTLINE + "," + STRUCTURE_LUMEN));
            result.put("肌层面积占比", createNameIndicator("Muscularis area%",getProportion(areaE.divide(new BigDecimal(1000)), wallArea).toString(), PERCENTAGE,STRUCTURE_MUSCULAR + "," + STRUCTURE_OUTLINE + "," + STRUCTURE_LUMEN));
        } else {
            result.put("角质层面积占比", createNameIndicator("Stratum corneum area%","0.00", PERCENTAGE,STRATUM_CORNEUM + "," + STRUCTURE_OUTLINE + "," + STRUCTURE_LUMEN));
            result.put("颗粒层+棘层+基底细胞层面积占比", createNameIndicator("Nucleated cell layer area% ","0.00", PERCENTAGE,GRANULAR_SPINOUS_BASAL + "," + STRUCTURE_OUTLINE + "," + STRUCTURE_LUMEN));
            result.put("黏膜固有层+黏膜肌层+黏膜下层面积占比", createNameIndicator("Subepithelium area %","0.00", PERCENTAGE,STRUCTURE_LAMINA_MUSCULAR_SUBMUCOSA + "," + STRUCTURE_OUTLINE + "," + STRUCTURE_LUMEN));
            result.put("肌层面积占比", createNameIndicator("Muscularis area%","0.00", PERCENTAGE,STRUCTURE_MUSCULAR + "," + STRUCTURE_OUTLINE + "," + STRUCTURE_LUMEN));
        }

        // 5 食管面积 mm² = F - A
        result.put("食管面积", createNameIndicator("Esophagus area", wallArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, STRUCTURE_OUTLINE + "," + STRUCTURE_LUMEN));

        return result;
    }

    private BigDecimal getAreaOrZero(Annotation annotation) {
        return ObjectUtil.isNotEmpty(annotation) && annotation.getStructureAreaNum() != null ? annotation.getStructureAreaNum() : BigDecimal.ZERO;
    }
}
