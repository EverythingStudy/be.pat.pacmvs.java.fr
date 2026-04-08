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
 * 前列腺
 *
 * @author yxy
 */
@Slf4j
@Service("Prostate_3")
public class Prostate_3ParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.info("Prostate_3ParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> resultsMap = new HashMap<>();
        // A：腺泡/导管面积（单个）×103 μm2
        Annotation annotationA = new Annotation();
        annotationA.setAreaName("腺泡/导管面积（单个）");
        annotationA.setAreaUnit(MULTIPLIED_SQ_UM_THOUSAND);
        this.commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "32C06C", annotationA, 1);
        // B：腺泡/导管面积（全片）mm2
        BigDecimal organAreaB = this.commonJsonParser.getOrganArea(jsonTask, "32C06C").getStructureAreaNum();
        // C：腺泡/导管周长（单个）μm
        Annotation annotationC = new Annotation();
        annotationC.setPerimeterName("腺泡/导管周长（单个）");
        annotationC.setPerimeterUnit(UM);
        this.commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "32C06C", annotationC, 1);
        // D 管腔面积（单个）
        Annotation annotationD = new Annotation();
        annotationD.setAreaName("管腔面积（单个）");
        annotationD.setAreaUnit(MULTIPLIED_SQ_UM_THOUSAND);
        this.commonJsonParser.putAnnotationDynamicData(jsonTask, "32C06C", "32C0F4", annotationD, 1, true);
        // E：腺泡/导管细胞核数量（单个）
        Annotation annotationE = new Annotation();
        annotationE.setCountName("腺泡/导管细胞核数量（单个）");
        annotationE.setCountUnit(PIECE);
        this.commonJsonParser.putAnnotationDynamicData(jsonTask, "32C06C", "32C061", annotationE, 1, true);
        // F：组织轮廓面积mm2
        SingleSlide singleSlide = this.singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal organAreaF = new BigDecimal(singleSlide.getArea());
        // G：尿道及尿道周围面积mm2
        BigDecimal organAreaG = this.commonJsonParser.getOrganArea(jsonTask, "32C060").getStructureAreaNum();

        // 指标存放
        BigDecimal one = new BigDecimal("100");
        resultsMap.put("腺泡/导管面积（全片）", createIndicator(organAreaB, SQ_MM, "32C06C"));

        // 1=F-G：前列腺面积mm2
        resultsMap.put("前列腺面积", createNameIndicator("Prostate gland area", organAreaF.subtract(organAreaG), SQ_MM, "32C111"));
        // 2=B/F：腺泡/导管面积占比
        resultsMap.put("腺泡/导管面积占比", createNameIndicator("Acinar area%", getProportion(organAreaB, organAreaF), PERCENTAGE, this.areaUtils.getStructureIds("32C111", "32C06C")));
        // 3=1-D/A：腺泡/导管面积占比（单个）
        // 4=D/A：管腔占比（单个）
        // 5=E/C：腺泡/导管细胞核密度（单个）
        List<BigDecimal> list3 = new ArrayList<>();
        List<BigDecimal> list4 = new ArrayList<>();
        List<BigDecimal> list5 = new ArrayList<>();
        List<Annotation> annotationList = this.commonJsonParser.getStructureContourList(jsonTask, "32C06C");
        for (Annotation i : annotationList) {
            // mm2
            BigDecimal areaD = this.commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "32C0F4", true).getStructureAreaNum();
            BigDecimal da4 = getProportion(areaD, i.getStructureAreaNum());
            list4.add(da4);
            list3.add(one.subtract(da4));
            Integer e = this.commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "32C061", true).getCount();
            // mm转μm
            BigDecimal ec5 = this.commonJsonParser.bigDecimalDivideCheck(new BigDecimal(e), i.getStructurePerimeterNum().multiply(new BigDecimal(1000)));
            list5.add(ec5);
        }
        String spermAreaPer3 = MathUtils.getConfidenceInterval(list3);
        String spermAreaPer4 = MathUtils.getConfidenceInterval(list4);
        String spermAreaPer5 = MathUtils.getConfidenceInterval(list5);

        resultsMap.put("腺泡/导管面积占比（单个）", createNameIndicator("Acinar epithelial area% (per)", spermAreaPer3, PERCENTAGE, this.areaUtils.getStructureIds("32C06C", "32C0F4")));
        resultsMap.put("管腔占比（单个）", createNameIndicator("Acinar lumen area% (per)", spermAreaPer4, PERCENTAGE, this.areaUtils.getStructureIds("32C06C", "32C0F4")));
        resultsMap.put("腺泡/导管细胞核密度（单个）", createNameIndicator("Nucleus density of acinar epithelium (per)", spermAreaPer5, PIECE_UM, this.areaUtils.getStructureIds("32C06C", "32C061")));

        this.aiForecastService.addAiForecast(jsonTask.getSingleId(), resultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Prostate_3";
    }
}
