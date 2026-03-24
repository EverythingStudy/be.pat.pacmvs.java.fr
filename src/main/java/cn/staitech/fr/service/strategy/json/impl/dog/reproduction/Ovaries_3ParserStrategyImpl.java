package cn.staitech.fr.service.strategy.json.impl.dog.reproduction;

import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 卵巢
 *
 * @author yxy
 */
@Slf4j
@Service("Ovaries_3")
public class Ovaries_3ParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.info("Ovaries_3ParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> resultsMap = new HashMap<>();
       /* // A：卵泡数量
        Integer countA = this.commonJsonParser.getOrganAreaCount(jsonTask, "3240CB");
        // B：卵泡面积（全片）mm2
        BigDecimal organAreaB = this.commonJsonParser.getOrganArea(jsonTask, "3240CB").getStructureAreaNum();
        // C：卵泡面积（单个）×103 μm2
        Annotation annotationC = new Annotation();
        annotationC.setAreaName("卵泡面积（单个）");
        annotationC.setAreaUnit(MULTIPLIED_SQ_UM_THOUSAND);
        this.commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "3240CB", annotationC, 1);*/
        // D：血管面积μm2
        BigDecimal organAreaD = this.commonJsonParser.getOrganArea(jsonTask, "324003").getStructureAreaNum();
        organAreaD = organAreaD.multiply(new BigDecimal(1000000)).setScale(3, RoundingMode.HALF_UP);
        // E：血管外红细胞面积μm2
        BigDecimal organAreaE = commonJsonParser.getInsideOrOutside(jsonTask, "324003", "14C004", false).getStructureAreaNum();
        organAreaE = organAreaE.multiply(new BigDecimal(1000000)).setScale(3, RoundingMode.HALF_UP);
        // F：血管内红细胞面积μm2
        BigDecimal organAreaF = commonJsonParser.getInsideOrOutside(jsonTask, "324003", "14C004", true).getStructureAreaNum();
        organAreaF = organAreaF.multiply(new BigDecimal(1000000)).setScale(3, RoundingMode.HALF_UP);
        // G：组织轮廓面积mm2
        SingleSlide singleSlide = this.singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal organAreaG = new BigDecimal(singleSlide.getArea());

        // 指标存放
       /* resultsMap.put("卵泡数量", createIndicator(countA, PIECE, "3240CB"));
        resultsMap.put("卵泡面积（全片）", createIndicator(organAreaB, SQ_MM, "3240CB"));*/
        resultsMap.put("血管面积", createIndicator(organAreaD, SQ_UM, "324003"));
        resultsMap.put("血管外红细胞面积", createIndicator(organAreaE, SQ_UM, this.areaUtils.getStructureIds("324003", "324004")));
        resultsMap.put("血管内红细胞面积", createIndicator(organAreaF, SQ_UM, this.areaUtils.getStructureIds("324003", "324004")));
       /* // 1=A：卵泡数量
        resultsMap.put("卵泡数量", createNameIndicator("Follicle numbers", countA, PIECE, "3240CB"));

        // 2=C：卵泡面积（单个）×103 μm2
        List<BigDecimal> list = new ArrayList<>();
        List<Annotation> annotationList = this.commonJsonParser.getStructureContourList(jsonTask, "3240CB");
        for (Annotation i : annotationList) {
            String area = this.areaUtils.convertToSquareMicrometer(i.getStructureAreaNum().toString());
            list.add(new BigDecimal(area));
        }
        String spermAreaPer = MathUtils.getConfidenceInterval(list);
        resultsMap.put("卵泡面积（单个）", createNameIndicator("Follicle area", spermAreaPer, MULTIPLIED_SQ_UM_THOUSAND, "3240CB"));

        // 3=A/G：卵泡密度
        BigDecimal ag3 = this.commonJsonParser.bigDecimalDivideCheck(BigDecimal.valueOf(countA), organAreaG);
        resultsMap.put("卵泡密度", createNameIndicator("Follicle density", ag3, SQ_MM_PIECE, this.areaUtils.getStructureIds("3240CB", "324111")));
        // 4=B/G：卵泡面积占比
        BigDecimal bg4 = this.commonJsonParser.bigDecimalDivideCheck(organAreaB, organAreaG);
        resultsMap.put("卵泡面积占比", createNameIndicator("Follicle area%", bg4, PERCENTAGE, this.areaUtils.getStructureIds("3240CB", "324111")));*/
        // 5=D/G：血管面积占比
        BigDecimal dg5 = getProportion(organAreaD, organAreaG.multiply(new BigDecimal(1000000)));
        resultsMap.put("血管面积占比", createNameIndicator("Vessel area%", dg5, PERCENTAGE, this.areaUtils.getStructureIds("324003", "324111")));
        // 6=E/G：血管外红细胞面积占比
        BigDecimal eg6 = getProportion(organAreaE, organAreaG.multiply(new BigDecimal(1000000)));
        resultsMap.put("血管外红细胞面积占比", createNameIndicator("Extravascular Erythrocyte area%", eg6, PERCENTAGE, this.areaUtils.getStructureIds("324003", "324004", "324111")));
        // 7=F/G：血管内红细胞面积占比
        BigDecimal fg7 = getProportion(organAreaF, organAreaG.multiply(new BigDecimal(1000000)));
        resultsMap.put("血管内红细胞面积占比", createNameIndicator("Intravascular Erythrocyte area%", fg7, PERCENTAGE, this.areaUtils.getStructureIds("324003", "324004", "324111")));
        // 8=G：卵巢面积
        resultsMap.put("卵巢面积", createNameIndicator("Ovary area", organAreaG, SQ_MM, "324111"));

        this.aiForecastService.addAiForecast(jsonTask.getSingleId(), resultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Ovaries_3";
    }
}
