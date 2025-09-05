package cn.staitech.fr.service.strategy.json.impl;

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

    /**
     * 结构	编码
     * 肌纤维	15C02A
     * 血管	15C003
     * 红细胞	15C004
     * 间质	15C027
     * 组织轮廓	15C111
     * 算法输出指标	指标代码（仅限本文档）	单位(保留小数点后3位)	备注
     * 肌纤维面积（单个）	A	平方毫米	无
     * 间质面积	B	103平方微米	 若输出结果为多个则相加
     * 血管面积	C	103平方微米	 若输出结果为多个则相加
     * 红细胞面积	D	平方微米	 若输出结果为多个则相加
     * 血管内红细胞面积	E	平方微米	若输出结果为多个则相加
     * 组织轮廓	F	103平方微米	无
     * <p>
     * 产品呈现指标	指标代码（仅限本文档）	单位(保留小数点后3位)	English	计算方式	备注
     * 肌纤维面积（单个）	1	平方毫米	Muscle fiber area (per)	1=A	以95%置信区间和均数±标准差呈现
     * 间质面积占比	2	%	Mesenchyme area %	2=B/F	无
     * 血管面积占比	3	%	Vessel area%	3=C/F	无
     * 血管内红细胞面积占比	4	%	Intravascular erythrocyte area%	4=E/F
     * 血管外红细胞面积占比	5	%	Extravascular erythrocyte area%	5=(D-E)/F
     * 骨骼肌面积	6	103平方微米	Skeletal muscle area	6=F
     */

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> resultsMap = new HashMap<>();

        // 获取各种指标
        // B间质面积
        BigDecimal organAreaB = commonJsonParser.getOrganArea(jsonTask, "15C027").getStructureAreaNum();
        // C血管面积
        BigDecimal organAreaC = commonJsonParser.getOrganArea(jsonTask, "15C003").getStructureAreaNum();
        // D红细胞面积
        BigDecimal organAreaD = commonJsonParser.getOrganArea(jsonTask, "15C004").getStructureAreaNum();
        Annotation annotation = commonJsonParser.getInsideOrOutside(jsonTask, "15C003", "15C004", true);
        // E血管内红细胞面积
        BigDecimal organAreaE = annotation.getStructureAreaNum();
//        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());// F精细轮廓总面积
//
//        BigDecimal organF = BigDecimal.valueOf(Double.parseDouble(slideArea));
        // F精细轮廓总面积
        BigDecimal organF = commonJsonParser.getOrganArea(jsonTask, "15C111").getStructureAreaNum();
        // 间质面积占比
        BigDecimal mesenchymeArea = commonJsonParser.getProportion(organAreaB, organF);
        // 血管面积占比
        BigDecimal vesselArea = commonJsonParser.getProportion(organAreaC, organF);
        // 血管内红细胞面积占比
        BigDecimal vesselInErythrocyteArea = commonJsonParser.getProportion(commonJsonParser.getBigDecimalValue(organAreaE), organF);
        // 血管外红细胞面积占比
        BigDecimal vesselOutErythrocyteArea = commonJsonParser.getProportion(commonJsonParser.getBigDecimalValue(organAreaD.subtract(organAreaE)), organF);

        //肌纤维面积（单个）
        List<Annotation> annotationList = commonJsonParser.getStructureContourList(jsonTask, "15C02A");
        List<BigDecimal> annotationAreaList = annotationList.stream().map(anno -> new BigDecimal(anno.getArea()).setScale(3, RoundingMode.DOWN)).collect(Collectors.toList());
        String muscleFiberArea = MathUtils.getConfidenceInterval(annotationAreaList);

        Annotation annotation1 = new Annotation();
        annotation1.setAreaName("肌纤维面积（单个）");
        annotation1.setAreaUnit(SQ_MM);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "15C02A", annotation1, 2);


        // 算法输出指标
        resultsMap.put("肌纤维面积（单个）", createDefaultIndicator("15C02A"));// A肌纤维面积（单个）
//        resultsMap.put("间质面积", createIndicator(areaUtils.convertToSquareMicrometer(organAreaB.toString()), SQ_UM_THOUSAND, "15C027"));
//        resultsMap.put("血管面积", createIndicator(areaUtils.convertToSquareMicrometer(organAreaC.toString()), SQ_UM_THOUSAND, "15C003"));
//        resultsMap.put("红细胞面积", createIndicator(areaUtils.convertToMicrometer(organAreaD.toString()), SQ_UM, "15C004"));
//        resultsMap.put("血管内红细胞面积", createIndicator(areaUtils.convertToMicrometer(organAreaE.toString()), SQ_UM, "15C003,15C004"));
//
//        // 产品呈现指标
//        resultsMap.put("肌纤维面积(单个)", createNameIndicator("Muscle fiber area (per)", muscleFiberArea, SQ_UM, "15C02A"));
//        resultsMap.put("间质面积占比", createNameIndicator("Mesenchyme area %", mesenchymeArea, PERCENTAGE, "15C027"));
//        resultsMap.put("血管面积占比", createNameIndicator("Vessel area%", vesselArea, PERCENTAGE, "15C003"));
//        resultsMap.put("血管内红细胞面积占比", createNameIndicator("Intravascular erythrocyte area%", vesselInErythrocyteArea.setScale(3, RoundingMode.UP), PERCENTAGE, "15C003,15C004"));
//        resultsMap.put("血管外红细胞面积占比", createNameIndicator("Extravascular erythrocyte area%", vesselOutErythrocyteArea.setScale(3, RoundingMode.UP), PERCENTAGE, "15C004,15C003"));
        resultsMap.put("骨骼肌面积", createNameIndicator("Skeletal muscle area", areaUtils.convertToSquareMicrometer(organF.toString()), SQ_UM_THOUSAND, "15C111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), resultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Muscle";
    }
}
