package cn.staitech.fr.service.strategy.json.impl.rat;

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
import cn.staitech.fr.service.strategy.json.OutlineCustom;
import cn.staitech.fr.utils.AreaUtils;
import cn.staitech.fr.utils.DecimalUtils;
import cn.staitech.fr.utils.MathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
 * @ClassName: HarderianGlandParserStrategyImpl
 * @Description-d:哈德氏腺
 * @date 2025年7月21日
 */
@Slf4j
@Component("Harderian_gland")
public class HarderianGlandParserStrategyImpl extends AbstractCustomParserStrategy implements OutlineCustom {

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
    @Resource
    private AreaUtils areaUtils;
    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.debug("D65EyeAndOpticNerveParserStrategyImpl init");
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
        log.info("指标计算开始-哈氏腺");
        Map<String, IndicatorAddIn> map = new HashMap<>();
        // E 腺泡面积（全片）mm2
        BigDecimal acinusArea = commonJsonParser.getOrganArea(jsonTask, "10206D").getStructureAreaNum();
        // F 腺泡细胞核数量（全片）个
        Integer nucleusCount = commonJsonParser.getOrganAreaCount(jsonTask, "10206E");

        String accurateArea = singleSlideMapper.selectById(jsonTask.getSingleId()).getArea();
        BigDecimal accurateAreaBigDecimal = new BigDecimal(accurateArea);

        // 腺泡列表
        List<Annotation> structureContourList = commonJsonParser.getStructureContourList(jsonTask, "10206D");
        List<BigDecimal> listNum = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(structureContourList)) {
            for (Annotation annotation : structureContourList) {
                // A 腺泡面积（单个）	A	103平方微米
                BigDecimal structureAreaNum = annotation.getStructureAreaNum().multiply(new BigDecimal(1000));

                // B 腺泡细胞核数量（单个）	B	个	单个腺泡内数据相加输出
                Annotation contourInsideOrOutside2 = commonJsonParser.getContourInsideOrOutside(jsonTask, annotation.getContour(), "10206E", true);
                Integer count = contourInsideOrOutside2.getCount();

                // 2=B/A
                if (structureAreaNum.compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal divide = new BigDecimal(count).divide(structureAreaNum, 7, RoundingMode.HALF_UP);
                    listNum.add(divide);
                }
            }
        }
        String confidenceInterval = MathUtils.getConfidenceInterval(listNum);
        // A 腺泡面积（单个） 103 μm2
        Annotation annotationC = new Annotation();
        annotationC.setAreaName("腺泡面积（单个）");
        annotationC.setAreaUnit(CommonConstant.SQUARE_MICROMETER);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "10206D", annotationC, 1);
        // B 腺泡细胞核数量（单个）个
        Annotation annotationB = new Annotation();
        annotationB.setCountName("腺泡细胞核数量（单个）");
        annotationB.setCountUnit("个");
        commonJsonParser.putAnnotationDynamicData(jsonTask, "10206D", "10206E", annotationB);


        // 算法输出指标 -------------------------------------------------------------
        // E 腺泡面积（全片）mm2
        map.put("腺泡面积（全片）", new IndicatorAddIn("腺泡面积（全片）", DecimalUtils.setScale3(acinusArea), CommonConstant.SQUARE_MILLIMETRE, CommonConstant.NUMBER_1, "10206D"));
        // F 腺泡细胞核数量（全片）个
        map.put("腺泡细胞核数量（全片）", new IndicatorAddIn("腺泡细胞核数量（全片）", nucleusCount.toString(), CommonConstant.PIECE, CommonConstant.NUMBER_1, "10206E"));

        // 产品呈现指标 -------------------------------------------------------------
        if (accurateAreaBigDecimal.compareTo(BigDecimal.ZERO) != 0) {
            // 1 腺泡面积占比（全片）%	Acinus area%（all）	1=E/D
            map.put("腺泡面积占比（全片）", new IndicatorAddIn("Acinus area %（all）", getProportion(acinusArea, accurateAreaBigDecimal).toString(), CommonConstant.PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("10206D", "102111")));

            // 色素面积占比 3 % Pigment area % 3 = C / D
            // BigDecimal pigmentDivideArea = pigmentArea.divide(accurateAreaBigDecimal, 7, BigDecimal.ROUND_HALF_UP);
            // map.put("色素面积占比", new IndicatorAddIn("Pigment area %", DecimalUtils.percentScale3(pigmentDivideArea), "%"));
        } else {
            //map.put("腺泡面积占比（全片）", new IndicatorAddIn("Acinus area %（all）", "0.000", "%",areaUtils.getStructureIds("10206D","102111")));
            // map.put("色素面积占比", new IndicatorAddIn("Pigment area %", "0.000", "%"));
        }
        // 2 腺泡细胞核密度(单个)  个/103 μm2  Nucleus density of acinus(per) 2 = B / A 95 % 置信区间和均数±标准差
        map.put("腺泡细胞核密度(单个)", new IndicatorAddIn("Nucleus density of acinus(per)", confidenceInterval, CommonConstant.SQ_SQUARE_MICROMETER_PIECE_EN, CommonConstant.NUMBER_0, areaUtils.getStructureIds("10206D", "10206E", "10206D")));

        // 4 腺泡细胞核密度（全片）个/mm2 Nucleus density of acinus (all) 4 = F / E
        map.put("腺泡细胞核密度（全片）", new IndicatorAddIn("Nucleus density of acinus (all)", bigDecimalDivideCheck(new BigDecimal(nucleusCount), acinusArea).toString(), CommonConstant.SQ_MM_PIECE_EN, CommonConstant.NUMBER_0, areaUtils.getStructureIds("10206E", "10206D")));

        // 5 哈氏腺面积 mm2
        map.put("哈德氏腺面积", new IndicatorAddIn("Acinus area", DecimalUtils.setScale3(accurateAreaBigDecimal), CommonConstant.SQUARE_MILLIMETRE, CommonConstant.NUMBER_0, "102111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
        log.info("指标计算结束-哈氏腺");
    }

    @Override
    public void getCustomOutLine(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal pituitaryH = new BigDecimal(singleSlide.getArea());
        indicatorResultsMap.put("哈氏腺面积", createNameIndicator("Acinus area", String.valueOf(pituitaryH.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "102111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Harderian_gland";
    }
}
