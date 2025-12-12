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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author wanglibei
 * @version V1.0
 * @ClassName: TestisParserStrategyImpl
 * @Description-d:睾丸
 * @date 2025年7月22日
 */
@Slf4j
@Service("Testis")
public class TestisParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.info("TestisParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {

        Map<String, IndicatorAddIn> resultsMap = new HashMap<>();

        // 获取各种指标
        // B 生精小管面积（全片） mm2
        BigDecimal organAreaB = commonJsonParser.getOrganArea(jsonTask, "12E0FA").getStructureAreaNum();
        // D 生精小管数量
        Integer areaCountD = areaUtils.getOrganAreaCount(jsonTask, "12E0FA");
        // H 间质细胞核数量
        Integer areaCountH = areaUtils.getOrganAreaCount(jsonTask, "12E0FE");
        // I血管面积
        BigDecimal organAreaI = areaUtils.getOrganArea(jsonTask, "12E003");
        // J组织轮廓
        String slideAreaJ = areaUtils.getFineContourArea(jsonTask.getSingleId());
        BigDecimal organAreaJ = BigDecimal.valueOf(Double.parseDouble(slideAreaJ));

        Annotation annotationBy = new Annotation();
        // F 生精细胞核数量（单个）
        annotationBy.setCountName("生精细胞核数量（单个）");
        commonJsonParser.putAnnotationDynamicData(jsonTask, "12E0FA", "12E0FC", annotationBy);
        // G 支持细胞核数量（单个）
        annotationBy.setCountName("支持细胞核数量（单个）");
        commonJsonParser.putAnnotationDynamicData(jsonTask, "12E0FA", "12E0FD", annotationBy);
        //A 生精小管面积（单个）
        annotationBy.setCountName(null);
        annotationBy.setAreaName("生精小管内腔面积（单个）");
        annotationBy.setAreaUnit(SQ_UM_THOUSAND);
        commonJsonParser.putAnnotationDynamicData(jsonTask, "12E0FA", "12E0FB", annotationBy, 1,true);
        //B 生精小管面积（全片）
        annotationBy.setAreaName("生精小管面积（单个）");
        annotationBy.setAreaUnit(SQ_UM_THOUSAND);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "12E0FA", annotationBy, 1);
        //C 生精小管周长（单个）
        annotationBy.setAreaName(null);
        annotationBy.setAreaUnit(null);
        annotationBy.setPerimeterName("生精小管周长（单个）");
        annotationBy.setPerimeterUnit(MM);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "12E0FA", annotationBy, 3);
        //E 生精小管内腔面积（单个）
        Annotation annotationBy1 = new Annotation();
        annotationBy1.setAreaName("生精小管内腔面积（单个）");
        annotationBy1.setAreaUnit(SQ_UM_THOUSAND);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "12E0FB", annotationBy1, 1);

        // 算法输出指标

        resultsMap.put("生精小管数量", createIndicator(areaCountD, PIECE, "12E0FA"));
        resultsMap.put("间质细胞核数量", createIndicator(areaCountH, PIECE, areaUtils.getStructureIds("12E0FA", "12E0FE")));
        resultsMap.put("血管面积", createIndicator(organAreaI.setScale(3, RoundingMode.HALF_UP), SQ_MM, "12E003"));


        // 计算指标
        // 4 生精小管面积（单个）
        List<BigDecimal> list1 = new ArrayList<>();
        List<Annotation> annotationList1 = commonJsonParser.getStructureContourList(jsonTask, "12E0FA");
        for (Annotation annotation1 : annotationList1) {
            String area = areaUtils.micrometerToSquareMicrometer(annotation1.getArea());
            list1.add(BigDecimal.valueOf(Double.parseDouble(area)));
        }

        // 6 生精小管厚度（单个）
        List<BigDecimal> list2 = new ArrayList<>();
        for (Annotation i : annotationList1) {
            Annotation annotation2 = commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "12E0FB", true);
            if (i.getArea() != null && annotation2.getArea() != null) {
                BigDecimal sqrt1 = commonJsonParser.sqrt(commonJsonParser.bigDecimalDivideCheck(BigDecimal.valueOf(Double.parseDouble(i.getArea())), BigDecimal.valueOf(Double.parseDouble(A))));
                BigDecimal sqrt2 = commonJsonParser.sqrt(commonJsonParser.bigDecimalDivideCheck(BigDecimal.valueOf(Double.parseDouble(annotation2.getArea())), BigDecimal.valueOf(Double.parseDouble(A))));
                BigDecimal res = sqrt1.subtract(sqrt2);
                list2.add(res);
            }
        }
        // 7 生精细胞核密度（单个）
        List<BigDecimal> list3 = new ArrayList<>();
        for (Annotation i : annotationList1) {
            Annotation annotation2 = commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "12E0FC", true);
            list3.add(commonJsonParser.bigDecimalDivideCheck(BigDecimal.valueOf(annotation2.getCount()), i.getStructurePerimeterNum()));
        }
        // 8 个/mm 支持细胞核密度（单个）
        List<BigDecimal> list4 = new ArrayList<>();
        for (Annotation i : annotationList1) {
            Annotation annotation2 = commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "12E0FD", true);
            list4.add(commonJsonParser.bigDecimalDivideCheck(BigDecimal.valueOf(annotation2.getCount()), i.getStructurePerimeterNum()));

        }

        // 9 生精细胞核：支持细胞核（单个）
        List<BigDecimal> list5 = new ArrayList<>();
        for (Annotation i : annotationList1) {
            Annotation annotation2 = commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "12E0FC", true);
            Annotation annotation3 = commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "12E0FD", true);
            list5.add(commonJsonParser.bigDecimalDivideCheck(BigDecimal.valueOf(annotation2.getCount()), BigDecimal.valueOf(annotation3.getCount())));
        }

        // 产品呈现指标

        //1 睾丸面积	mm2 1=J
        resultsMap.put("睾丸面积", createNameIndicator("Testicular area", organAreaJ.setScale(3, RoundingMode.HALF_UP), SQ_MM, "12E111"));
        //2 生精小管面积（全片）mm2	2=B
        resultsMap.put("生精小管面积（全片）", createNameIndicator("Seminiferous tubules area (all)", organAreaB.setScale(3, RoundingMode.HALF_UP), SQ_MM, "12E0FA"));
        //3 生精小管面积占比	%3=B/J
        resultsMap.put("生精小管面积占比", createNameIndicator("Seminiferous tubules area%", getProportion(organAreaB, organAreaJ), PERCENTAGE, areaUtils.getStructureIds("12E0FA", "12E111")));
        //4 生精小管面积（单个）103 μm2	4=A
        resultsMap.put("生精小管面积（单个）", createNameIndicator("Seminiferous tubules area (per)", MathUtils.getConfidenceInterval(list1), SQ_UM_THOUSAND, "12E0FA"));
        //5 生精小管密度	个/mm2 5=D/J
        resultsMap.put("生精小管密度", createNameIndicator("Density of seminiferous tubules", bigDecimalDivideCheck(new BigDecimal(areaCountD), organAreaJ).toString(), SQ_MM_PIECE, areaUtils.getStructureIds("12E0FA", "12E111")));
        //6 生精小管厚度（单个）μm	6=\sqrt{\smash[b]{A/\pi}}-\sqrt{\smash[b]{E/\pi}}
        resultsMap.put("生精小管厚度（单个）", createNameIndicator("Average thickness of spermatogenic tubules (per)", MathUtils.getConfidenceInterval(list2), UM, areaUtils.getStructureIds("12E0FA", "12E0FB")));
        //7 生精细胞核密度（单个）个/mm	7=F/C
        resultsMap.put("生精细胞核密度（单个）", createNameIndicator("Nucleus density of Spermatogenic cells (per)", MathUtils.getConfidenceInterval(list3), MM_PIECE, areaUtils.getStructureIds("12E0FA", "12E0FC")));
        //8 支持细胞核密度（单个）	8=G/C
        resultsMap.put("支持细胞核密度（单个）", createNameIndicator("Nucleus density of Sertoli (per)", MathUtils.getConfidenceInterval(list4), MM_PIECE, areaUtils.getStructureIds("12E0FA", "12E0FD")));
        //9 生精细胞核：支持细胞核（单个）无	9=F/G
        resultsMap.put("生精细胞核：支持细胞核（单个）", createNameIndicator("Spermatogenic nucleus:  Sertoli nucleus ratio (per)", MathUtils.getConfidenceInterval(list5), NOT, areaUtils.getStructureIds("12E0FA", "12E0FC", "12E0FD")));
        // 10 血管面积占比 %
        resultsMap.put("血管面积占比", createNameIndicator("Vessel area%", getProportion(organAreaI, organAreaJ), PERCENTAGE, areaUtils.getStructureIds("12E003", "12E111")));
        //11 间质细胞核：生精小管	11=H/D
        resultsMap.put("间质细胞核：生精小管", createNameIndicator("Leydig nucleus: seminiferous tubules ratio", bigDecimalDivideCheck(BigDecimal.valueOf(areaCountH), BigDecimal.valueOf(areaCountD)), NOT, areaUtils.getStructureIds("12E0FA", "12E0FE")));
        //12 间质面积占比	% 12=(J-B)/J
        resultsMap.put("间质面积占比", createNameIndicator("Mesenchyme area%", getProportion(organAreaJ.subtract(organAreaB), organAreaJ), PERCENTAGE, areaUtils.getStructureIds("12E111", "12E0FA")));
        //13 间质细胞核密度 个/mm2	13=H/J
        resultsMap.put("间质细胞核密度", createNameIndicator("Nucleus density of leydig cells", bigDecimalDivideCheck(BigDecimal.valueOf(areaCountH), organAreaJ), SQ_MM_PIECE, areaUtils.getStructureIds("12E0FE", "12E111")));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), resultsMap);
    }

    /**
     * 生精小管密度计算
     */
    private BigDecimal getDensityResult(Integer areaCountD, String slideAreaJ) {
        BigDecimal areaCountBD = new BigDecimal(areaCountD);
        BigDecimal slideAreaBD = new BigDecimal(slideAreaJ);
        BigDecimal densityResult;
        if (areaCountBD.compareTo(BigDecimal.ZERO) == 0 || slideAreaBD.compareTo(BigDecimal.ZERO) == 0) {
            densityResult = BigDecimal.ZERO;
        } else {
            densityResult = commonJsonParser.bigDecimalDivideCheck(areaCountBD, slideAreaBD);
        }
        return densityResult;
    }

    @Override
    public String getAlgorithmCode() {
        return "Testis";
    }
}
