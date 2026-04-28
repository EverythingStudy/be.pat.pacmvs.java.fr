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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 附睾
 *
 * @author yxy
 */
@Slf4j
@Service("Epididymides_3")
public class Epididymides_3ParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.info("Epididymides_3ParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> resultsMap = new HashMap<>();
        // A：输出小管/附睾管黏膜上皮面积（单个）×103 μm2
        Annotation annotationA = new Annotation();
        annotationA.setAreaName("输出小管/附睾管黏膜上皮面积（单个）");
        annotationA.setAreaUnit(MULTIPLIED_SQ_UM_THOUSAND);
        this.commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "32F0F5", annotationA, 1);
        // B：输出小管/附睾管黏膜上皮面积（全片）mm2
        Annotation organ = this.commonJsonParser.getOrganArea(jsonTask, "32F0F5");
        BigDecimal organAreaB = organ.getStructureAreaNum();
        // C：输出小管/附睾管黏膜上皮周长（单个）μm
        Annotation annotationC = new Annotation();
        annotationC.setPerimeterName("输出小管/附睾管黏膜上皮周长（单个）");
        annotationC.setPerimeterUnit(UM);
        this.commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "32F0F5", annotationC, 1);
        // D：输出小管/附睾管管腔面积（单个）×103 μm2
        Annotation annotationD = new Annotation();
        annotationD.setAreaName("输出小管/附睾管管腔面积（单个）");
        annotationD.setAreaUnit(MULTIPLIED_SQ_UM_THOUSAND);
        this.commonJsonParser.putAnnotationDynamicData(jsonTask, "32F0F5", "32F0F4", annotationD, 1, true);
        // E：输出小管/附睾管管腔面积（全片）mm2
        BigDecimal organAreaE = this.commonJsonParser.getInsideOrOutside(jsonTask, "32F0F5", "32F0F4", true).getStructureAreaNum();
        // F：精子面积（单个）×103 μm2
        Annotation annotationF = new Annotation();
        annotationF.setAreaName("精子面积（单个）");
        annotationF.setAreaUnit(MULTIPLIED_SQ_UM_THOUSAND);
        this.commonJsonParser.putAnnotationDynamicData(jsonTask, "32F0F5", "32F0F7", annotationF, 1, true);
        // G：精子面积（全片）mm2
        BigDecimal organAreaG = this.commonJsonParser.getInsideOrOutside(jsonTask, "32F0F5", "32F0F7", true).getStructureAreaNum();
        // H：黏膜上皮细胞核数量（单个）
        Annotation annotationH = new Annotation();
        annotationH.setCountName("黏膜上皮细胞核数量（单个）");
        annotationH.setCountUnit(PIECE);
        this.commonJsonParser.putAnnotationDynamicData(jsonTask, "32F0F5", "32F0F6", annotationH, 1, true);
        // I：组织轮廓面积mm2
        SingleSlide singleSlide = this.singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal organAreaI = new BigDecimal(singleSlide.getArea());

        // 指标存放
        BigDecimal one = new BigDecimal("100");
        resultsMap.put("输出小管/附睾管黏膜上皮面积（全片）", createIndicator(organAreaB, SQ_MM, "32F0F5"));
        resultsMap.put("输出小管/附睾管管腔面积（全片）", createIndicator(organAreaE, PIECE, this.areaUtils.getStructureIds("32F0F5", "32F0F4")));
        resultsMap.put("精子面积（全片）", createIndicator(organAreaG, SQ_MM, this.areaUtils.getStructureIds("32F0F5", "32F0F7")));

        // 1=I：附睾面积
        resultsMap.put("附睾面积", createNameIndicator("Epididymal area", organAreaI, SQ_MM, "32F111"));
        // 2=B/I：输出小管和附睾管面积占比（全片）
        BigDecimal bi = getProportion(organAreaB, organAreaI);
        resultsMap.put("输出小管和附睾管面积占比（全片）", createNameIndicator("Efferent ducts and epididymal ducts area%（all）", bi, PERCENTAGE, this.areaUtils.getStructureIds("32F0F5", "32F111")));
        // 3=1-B/I：间质面积占比
        resultsMap.put("间质面积占比", createNameIndicator("Mesenchyme area%", one.subtract(bi), PERCENTAGE, this.areaUtils.getStructureIds("32F0F5", "32F111")));
        // 6=G/E：精子面积占比（全片）
        resultsMap.put("精子面积占比（全片）", createNameIndicator("Sperm area% (all)", getProportion(organAreaG, organAreaE), PERCENTAGE, this.areaUtils.getStructureIds("32F0F5", "32F0F4", "32F0F7")));

        // 4=1-D/A：黏膜上皮面积占比（单个）
        List<BigDecimal> list4 = new ArrayList<>();
        // 5=F/D：精子面积占比（单个）
        List<BigDecimal> list5 = new ArrayList<>();
        // 7=H/C：黏膜上皮细胞核密度（单个）
        List<BigDecimal> list7 = new ArrayList<>();
        // 8=\sqrt{\smash[b]{A/\pi}}-\sqrt{\smash[b]{D/\pi}}：黏膜上皮厚度（单个）
        List<BigDecimal> list8 = new ArrayList<>();
        List<Annotation> annotationList = this.commonJsonParser.getStructureContourList(jsonTask, "32F0F5");
        for (Annotation i : annotationList) {
            // mm2
            BigDecimal areaD = this.commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "32F0F4", true).getStructureAreaNum();
            list4.add(one.subtract(getProportion(areaD, i.getStructureAreaNum())));

            // mm2
            BigDecimal areaF = this.commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "32F0F7", true).getStructureAreaNum();
            list5.add(getProportion(areaF, areaD));

            Integer countH = this.commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "32F0F6", true).getCount();
            // um
            BigDecimal cum = i.getStructurePerimeterNum().multiply(new BigDecimal(1000));
            BigDecimal hc7 = this.commonJsonParser.bigDecimalDivideCheck(new BigDecimal(countH), cum);
            list7.add(hc7);

            // um2
            String dum2 = this.commonJsonParser.convertToMicrometer(areaD.toString());
            // um2
            String aum2 = this.commonJsonParser.convertToMicrometer(i.getStructureAreaNum().toString());
            BigDecimal aPai = this.commonJsonParser.bigDecimalDivideCheck(new BigDecimal(aum2), new BigDecimal(A));
            BigDecimal sqrt1 = this.commonJsonParser.sqrt(aPai);
            BigDecimal dPai = this.commonJsonParser.bigDecimalDivideCheck(new BigDecimal(dum2), new BigDecimal(A));
            BigDecimal sqrt2 = this.commonJsonParser.sqrt(dPai);
            BigDecimal result8 = sqrt1.subtract(sqrt2);
            list8.add(result8);
        }
        String spermAreaPer4 = MathUtils.getConfidenceInterval(list4);
        String spermAreaPer5 = MathUtils.getConfidenceInterval(list5);
        String spermAreaPer7 = MathUtils.getConfidenceInterval(list7);
        String spermAreaPer8 = MathUtils.getConfidenceInterval(list8);
        resultsMap.put("黏膜上皮面积占比（单个）", createNameIndicator("Mucosal epithelium area% (per)", spermAreaPer4, PERCENTAGE, this.areaUtils.getStructureIds("32F0F5", "32F0F4")));
        resultsMap.put("精子面积占比（单个）", createNameIndicator("Sperm area% (per)", spermAreaPer5, PERCENTAGE, this.areaUtils.getStructureIds("32F0F5", "32F0F4", "32F0F7")));
        resultsMap.put("黏膜上皮细胞核密度（单个）", createNameIndicator("Mucosal epithelial nucleus% (per)", spermAreaPer7, PIECE_UM, this.areaUtils.getStructureIds("32F0F5", "32F0F6")));
        resultsMap.put("黏膜上皮厚度（单个）", createNameIndicator("Average thickness of mucosal epithelium (per) ", spermAreaPer8, UM, this.areaUtils.getStructureIds("32F0F5", "32F0F4")));

        this.aiForecastService.addAiForecast(jsonTask.getSingleId(), resultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Epididymides_3";
    }
}
