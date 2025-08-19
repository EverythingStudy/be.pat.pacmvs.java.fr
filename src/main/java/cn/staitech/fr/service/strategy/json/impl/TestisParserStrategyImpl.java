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
 * 
* @ClassName: TestisParserStrategyImpl
* @Description-d:睾丸
* @author wanglibei
* @date 2025年7月22日
* @version V1.0
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
        BigDecimal organAreaJ = BigDecimal.valueOf(Double.parseDouble(slideAreaJ));
        // todo F生精细胞核数量（单个）
        // todo G支持细胞核数量（单个）
        Annotation annotationBy = new Annotation();
        annotationBy.setCountName("生精细胞核数量（单个）");
        commonJsonParser.putAnnotationDynamicData(jsonTask,"12E0FA","12E0FC",annotationBy);
        annotationBy.setCountName("支持细胞核数量（单个）");
        commonJsonParser.putAnnotationDynamicData(jsonTask,"12E0FA","12E0FD",annotationBy);
        annotationBy.setCountName(null);
//        annotationBy.setAreaName("生精小管内腔面积（单个）");
//        annotationBy.setAreaUnit(SQ_UM_THOUSAND);
//        commonJsonParser.putAnnotationDynamicData(jsonTask,"12E0FA","12E0FB",annotationBy,1);
        annotationBy.setAreaName("生精小管面积（单个）");
        annotationBy.setAreaUnit(SQ_UM_THOUSAND);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask,"12E0FA",annotationBy,1);
        annotationBy.setAreaName(null);
        annotationBy.setAreaUnit(null);
        annotationBy.setPerimeterName("生精小管周长（单个）");
        annotationBy.setPerimeterUnit("毫米");
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask,"12E0FA",annotationBy,3);
        Annotation annotationBy1 = new Annotation();
        annotationBy1.setAreaName("生精小管内腔面积（单个）");
        annotationBy1.setAreaUnit(SQ_UM_THOUSAND);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask,"12E0FB",annotationBy1,1);

        // 算法输出指标
        /**
            A	生精小管面积（单个）	12E0FA
			B	生精小管面积（全片）	12E0FA
			C	生精小管周长（单个）	12E0FA
			D	生精小管数量	12E0FA
			E	生精小管内腔面积（单个）	12E0FA、12E0FB
			F	生精细胞核数量（单个）	12E0FA、12E0FC
			G	支持细胞核数量（单个）	12E0FA、12E0FD
			H	间质细胞核数量	12E0FA、12E0FE
			I	血管面积	12E003
			J	组织轮廓面积	12E111
			
			睾丸面积	1=J
			生精小管面积（全片）	2=B
			生精小管面积占比	3=B/J
			生精小管面积（单个）	4=A
			生精小管密度	5=D/J
			生精小管厚度（单个）	6=\sqrt{\smash[b]{A/\pi}}-\sqrt{\smash[b]{E/\pi}}
			生精细胞核密度（单个）	7=F/C
			支持细胞核密度（单个）	8=G/C
			生精细胞核：支持细胞核（单个）	9=F/G
			血管面积占比	10=I/J
			间质细胞核：生精小管	11=H/D
			间质面积占比	12=(J-B)/J
			间质细胞核密度	13=H/J
         */
        resultsMap.put("生精小管面积（单个）", createDefaultIndicator("12E0FA"));// A生精小管面积（单个）
        resultsMap.put("生精小管面积（全片）", createIndicator(organAreaB.setScale(3, RoundingMode.HALF_UP), SQ_MM,"12E0FA"));
        resultsMap.put("生精小管周长（单个）", createDefaultIndicator("12E0FA"));
        resultsMap.put("生精小管数量", createIndicator(areaCountD, PIECE,"12E0FA"));
        resultsMap.put("生精小管内腔面积（单个）", createDefaultIndicator(areaUtils.getStructureIds("12E0FA","12E0FB")));// E生精小管内腔面积（单个）
        resultsMap.put("生精细胞核数量（单个）", createDefaultIndicator(areaUtils.getStructureIds("12E0FA","12E0FC")));
        resultsMap.put("支持细胞核数量（单个）", createDefaultIndicator(areaUtils.getStructureIds("12E0FA","12E0FD")));
        resultsMap.put("间质细胞核数量", createIndicator(areaCountH, PIECE,areaUtils.getStructureIds("12E0FA","12E0FE")));
