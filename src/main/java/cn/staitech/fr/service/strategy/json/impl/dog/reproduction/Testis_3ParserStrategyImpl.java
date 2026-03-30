package cn.staitech.fr.service.strategy.json.impl.dog.reproduction;

import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
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
 * 睾丸
 *
 * @author yxy
 */
@Slf4j
@Service("Testis_3")
public class Testis_3ParserStrategyImpl extends AbstractCustomParserStrategy {
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private AreaUtils areaUtils;
    @Resource
    private CommonJsonCheck commonJsonCheck;
    @Resource
    private SingleSlideMapper singleSlideMapper;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("Testis_3ParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> resultsMap = new HashMap<>();
        // A：生精小管面积（单个）×103 μm2
        Annotation annotationA = new Annotation();
        annotationA.setAreaName("生精小管面积（单个）");
        annotationA.setAreaUnit(MULTIPLIED_SQ_UM_THOUSAND);
        this.commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "32E0FA", annotationA, 1);
        // B：生精小管面积（全片）mm2
        Annotation organ = this.commonJsonParser.getOrganArea(jsonTask, "32E0FA");
        BigDecimal organAreaB = organ.getStructureAreaNum();
        // C：生精小管周长（单个）μm
        Annotation annotationC = new Annotation();
        annotationC.setPerimeterName("生精小管周长（单个）");
        annotationC.setPerimeterUnit(UM);
        this.commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "32E0FA", annotationC, 1);
        // D：生精小管数量
        Integer countD = this.commonJsonParser.getOrganAreaCount(jsonTask, "32E0FA");
        // E：生精小管内腔面积（单个）×103 μm2
        Annotation annotationE = new Annotation();
        annotationE.setAreaName("生精小管内腔面积（单个）");
        annotationE.setAreaUnit(MULTIPLIED_SQ_UM_THOUSAND);
        this.commonJsonParser.putAnnotationDynamicData(jsonTask, "32E0FA", "32E0FB", annotationE, 1, true);
        // F：生精细胞核数量（单个）
        Annotation annotationF = new Annotation();
        annotationF.setCountName("生精细胞核数量（单个）");
        annotationF.setCountUnit(PIECE);
        this.commonJsonParser.putAnnotationDynamicData(jsonTask, "32E0FA", "32E0FC", annotationF, 1, true);
        // G：支持细胞核数量（单个）
        Annotation annotationG = new Annotation();
        annotationG.setCountName("支持细胞核数量（单个）");
        annotationG.setCountUnit(PIECE);
        this.commonJsonParser.putAnnotationDynamicData(jsonTask, "32E0FA", "32E0FD", annotationF, 1, true);
        // H：间质细胞核数量
        Integer countH = this.commonJsonParser.getOrganAreaCount(jsonTask, "32E0FE");
        // I：组织轮廓面积
        SingleSlide singleSlide = this.singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal organAreaI = new BigDecimal(singleSlide.getArea());

        // 指标存放
        BigDecimal one = new BigDecimal("100");
        resultsMap.put("生精小管数量", createIndicator(countD, PIECE, "32E0FA"));
        resultsMap.put("间质细胞核数量", createIndicator(countH, PIECE, "32E0FE"));

        // 1=I：睾丸面积
        resultsMap.put("睾丸面积", createNameIndicator("Testicular area", organAreaI, SQ_MM, "32E111"));
        // 2=B：生精小管面积（全片）
        resultsMap.put("生精小管面积（全片）", createNameIndicator("Seminiferous tubules area (all)", organAreaB, SQ_MM, "32E0FA"));
        // 3=B/I：生精小管面积占比
        resultsMap.put("生精小管面积占比", createNameIndicator("Seminiferous tubules area%", getProportion(organAreaB, organAreaI), PERCENTAGE, this.areaUtils.getStructureIds("32E0FA", "32E111")));
        // 5=D/I：生精小管密度
        resultsMap.put("生精小管密度", createNameIndicator("Density of seminiferous tubules", this.commonJsonParser.bigDecimalDivideCheck(new BigDecimal(countD), organAreaI), SQ_MM_PIECE, this.areaUtils.getStructureIds("32E0FA", "32E111")));

        // 4=A：生精小管面积（单个）
        List<BigDecimal> list4 = new ArrayList<>();
        // 6=\sqrt{\smash[b]{A/\pi}}-\sqrt{\smash[b]{E/\pi}}：生精小管厚度（单个）
        List<BigDecimal> list6 = new ArrayList<>();
        // 7=F/C：生精细胞核密度（单个）
        List<BigDecimal> list7 = new ArrayList<>();
        // 8=G/C：支持细胞核密度（单个）
        List<BigDecimal> list8 = new ArrayList<>();
        // 9=F/G：生精细胞核：支持细胞核（单个）
        List<BigDecimal> list9 = new ArrayList<>();
        List<Annotation> annotationList = this.commonJsonParser.getStructureContourList(jsonTask, "32E0FA");
        for (Annotation i : annotationList) {
            // ×103 μm2
            String a = this.commonJsonParser.convertToSquareMicrometer(i.getStructureAreaNum().toString());
            list4.add(new BigDecimal(a));

            // mm2
            BigDecimal areaE = this.commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "32E0FB", true).getStructureAreaNum();
            // um2
            String eum2 = this.commonJsonParser.convertToMicrometer(areaE.toString());
            // um2
            String aum2 = this.commonJsonParser.convertToMicrometer(i.getStructureAreaNum().toString());
            BigDecimal aPai = this.commonJsonParser.bigDecimalDivideCheck(new BigDecimal(aum2), new BigDecimal(A));
            BigDecimal sqrt1 = this.commonJsonParser.sqrt(aPai);
            BigDecimal ePai = this.commonJsonParser.bigDecimalDivideCheck(new BigDecimal(eum2), new BigDecimal(A));
            BigDecimal sqrt2 = this.commonJsonParser.sqrt(ePai);
            BigDecimal result6 = sqrt1.subtract(sqrt2);
            list6.add(result6);

            Integer countF = this.commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "32E0FC", true).getCount();
            // um
            String cum = this.commonJsonParser.convertToSquareMicrometer(i.getStructurePerimeterNum().toString());
            BigDecimal fc7 = this.commonJsonParser.bigDecimalDivideCheck(new BigDecimal(countF), new BigDecimal(cum));
            list7.add(fc7);

            Integer countG = this.commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "32E0FD", true).getCount();
            BigDecimal gc8 = this.commonJsonParser.bigDecimalDivideCheck(new BigDecimal(countG), new BigDecimal(cum));
            list8.add(gc8);

            BigDecimal fg9 = this.commonJsonParser.bigDecimalDivideCheck(new BigDecimal(countF), new BigDecimal(countG));
            list9.add(fg9);
        }
        String spermAreaPer4 = MathUtils.getConfidenceInterval(list4);
        String spermAreaPer6 = MathUtils.getConfidenceInterval(list6);
        String spermAreaPer7 = MathUtils.getConfidenceInterval(list7);
        String spermAreaPer8 = MathUtils.getConfidenceInterval(list8);
        String spermAreaPer9 = MathUtils.getConfidenceInterval(list9);
        resultsMap.put("生精小管面积（单个）", createNameIndicator("Seminiferous tubules area (per)", spermAreaPer4, MULTIPLIED_SQ_UM_THOUSAND, "32E0FA"));
        resultsMap.put("生精小管厚度（单个）", createNameIndicator("Average thickness of spermatogenic tubules (per)", spermAreaPer6, UM, this.areaUtils.getStructureIds("32E0FA", "32E0FB")));
        resultsMap.put("生精细胞核密度（单个）", createNameIndicator("Nucleus density of Spermatogenic cells (per)", spermAreaPer7, PIECE_UM, this.areaUtils.getStructureIds("32E0FA", "32E0FC")));
        resultsMap.put("支持细胞核密度（单个）", createNameIndicator("Nucleus density of Sertoli (per) ", spermAreaPer8, PIECE_UM, this.areaUtils.getStructureIds("32E0FA", "32E0FD")));
        resultsMap.put("生精细胞核：支持细胞核（单个）", createNameIndicator("Spermatogenic nucleus:  Sertoli nucleus ratio (per) ", spermAreaPer9, null, this.areaUtils.getStructureIds("32E0FA", "32E0FC", "32E0FD")));

        // 10=H/D：间质细胞核：生精小管
        resultsMap.put("间质细胞核：生精小管", createNameIndicator("Leydig nucleus: seminiferous tubules ratio", this.commonJsonParser.bigDecimalDivideCheck(new BigDecimal(countH), new BigDecimal(countD)), null, this.areaUtils.getStructureIds("32E0FA", "32E0FE")));
        // 11=（I-B）/I=1-B/I：间质面积占比
        BigDecimal bi11 = getProportion(organAreaB, organAreaI);
        BigDecimal result11 = one.subtract(bi11);
        resultsMap.put("间质面积占比", createNameIndicator("Mesenchyme area%", result11, PERCENTAGE, this.areaUtils.getStructureIds("32E0FA", "32E111")));
        // 12=H/I：间质细胞核密度
        resultsMap.put("间质细胞核密度", createNameIndicator("Nucleus density of leydig cells", this.commonJsonParser.bigDecimalDivideCheck(new BigDecimal(countH), organAreaI), SQ_MM_PIECE, this.areaUtils.getStructureIds("32E0FE", "32E111")));

        this.aiForecastService.addAiForecast(jsonTask.getSingleId(), resultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Testis_3";
    }
}
