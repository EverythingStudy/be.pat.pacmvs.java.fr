package cn.staitech.fr.service.strategy.json.impl.rat.intestines;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import cn.staitech.fr.utils.DecimalUtils;
import cn.staitech.fr.utils.MathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wanglibei
 * @version V1.0
 * @ClassName: CoagulatingGlangParserStrategyImpl
 * @Description-d:凝固腺
 * @date 2025年7月22日
 */
@Slf4j
@Component("Coagulating_glang")
public class CoagulatingGlangParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private CommonJsonCheck commonJsonCheck;
    @Autowired
    private AreaUtils areaUtils;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.debug("AdrenalGlandParserStrategyImpl init");
    }

    @Override
    public void parseJson(JsonTask jsonTask, JsonFile jsonFileS) {
        commonJsonParser.parseJson(jsonTask, jsonFileS);
    }

    @Override
    public boolean checkJson(JsonTask jsonTask, List<JsonFile> jsonFileList) {
        return commonJsonCheck.checkJson(jsonTask, jsonFileList);
    }

    /**
     * 指标计算
     *
     * @param jsonTask
     */
    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("标计算开始-大鼠凝固腺");
        Map<String, IndicatorAddIn> map = new HashMap<>();

        // B 腺上皮面积（全片）
        BigDecimal colonArea = commonJsonParser.getOrganArea(jsonTask, "12B074").getStructureAreaNum();
        // D 腺腔面积（全片）
        BigDecimal areaNum2 = commonJsonParser.getOrganArea(jsonTask, "12B0E9").getStructureAreaNum();
        // F 组织轮廓
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal tissueArea = new BigDecimal(singleSlide.getArea());

        // A 腺上皮面积（单个） mm2
        Annotation annotationA = new Annotation();
        annotationA.setAreaName("腺上皮面积（单个）");
        annotationA.setAreaUnit(CommonConstant.SQUARE_MILLIMETRE);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "12B074", annotationA, 3);

        // C 腺腔面积（单个）mm2
        Annotation annotationByC = new Annotation();
        annotationByC.setAreaName("腺腔面积（单个）");
        annotationByC.setAreaUnit(CommonConstant.SQUARE_MILLIMETRE);
        commonJsonParser.putAnnotationDynamicData(jsonTask, "12B074", "12B0E9", annotationByC);

        //E 腺上皮细胞核数量（单个）个	单个腺上皮细胞核数量
        Annotation annotationByE = new Annotation();
        annotationByE.setCountName("腺上皮细胞核数量（单个）");
        annotationByE.setCountUnit("个");
        commonJsonParser.putAnnotationDynamicData(jsonTask, "12B074", "12B0ED", annotationByE);

        // 算法输出指标 -------------------------------------------------------------

        // D 腺腔面积（全片）D mm2
        map.put("腺腔面积（全片）", new IndicatorAddIn("腺腔面积（全片）", DecimalUtils.setScale3(areaNum2), CommonConstant.SQUARE_MILLIMETRE, CommonConstant.NUMBER_1, areaUtils.getStructureIds("12B074", "12B0E9")));

        // 产品呈现指标 -------------------------------------------------------------
        // 1 组织轮廓的面积 凝固腺面积 mm2	Coagulating gland area	1=F
        map.put("凝固腺面积", new IndicatorAddIn("Coagulating gland area", DecimalUtils.setScale3(tissueArea), CommonConstant.SQUARE_MILLIMETRE, CommonConstant.NUMBER_0, "12B111"));
        // 2 腺上皮面积（全片）	平方毫米	Acinar epithelial area (all)	2=B
        map.put("腺上皮面积（全片）", new IndicatorAddIn("Acinar epithelial area (all)", DecimalUtils.setScale3(colonArea), CommonConstant.SQUARE_MILLIMETRE, CommonConstant.NUMBER_0, "12B074"));

        // 3 腺上皮面积占比（单个）	%	Acinar epithelial area% (per)	3=A/(A+C) 以95%置信区间和均数±标准差呈现
        // 4 腺泡上皮细胞核密度（单个）	个/mm2	Nucleus density of acinar epithelium (per)	4=E/A	以95%置信区间和均数±标准差呈现
        List<Annotation> structureContourList = commonJsonParser.getStructureContourList(jsonTask, "12B074");
        List<BigDecimal> lists = new ArrayList<>();
        List<BigDecimal> listNum = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(structureContourList)) {
            for (Annotation annotation : structureContourList) {
                // A 腺上皮面积（单个）	A	平方毫米	单个腺上皮面积
                BigDecimal structureAreaNum = annotation.getStructureAreaNum();
                // C 腺腔面积（单个）	C	平方毫米	单个腺上皮内所有腺腔面积
                Annotation contourInsideOrOutside = commonJsonParser.getContourInsideOrOutside(jsonTask, annotation.getContour(), "12B0E9", true);
                Annotation contourInsideOrOutside2 = commonJsonParser.getContourInsideOrOutside(jsonTask, annotation.getContour(), "12B0ED", true);
                // 面积 C
                BigDecimal structureAreaNum1 = contourInsideOrOutside.getStructureAreaNum();
                // A+C
                BigDecimal addAC = structureAreaNum.add(structureAreaNum1);
                if (addAC.compareTo(BigDecimal.ZERO) != 0) {
                    // 3=A/(A+C)
                    lists.add(structureAreaNum.divide(addAC, 7, RoundingMode.HALF_UP).multiply(new BigDecimal(100)));
                }
                // E
                Integer count = contourInsideOrOutside2.getCount();
                if (addAC.compareTo(BigDecimal.ZERO) != 0) {
                    // 4=E/A
                    BigDecimal divide = new BigDecimal(count).divide(structureAreaNum, 7, RoundingMode.HALF_UP);
                    listNum.add(divide);
                }
            }
        }

        // 3 腺上皮面积占比（单个）	%	Acinar epithelial area% (per)	3=A/(A+C) 以95%置信区间和均数±标准差呈现
        map.put("腺上皮面积占比（单个）", new IndicatorAddIn("Acinar epithelial area% (per)", MathUtils.getConfidenceInterval(lists), CommonConstant.PERCENTAGE, areaUtils.getStructureIds("12B074", "12B0E9")));

        // 4 腺泡上皮细胞核密度（单个）	4	个/平方毫米	Nucleus density of acinar epithelium (per)	4=E/A 以95%置信区间和均数±标准差呈现
        map.put("腺泡上皮细胞核密度（单个）", new IndicatorAddIn("Nucleus density of acinar epithelium (per)", MathUtils.getConfidenceInterval(listNum), CommonConstant.SQ_MM_PIECE_EN, areaUtils.getStructureIds("12B074", "12B0ED")));
        // 5 间质和肌层面积占比		%	Mesenchyme and muscular area%	5=(F-B-D)/F
        map.put("间质和肌层面积占比", new IndicatorAddIn("Mesenchyme and muscular area%", getProportion(tissueArea.subtract(colonArea).subtract(areaNum2), tissueArea).toString(), CommonConstant.PERCENTAGE, areaUtils.getStructureIds("12B111", "12B074", "12B0E9")));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
        log.info("标计算结束-大鼠凝固腺");
    }

    @Override
    public String getAlgorithmCode() {
        return "Coagulating_glang";
    }
}
