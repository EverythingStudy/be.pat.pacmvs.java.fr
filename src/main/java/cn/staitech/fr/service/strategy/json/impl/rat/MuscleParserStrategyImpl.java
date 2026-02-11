package cn.staitech.fr.service.strategy.json.impl.rat;

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
 * 大鼠-骨骼肌-MU
 */
@Slf4j
@Service("Muscle")
public class MuscleParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.info("MuscleParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        //A 肌纤维面积（单个）
        List<Annotation> annotationList = commonJsonParser.getStructureContourList(jsonTask, "15C02A");
        // B间质面积
        BigDecimal organAreaB = commonJsonParser.getOrganArea(jsonTask, "15C027").getStructureAreaNum();
        // C血管面积
        BigDecimal organAreaC = commonJsonParser.getOrganArea(jsonTask, "15C003").getStructureAreaNum();
        // D红细胞面积
        BigDecimal organAreaD = commonJsonParser.getOrganArea(jsonTask, "15C004").getStructureAreaNum();
        // E血管内红细胞面积
        Annotation annotation = commonJsonParser.getInsideOrOutside(jsonTask, "15C003", "15C004", true);
        BigDecimal organAreaE = annotation.getStructureAreaNum();
        // F精细轮廓总面积
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());
        // BigDecimal organF = commonJsonParser.getOrganArea(jsonTask, "15C111").getStructureAreaNum();
        BigDecimal organF = BigDecimal.valueOf(Double.parseDouble(slideArea));
        //1 肌纤维面积（单个） mm2 1=A
        List<BigDecimal> annotationAreaList = annotationList.stream().map(anno -> new BigDecimal(anno.getArea()).setScale(3, RoundingMode.DOWN)).collect(Collectors.toList());
        String muscleFiberArea = MathUtils.getConfidenceInterval(annotationAreaList);

        //2 间质面积占比 % 2=B/F
        BigDecimal mesenchymeArea = commonJsonParser.getProportion(organAreaB, organF);
        //3 血管面积占比 % 3=C/F
        BigDecimal vesselArea = commonJsonParser.getProportion(organAreaC, organF);
        //4 血管内红细胞面积占比 4=E/F
        BigDecimal vesselInErythrocyteArea = commonJsonParser.getProportion(commonJsonParser.getBigDecimalValue(organAreaE), organF);
        //5 血管外红细胞面积占比 5=(D-E)/F
        BigDecimal vesselOutErythrocyteArea = commonJsonParser.getProportion(commonJsonParser.getBigDecimalValue(organAreaD.subtract(organAreaE)), organF);

        Map<String, IndicatorAddIn> resultsMap = new HashMap<>();
        Annotation annotation1 = new Annotation();
        annotation1.setAreaName("肌纤维面积（单个）");
        annotation1.setAreaUnit(SQ_UM);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "15C02A", annotation1, 2);

        resultsMap.put("间质面积", createIndicator(areaUtils.convertToSquareMicrometer(organAreaB.toString()), SQ_UM_THOUSAND, "15C027"));
        resultsMap.put("血管面积", createIndicator(areaUtils.convertToSquareMicrometer(organAreaC.toString()), SQ_UM_THOUSAND, "15C003"));
        resultsMap.put("红细胞面积", createIndicator(areaUtils.convertToMicrometer(organAreaD.toString()), SQ_UM, "15C004"));
        resultsMap.put("血管内红细胞面积", createIndicator(areaUtils.convertToMicrometer(organAreaE.toString()), SQ_UM, "15C003,15C004"));

        // 产品呈现指标
        resultsMap.put("肌纤维面积(单个)", createNameIndicator("Muscle fiber area (per)", muscleFiberArea, SQ_UM, "15C02A"));
        resultsMap.put("间质面积占比", createNameIndicator("Mesenchyme area %", mesenchymeArea, PERCENTAGE, "15C027"));
        resultsMap.put("血管面积占比", createNameIndicator("Vessel area%", vesselArea, PERCENTAGE, "15C003"));
        resultsMap.put("血管内红细胞面积占比", createNameIndicator("Intravascular erythrocyte area%", vesselInErythrocyteArea.setScale(3, RoundingMode.UP), PERCENTAGE, "15C003,15C004"));
        resultsMap.put("血管外红细胞面积占比", createNameIndicator("Extravascular erythrocyte area%", vesselOutErythrocyteArea.setScale(3, RoundingMode.UP), PERCENTAGE, "15C004,15C003"));
        resultsMap.put("骨骼肌面积", createNameIndicator("Skeletal muscle area", organF, SQ_MM, "15C111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), resultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Muscle";
    }
}