//        resultsMap.put("血管面积", createIndicator(organAreaI.setScale(3, RoundingMode.HALF_UP), SQ_MM,"12E003"));



        // 计算指标
        BigDecimal densityResult = getDensityResult(areaCountD, slideAreaJ);
        // 生精小管面积占比
        BigDecimal seminiferousTubulesArea = commonJsonParser.getProportion(organAreaB, organAreaJ);
        // 生精小管面积（单个）
        List<BigDecimal> list1 = new ArrayList<>();
        List<Annotation> annotationList1 = commonJsonParser.getStructureContourList(jsonTask,"12E0FA");
        for (Annotation annotation1 : annotationList1) {
            String area = areaUtils.micrometerToSquareMicrometer(annotation1.getArea());
            list1.add(BigDecimal.valueOf(Double.parseDouble(area)));
        }
        String seminiferousTubulesAreaSingle = MathUtils.getConfidenceInterval(list1);
        // 生精小管厚度（单个）
        List<BigDecimal> list2 = new ArrayList<>();
        for (Annotation i : annotationList1) {
            Annotation annotation2 = commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "12E0FB", true);
            if(i.getArea() != null && annotation2.getArea() != null){
                BigDecimal sqrt1 = commonJsonParser.sqrt(commonJsonParser.bigDecimalDivideCheck(BigDecimal.valueOf(Double.parseDouble(i.getArea())),BigDecimal.valueOf(Double.parseDouble(A))));
                BigDecimal sqrt2 = commonJsonParser.sqrt(commonJsonParser.bigDecimalDivideCheck(BigDecimal.valueOf(Double.parseDouble(annotation2.getArea())),BigDecimal.valueOf(Double.parseDouble(A))));
                BigDecimal res = sqrt1.subtract(sqrt2);
                list2.add(res);
            }
        }
        String averageThicknessOfSpermatogenicTubules = MathUtils.getConfidenceInterval(list2);
        // 生精细胞核密度（单个）
        List<BigDecimal> list3 = new ArrayList<>();
        for (Annotation i : annotationList1) {
            Annotation annotation2 = commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "12E0FC", true);
            list3.add(commonJsonParser.bigDecimalDivideCheck(BigDecimal.valueOf(annotation2.getCount()),i.getStructurePerimeterNum()));
        }
        String nucleusDensityOfSpermatogenicCells = MathUtils.getConfidenceInterval(list3);
        // 支持细胞核密度（单个）
        List<BigDecimal> list4 = new ArrayList<>();
        for (Annotation i : annotationList1) {
            Annotation annotation2 = commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "12E0FD", true);
            list4.add(commonJsonParser.bigDecimalDivideCheck(BigDecimal.valueOf(annotation2.getCount()),i.getStructurePerimeterNum()));

        }
        String nucleusDensityOfSupportCells = MathUtils.getConfidenceInterval(list4);

        // 生精细胞核：支持细胞核（单个）
        List<BigDecimal> list5 = new ArrayList<>();
        for (Annotation i : annotationList1) {
            Annotation annotation2 = commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "12E0FC", true);
            Annotation annotation3 = commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "12E0FD", true);
            list5.add(commonJsonParser.bigDecimalDivideCheck(BigDecimal.valueOf(annotation2.getCount()),BigDecimal.valueOf(annotation3.getCount())));
        }
        String nucleusDensityOfSpermatogenicCellsSupportCells = MathUtils.getConfidenceInterval(list5);
        // 血管面积占比
        BigDecimal vesselArea = commonJsonParser.getProportion(organAreaI, organAreaJ);
        // 间质细胞核：生精小管

        BigDecimal interstitialCellNuclei = BigDecimal.ZERO;
        if(areaCountD != 0 && areaCountH != 0){
            interstitialCellNuclei = BigDecimal.valueOf(areaCountH).divide(BigDecimal.valueOf(areaCountD), 3, RoundingMode.HALF_UP);
        }
        // 间质面积占比
        BigDecimal interstitialArea = commonJsonParser.getProportion(organAreaJ.subtract(organAreaB), organAreaJ);
        // 间质细胞核密度
        BigDecimal interstitialCellNucleiDensity = commonJsonParser.getProportionMultiply(BigDecimal.valueOf(areaCountH), organAreaJ);


        // 产品呈现指标
        /**
        A	生精小管面积（单个）	12E0FA
		B	生精小管面积（全片）	12E0FA
		C	生精小管周长（单个）	12E0FA
		D	生精小管数量	12E0FA
		E	生精小管内腔面积（单个）	12E0FA、12E0FB
		F	生精细胞核数量（单个）	12E0FA、12E0FC
		G	支持细胞核数量（单个）	12E0FA、12E0FD
		H	间质细胞核数量	12E0FA、12E0FE
		I	血管面积	12E003
		J	组织轮廓面积	12E111
		
		睾丸面积	1=J
		生精小管面积（全片）	2=B
		生精小管面积占比	3=B/J
		生精小管面积（单个）	4=A
		生精小管密度	5=D/J
		生精小管厚度（单个）	6=\sqrt{\smash[b]{A/\pi}}-\sqrt{\smash[b]{E/\pi}}
		生精细胞核密度（单个）	7=F/C
		支持细胞核密度（单个）	8=G/C
		生精细胞核：支持细胞核（单个）	9=F/G
		血管面积占比	10=I/J
		间质细胞核：生精小管	11=H/D
		间质面积占比	12=(J-B)/J
		间质细胞核密度	13=H/J
     */
        //睾丸面积	1=J
        resultsMap.put("睾丸面积", createNameIndicator("Testicular area", organAreaJ.setScale(3, RoundingMode.HALF_UP), SQ_MM,"12E111"));
        //生精小管面积（全片）	2=B
        resultsMap.put("生精小管面积（全片）", createNameIndicator("Seminiferous tubules area (all)", organAreaB.setScale(3, RoundingMode.HALF_UP), SQ_MM,"12E0FA"));
        //生精小管面积占比	3=B/J
        resultsMap.put("生精小管面积占比", createNameIndicator("Seminiferous tubules area%", seminiferousTubulesArea, PERCENTAGE,areaUtils.getStructureIds("12E0FA","12E111")));
        //生精小管面积（单个）	4=A
        resultsMap.put("生精小管面积（单个）", createNameIndicator("Seminiferous tubules area (per)", seminiferousTubulesAreaSingle, SQ_UM_THOUSAND,"12E0FA"));
        //生精小管密度	5=D/J
        resultsMap.put("生精小管密度", createNameIndicator("Density of seminiferous tubules", densityResult.setScale(3, RoundingMode.HALF_UP), SQ_MM_PIECE,areaUtils.getStructureIds("12E0FA","12E111")));
        //生精小管厚度（单个）	6=\sqrt{\smash[b]{A/\pi}}-\sqrt{\smash[b]{E/\pi}}
        resultsMap.put("生精小管厚度（单个）", createNameIndicator("Average thickness of spermatogenic tubules (per)", averageThicknessOfSpermatogenicTubules, UM,areaUtils.getStructureIds("12E0FA","12E0FB")));
        //生精细胞核密度（单个）	7=F/C
        resultsMap.put("生精细胞核密度（单个）", createNameIndicator("Nucleus density of Spermatogenic cells (per)", nucleusDensityOfSpermatogenicCells, SQ_MM_PIECE,areaUtils.getStructureIds("12E0FA","12E0FC")));
        //支持细胞核密度（单个）	8=G/C
        resultsMap.put("支持细胞核密度（单个）", createNameIndicator("Nucleus density of Sertoli (per)", nucleusDensityOfSupportCells, SQ_MM_PIECE,areaUtils.getStructureIds("12E0FA","12E0FD")));
        //生精细胞核：支持细胞核（单个）	9=F/G
        resultsMap.put("生精细胞核：支持细胞核（单个）", createNameIndicator("Spermatogenic nucleus:  Sertoli nucleus ratio (per)", nucleusDensityOfSpermatogenicCellsSupportCells, NOT,areaUtils.getStructureIds("12E0FA","12E0FC","12E0FD")));
//        resultsMap.put("血管面积占比", createNameIndicator("Vessel area%", vesselArea, PERCENTAGE,areaUtils.getStructureIds("12E003","12E111")));
        //间质细胞核：生精小管	11=H/D
        resultsMap.put("间质细胞核：生精小管", createNameIndicator("Leydig nucleus: seminiferous tubules ratio", interstitialCellNuclei, NOT,areaUtils.getStructureIds("12E0FA","12E0FE")));
        //间质面积占比	12=(J-B)/J
        resultsMap.put("间质面积占比", createNameIndicator("Mesenchyme area%", interstitialArea, PERCENTAGE,areaUtils.getStructureIds("12E111","12E0FA")));
        //间质细胞核密度	13=H/J
        resultsMap.put("间质细胞核密度", createNameIndicator("Nucleus density of leydig cells", interstitialCellNucleiDensity, SQ_MM_PIECE,areaUtils.getStructureIds("12E0FA","12E0FA","12E111")));
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
