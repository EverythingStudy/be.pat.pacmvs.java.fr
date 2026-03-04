package cn.staitech.fr.service.strategy.json.impl.dog.musbonesjoint;

import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import cn.staitech.fr.utils.MathUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 狗-骨骼肌
 */
@Slf4j
@Service("Muscle_3")
public class Muscle_3ParserStrategyImpl extends AbstractCustomParserStrategy {
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private AreaUtils areaUtils;
    @Resource
    private CommonJsonCheck commonJsonCheck;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("Muscle_3ParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("指标计算开始：骨骼肌");

        //A 肌纤维面积（单个）
        List<Annotation> annotationList = commonJsonParser.getStructureContourList(jsonTask, "35C02A");
        // B肌纤维面积（全片）
        BigDecimal organAreaB = commonJsonParser.getOrganArea(jsonTask, "35C02A").getStructureAreaNum();
        // C间质面积
        BigDecimal organAreaC = commonJsonParser.getOrganArea(jsonTask, "35C027").getStructureAreaNum();
        // D肌细胞核数量
        Integer organAreaD = commonJsonParser.getOrganAreaCount(jsonTask, "35C05A");
        // E精细轮廓总面积
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());
        // BigDecimal organF = commonJsonParser.getOrganArea(jsonTask, "15C111").getStructureAreaNum();
        BigDecimal organE = BigDecimal.valueOf(Double.parseDouble(slideArea));
        //1 肌纤维面积（单个） mm2 1=A
        List<BigDecimal> annotationAreaList = annotationList.stream().map(anno -> new BigDecimal(anno.getArea()).setScale(3, RoundingMode.DOWN)).collect(Collectors.toList());
        String muscleFiberArea = MathUtils.getConfidenceInterval(annotationAreaList);

        //2 间质面积占比 % 2=C/E
        BigDecimal mesenchymeArea = commonJsonParser.getProportion(organAreaC, organE);
        //肌细胞核密度 个/mm2 3=D/B
        BigDecimal vesselArea = getDensityResult(organAreaD, organAreaB.toString());

        Map<String, IndicatorAddIn> resultsMap = new HashMap<>();
        Annotation annotation1 = new Annotation();
        annotation1.setAreaName("肌纤维面积（单个）");
        annotation1.setAreaUnit(SQ_UM);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "35C02A", annotation1, 2);

        resultsMap.put("肌纤维面积（全片）", createIndicator(organAreaB.toString(), SQ_MM, "35C02A"));
        resultsMap.put("间质面积", createIndicator(areaUtils.convertToSquareMicrometer(organAreaB.toString()), SQ_UM_THOUSAND, "35C027"));
        resultsMap.put("肌细胞核数量", createIndicator(organAreaD, PIECE, "35C05A"));


        // 产品呈现指标
        resultsMap.put("肌纤维面积(单个)", createNameIndicator("Muscle fiber area (per)", muscleFiberArea, SQ_UM, "35C02A"));
        resultsMap.put("间质面积占比", createNameIndicator("Mesenchyme area %", mesenchymeArea, PERCENTAGE, "35C027,35C111"));
        resultsMap.put("肌细胞核密度", createNameIndicator("Nuclear Density of myocyte", vesselArea.setScale(3, RoundingMode.UP), SQ_MM_PIECE, "35C05A,35C02A"));
        resultsMap.put("骨骼肌面积", createNameIndicator("Skeletal muscle area", organE, SQ_MM, "35C111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), resultsMap);

        log.info("指标计算结束：骨骼肌");
    }

    @Override
    public String getAlgorithmCode() {
        return "Muscle_3";
    }


    /**
     * 计算指标
     *
     * @return organAreaCount/slideArea结果
     */
    private BigDecimal getDensityResult(Integer organAreaCount, String slideArea) {
        return (0 == organAreaCount) ? BigDecimal.ZERO : commonJsonParser.bigDecimalDivideCheck(new BigDecimal(organAreaCount), new BigDecimal(slideArea));
    }
}
