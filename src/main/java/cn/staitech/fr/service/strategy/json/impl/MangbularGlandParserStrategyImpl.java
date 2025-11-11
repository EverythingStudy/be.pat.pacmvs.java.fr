package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
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
 * @author wanglibei
 * @version V1.0
 * @ClassName: MangbularGlandParserStrategyImpl
 * @Description-d:颌下腺
 * @date 2025年7月21日
 */
@Slf4j
@Service("Mangbular_gland")
public class MangbularGlandParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.info("MangbularGlandParserStrategyImpl init");
    }


    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        // 获取各种指标
        // A 颗粒管（红色）数量
        Integer organAreaCountA = commonJsonParser.getOrganAreaCount(jsonTask, "10B125");
        // B 黏液腺细胞核数量
        Integer organAreaCountB = commonJsonParser.getOrganAreaCount(jsonTask, "10B128");
        // D 有血管壁的血管面积
        BigDecimal organAreaD = commonJsonParser.getOrganArea(jsonTask, "10B003").getStructureAreaNum();
        // E有血管壁的血管数量
        Integer organAreaCountE = commonJsonParser.getOrganAreaCount(jsonTask, "10B003");
        // F红细胞面积
        BigDecimal organAreaF = commonJsonParser.getOrganArea(jsonTask, "10B004").getStructureAreaNum();
        // H组织轮廓
        String slideArea = singleSlideMapper.selectById(jsonTask.getSingleId()).getArea();
        BigDecimal organAreaH = BigDecimal.valueOf(Double.parseDouble(slideArea));
        // I颗粒管（红色）面积（全片）
        BigDecimal organAreaI = commonJsonParser.getOrganArea(jsonTask, "10B125").getStructureAreaNum();
        // C 颗粒管内细胞核数量（单个）
        Annotation annotationBy = new Annotation();
        annotationBy.setCountName("颗粒管内细胞核数量（单个）");
        commonJsonParser.putAnnotationDynamicData(jsonTask, "10B125", "10B126", annotationBy);
        // G颗粒管（红色）面积（单个）
        Annotation annotation2 = new Annotation();
        annotation2.setAreaName("颗粒管（红色）面积（单个）");
        annotation2.setAreaUnit(SQ_UM_THOUSAND);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "10B125", annotation2, 1);
        Map<String, IndicatorAddIn> resultsMap = new HashMap<>();
        // 算法输出指标
        resultsMap.put("颗粒管数量", createIndicator(organAreaCountA, PIECE, "10B125"));
        resultsMap.put("黏液腺细胞核数量", createIndicator(organAreaCountB, PIECE, "10B128"));
        resultsMap.put("血管面积", createIndicator(organAreaD, SQ_MM, "10B003"));
        resultsMap.put("血管数量", createIndicator(organAreaCountE, PIECE, "10B003"));
        resultsMap.put("红细胞面积", createIndicator(organAreaF, SQ_MM, "10B004"));
        resultsMap.put("颗粒管面积（全片）", createIndicator(organAreaI, SQ_MM, "10B125"));


        // 计算指标
        //1 颗粒管密度 个/mm2 1=A/H
        BigDecimal densityResult = getDensityResult(organAreaCountA, slideArea);
        //2 黏液腺细胞核密度 个/mm2 2=B/H
        BigDecimal nucleusResult = getDensityResult(organAreaCountB, slideArea);
        //3 颗粒管细胞核密度(单个颗粒管) 个/mm2  3=C/G
        List<Annotation> annotationList = commonJsonParser.getStructureContourList(jsonTask, "10B125");
        List<BigDecimal> list = new ArrayList<>();
        for (Annotation annotation : annotationList) {
            Annotation annotation1 = commonJsonParser.getContourInsideOrOutside(jsonTask, annotation.getContour(), "10B126", true);
            BigDecimal granularConvolutedTubule = commonJsonParser.bigDecimalDivideCheck(BigDecimal.valueOf(annotation1.getCount()), annotation.getStructureAreaNum());
            list.add(granularConvolutedTubule);
        }
        String granularConvolutedTubules = MathUtils.getConfidenceInterval(list);
        //4 血管面积占比 % 4=D/H
        BigDecimal vesselArea = commonJsonParser.getProportion(organAreaD, organAreaH);
        //5 红细胞面积占比 %  5=F/H
        BigDecimal erythrocyteArea = commonJsonParser.getProportion(organAreaF, organAreaH);
        // 7 颗粒管面积占比（全片） 7=I/H
        BigDecimal granularConvolutedTubulesArea = commonJsonParser.getProportion(organAreaI, organAreaH);

        // 产品呈现指标
        resultsMap.put("颗粒管密度", createNameIndicator("Density of granular convoluted tubules (eosinophilic)", densityResult, SQ_MM_PIECE, areaUtils.getStructureIds("10B125", "10B111")));
        resultsMap.put("黏液腺细胞核密度", createNameIndicator("Nucleus density of mucous gland", nucleusResult, SQ_MM_PIECE, areaUtils.getStructureIds("10B128", "10B111")));
        resultsMap.put("颗粒管细胞核密度(单个)", createNameIndicator("Nucleus density of granular convoluted tubule (per)", granularConvolutedTubules, SQ_MM_PIECE, areaUtils.getStructureIds("10B125", "10B126")));
        resultsMap.put("血管面积占比", createNameIndicator("Vessel area%", vesselArea, PERCENTAGE, areaUtils.getStructureIds("10B003", "10B111")));
        resultsMap.put("红细胞面积占比", createNameIndicator("Erythrocyte area%", erythrocyteArea, PERCENTAGE, areaUtils.getStructureIds("10B004", "10B111")));
        resultsMap.put("颌下腺面积", createNameIndicator("Submadibular gland area", organAreaH.setScale(3, BigDecimal.ROUND_HALF_UP).toString(), SQ_MM, "10B111"));
        resultsMap.put("颗粒管面积占比（全片）", createNameIndicator("Granular convoluted tubules area% (all)", granularConvolutedTubulesArea, PERCENTAGE, areaUtils.getStructureIds("10B125", "10B111")));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), resultsMap);
    }

    /**
     * 计算指标
     *
     * @return organAreaCount/slideArea结果
     */
    private BigDecimal getDensityResult(Integer organAreaCount, String slideArea) {
        return (0 == organAreaCount) ? BigDecimal.ZERO : commonJsonParser.bigDecimalDivideCheck(new BigDecimal(organAreaCount), new BigDecimal(slideArea));
    }

    @Override
    public String getAlgorithmCode() {
        return "Mangbular_gland";
    }
}
