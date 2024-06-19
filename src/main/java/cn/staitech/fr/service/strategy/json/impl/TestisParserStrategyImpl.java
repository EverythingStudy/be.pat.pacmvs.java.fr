package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
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
 * 睾丸-TE
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
        BigDecimal organAreaB = areaUtils.getOrganArea(jsonTask, "12E0FA");// B生精小管面积（全片）
        Annotation annotation = commonJsonParser.getOrganArea(jsonTask, "12E0FA");// C生精小管周长（单个）
        BigDecimal perimeterC = annotation.getStructurePerimeterNum();
        Integer areaCountD = areaUtils.getOrganAreaCount(jsonTask, "12E0FA");// D生精小管数量
        Integer areaCountH = areaUtils.getOrganAreaCount(jsonTask, "12E0FE");// H间质细胞核数量
        BigDecimal organAreaI = areaUtils.getOrganArea(jsonTask, "12E003");// I血管面积
        String slideAreaJ = areaUtils.getFineContourArea(jsonTask.getSingleId());// J组织轮廓
        BigDecimal organAreaJ = BigDecimal.valueOf(Long.parseLong(slideAreaJ));
        // todo F生精细胞核数量（单个）
        // todo G支持细胞核数量（单个）

        // 算法输出指标
        resultsMap.put("生精小管面积（单个）", createDefaultIndicator());// A生精小管面积（单个）
        resultsMap.put("生精小管面积（全片）", createIndicator(organAreaB, SQ_MM));
        resultsMap.put("生精小管周长（单个）", createDefaultIndicator());
        resultsMap.put("生精小管数量", createIndicator(areaCountD, PIECE));
        resultsMap.put("生精小管内腔面积（单个）", createDefaultIndicator());// E生精小管内腔面积（单个）
        resultsMap.put("生精细胞核数量（单个）", createDefaultIndicator());
        resultsMap.put("支持细胞核数量（单个）", createDefaultIndicator());
        resultsMap.put("间质细胞核数量", createIndicator(areaCountH, PIECE));
        resultsMap.put("血管面积", createIndicator(organAreaI, SQ_MM));




        // 计算指标
        BigDecimal densityResult = getDensityResult(areaCountD, slideAreaJ);

        // 生精小管面积占比
        BigDecimal seminiferousTubulesArea = commonJsonParser.getProportion(organAreaB, organAreaJ);
        // 生精小管面积（单个）
        List<BigDecimal> list1 = new ArrayList<>();
        List<Annotation> annotationList1 = commonJsonParser.getStructureContourList(jsonTask,"12E0FA");
        for (Annotation annotation1 : annotationList1) {
            list1.add(annotation1.getStructureAreaNum());
        }
        String seminiferousTubulesAreaSingle = MathUtils.getConfidenceInterval(list1);
        // 生精小管厚度（单个）
        List<BigDecimal> list2 = new ArrayList<>();
        for (Annotation i : annotationList1) {
            Annotation annotation2 = commonJsonParser.getInsideOrOutside(jsonTask, i.getContour(), "12E0FB", true);
            BigDecimal sqrt1 = commonJsonParser.sqrt(i.getStructurePerimeterNum().divide(BigDecimal.valueOf(Long.parseLong(A)), 3, RoundingMode.HALF_UP));
            BigDecimal sqrt2 = commonJsonParser.sqrt(annotation2.getStructurePerimeterNum().divide(BigDecimal.valueOf(Long.parseLong(A)), 3, RoundingMode.HALF_UP));

            list2.add(sqrt1.divide(sqrt2, 3, RoundingMode.HALF_UP));
        }
        String averageThicknessOfSpermatogenicTubules = MathUtils.getConfidenceInterval(list2);
        // 生精细胞核密度（单个）
        List<BigDecimal> list3 = new ArrayList<>();
        for (Annotation i : annotationList1) {
            Annotation annotation2 = commonJsonParser.getInsideOrOutside(jsonTask, i.getContour(), "12E0FC", true);
            list3.add(BigDecimal.valueOf(annotation2.getCount()).divide(i.getStructurePerimeterNum(), 3, RoundingMode.HALF_UP));
        }
        String nucleusDensityOfSpermatogenicCells = MathUtils.getConfidenceInterval(list3);
        // 支持细胞核密度（单个）
        List<BigDecimal> list4 = new ArrayList<>();
        for (Annotation i : annotationList1) {
            Annotation annotation2 = commonJsonParser.getInsideOrOutside(jsonTask, i.getContour(), "12E0FD", true);
            list4.add(BigDecimal.valueOf(annotation2.getCount()).divide(i.getStructurePerimeterNum(), 3, RoundingMode.HALF_UP));

        }
        String nucleusDensityOfSupportCells = MathUtils.getConfidenceInterval(list4);

        // 生精细胞核：支持细胞核（单个）
        List<BigDecimal> list5 = new ArrayList<>();
        for (Annotation i : annotationList1) {
            Annotation annotation2 = commonJsonParser.getInsideOrOutside(jsonTask, i.getContour(), "12E0FC", true);
            Annotation annotation3 = commonJsonParser.getInsideOrOutside(jsonTask, i.getContour(), "12E0FD", true);
            list5.add(BigDecimal.valueOf(annotation2.getCount()).divide(BigDecimal.valueOf(annotation3.getCount()), 3, RoundingMode.HALF_UP));
        }
        String nucleusDensityOfSpermatogenicCellsSupportCells = MathUtils.getConfidenceInterval(list5);
        // 血管面积占比
        BigDecimal vesselArea = commonJsonParser.getProportion(organAreaI, organAreaJ);
        // 间质细胞核：生精小管
        BigDecimal interstitialCellNuclei = BigDecimal.valueOf(areaCountH / areaCountD);
        // 间质面积占比
        BigDecimal interstitialArea = commonJsonParser.getProportion(organAreaJ.subtract(organAreaB), organAreaJ);
        // 间质细胞核密度
        BigDecimal interstitialCellNucleiDensity = commonJsonParser.getProportion(BigDecimal.valueOf(areaCountH), organAreaJ);


        // 产品呈现指标
        resultsMap.put("睾丸面积", createNameIndicator("Testicular area", slideAreaJ, SQ_MM));
        resultsMap.put("生精小管面积（全片）", createNameIndicator("Seminiferous tubules area (all)", organAreaB, SQ_MM));
        resultsMap.put("生精小管面积占比", createNameIndicator("Seminiferous tubules area%", seminiferousTubulesArea, PERCENTAGE));
        resultsMap.put("生精小管面积（单个）", createNameIndicator("Seminiferous tubules area (per)", seminiferousTubulesAreaSingle, SQ_UM_THOUSAND));
        resultsMap.put("生精小管密度", createNameIndicator("Density of seminiferous tubules", densityResult, SQ_UM_THOUSAND));
        resultsMap.put("生精小管厚度（单个）", createNameIndicator("Average thickness of spermatogenic tubules (per)", averageThicknessOfSpermatogenicTubules, UM));
        resultsMap.put("生精细胞核密度（单个）", createNameIndicator("Nucleus density of Spermatogenic cells (per)", nucleusDensityOfSpermatogenicCells, MM_PIECE));
        resultsMap.put("支持细胞核密度（单个）", createNameIndicator("Nucleus density of Sertoli (per)", nucleusDensityOfSupportCells, MM_PIECE));
        resultsMap.put("生精细胞核：支持细胞核（单个）", createNameIndicator("Spermatogenic nucleus:  Sertoli nucleus ratio (per)", nucleusDensityOfSpermatogenicCellsSupportCells, NOT));
        resultsMap.put("血管面积占比", createNameIndicator("Vessel area%", vesselArea, PERCENTAGE));
        resultsMap.put("间质细胞核：生精小管", createNameIndicator("Leydig nucleus: seminiferous tubules ratio", interstitialCellNuclei, PERCENTAGE));
        resultsMap.put("间质面积占比", createNameIndicator("Mesenchyme area%", interstitialArea, PERCENTAGE));
        resultsMap.put("间质细胞核密度", createNameIndicator("Nucleus density of leydig cells", interstitialCellNucleiDensity, SQ_MM_PIECE));
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
            densityResult = areaCountBD.divide(slideAreaBD, 3, RoundingMode.HALF_UP);// D/J
        }
        return densityResult;
    }

    @Override
    public String getAlgorithmCode() {
        return "Testis";
    }
}
