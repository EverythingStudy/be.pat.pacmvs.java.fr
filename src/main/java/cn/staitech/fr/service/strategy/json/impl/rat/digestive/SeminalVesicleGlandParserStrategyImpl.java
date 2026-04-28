package cn.staitech.fr.service.strategy.json.impl.rat.digestive;

import cn.hutool.core.collection.CollectionUtil;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
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
import cn.staitech.fr.utils.MathUtils;
import lombok.extern.slf4j.Slf4j;

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
 * @ClassName: SeminalVesicleGlandParserStrategyImpl
 * @Description-d:精囊腺
 * @date 2025年7月22日
 */
@Slf4j
@Component("Seminal_vesicles")
public class SeminalVesicleGlandParserStrategyImpl extends AbstractCustomParserStrategy {

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
        log.info("SeminalVesicleGlandParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("精囊腺结构指标计算开始");


        //B 腺上皮面积（全片）
        BigDecimal organArea = commonJsonParser.getOrganArea(jsonTask, "12D074").getStructureAreaNum();
        //D 腺腔面积（全片）
        BigDecimal organArea1 = commonJsonParser.getOrganArea(jsonTask, "12D0E9").getStructureAreaNum();

        //A 腺上皮面积（单个）
        Annotation annotation1 = new Annotation();
        annotation1.setAreaName("腺上皮面积（单个）");
        annotation1.setAreaUnit(SQ_MM);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "12D074", annotation1, 3);
        //C 腺腔面积（单个）
        Annotation annotationBy = new Annotation();
        annotationBy.setAreaName("腺腔面积（单个）");
        annotationBy.setAreaUnit(SQ_MM);
        commonJsonParser.putAnnotationDynamicData(jsonTask, "12D074", "12D0E9", annotationBy);
        //E 腺上皮细胞核数量（单个）
        Annotation annotationBy2 = new Annotation();
        annotationBy2.setCountName("腺上皮细胞核数量（单个）");
        annotationBy2.setCountUnit(PIECE);
        commonJsonParser.putAnnotationDynamicData(jsonTask, "12D074", "12D0ED", annotationBy2);
        // F 组织轮廓面积
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal organAreaF = new BigDecimal(singleSlide.getArea());

        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        //D 腺腔面积（全片）mm2
        indicatorResultsMap.put("腺腔面积（全片）", new IndicatorAddIn("腺腔面积（全片）", organArea1.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1, areaUtils.getStructureIds("12D074", "12D0E9")));
        //1 精囊腺面积 mm2 1=F
        indicatorResultsMap.put("精囊腺面积", new IndicatorAddIn("Seminal vesicle area", new BigDecimal(singleSlide.getArea()).setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_0, "12D111"));
        //2 腺上皮面积（全片） mm2 2=B
        indicatorResultsMap.put("腺上皮面积（全片）", new IndicatorAddIn("Acinar epithelial area (all)", organArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "12D074"));

        //3 腺上皮面积占比（单个） % 3=A/(A+C)
        List<Annotation> structureContourList = commonJsonParser.getStructureContourList(jsonTask, "12D074");
        List<BigDecimal> lists = new ArrayList<>();
        List<BigDecimal> listNum = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(structureContourList)) {
            for (Annotation annotation : structureContourList) {
                //A 腺上皮面积（单个）mm2
                BigDecimal structureAreaNum = annotation.getStructureAreaNum();
                // C 腺腔面积（单个）mm2
                Annotation contourInsideOrOutside = commonJsonParser.getContourInsideOrOutside(jsonTask, annotation.getContour(), "12D0E9", true);
                BigDecimal structureAreaNum1 = contourInsideOrOutside.getStructureAreaNum();
                // E 腺上皮细胞核数量（单个）个
                Annotation contourInsideOrOutside2 = commonJsonParser.getContourInsideOrOutside(jsonTask, annotation.getContour(), "12D0ED", true);
                Integer count = contourInsideOrOutside2.getCount();
                // A/(A+C)
                BigDecimal add = structureAreaNum.add(structureAreaNum1);
                if (add.signum() != 0) {
                    lists.add(getProportion(structureAreaNum, add));
                }
                // E/A
                if (structureAreaNum.signum() != 0) {
                    BigDecimal divide = bigDecimalDivideCheck(new BigDecimal(count), structureAreaNum);
                    listNum.add(divide);
                }
            }
        }
        indicatorResultsMap.put("腺上皮面积占比（单个）", new IndicatorAddIn("Acinar epithelial area% (per)", MathUtils.getConfidenceInterval(lists), PERCENTAGE, areaUtils.getStructureIds("12D074", "12D074", "12D0E9")));
        //4 腺泡上皮细胞核密度（单个） 个/mm2  4=E/A
        indicatorResultsMap.put("腺泡上皮细胞核密度（单个）", new IndicatorAddIn("Nucleus density of acinar epithelium (per)", MathUtils.getConfidenceInterval(listNum), SQ_MM_PIECE, areaUtils.getStructureIds("12D074", "12D0ED", "12D074")));
        //5 间质和肌层面积占比 % 5=(F-B-D)/F
        BigDecimal divide = getProportion(organAreaF.subtract(organArea).subtract(organArea1), organAreaF);
        indicatorResultsMap.put("间质和肌层面积占比", new IndicatorAddIn("Mesenchyme and muscular area%", divide.toString(), PERCENTAGE, areaUtils.getStructureIds("12D111", "12D074", "12D074", "12D0E9")));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Seminal_vesicles";
    }
}
