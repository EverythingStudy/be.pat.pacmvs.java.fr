package cn.staitech.fr.service.strategy.json.impl.dog.endocrinology;

import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.service.strategy.json.OutlineCustom;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 
* @ClassName: PituitaryParserStrategyImpl
* @Description:犬-垂体
* @author wanglibei
* @date 2026年2月11日
* @version V1.0
 */
@Slf4j
@Component("Pituitary_3")
public class PituitaryParserStrategyImpl extends AbstractCustomParserStrategy implements OutlineCustom {
    @Resource
    private AreaUtils areaUtils;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private CommonJsonCheck commonJsonCheck;
    @Resource
    private SingleSlideMapper singleSlideMapper;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("PituitaryParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("大鼠垂体构指标计算开始");
        // A神经部面积 A 平方毫米 若多个数据则相加输出
        BigDecimal pituitaryA = getOrganArea(jsonTask, "10607F").getStructureAreaNum();
        pituitaryA = commonJsonParser.getBigDecimalValue(pituitaryA.setScale(3, RoundingMode.HALF_UP));
        // B 中间部面积 B 平方毫米 若多个数据则相加输出
        BigDecimal pituitaryB = getOrganArea(jsonTask, "106081").getStructureAreaNum();
        pituitaryB = commonJsonParser.getBigDecimalValue(pituitaryB.setScale(3, RoundingMode.HALF_UP));
        // C 远侧部面积 C 平方毫米 若多个数据则相加输出
        BigDecimal pituitaryC = getOrganArea(jsonTask, "106083").getStructureAreaNum();
        pituitaryC = commonJsonParser.getBigDecimalValue(pituitaryC.setScale(3, RoundingMode.HALF_UP));
        //D 红细胞面积  平方毫米 数据相加输出
        BigDecimal pituitaryD = getOrganArea(jsonTask, "106004").getStructureAreaNum();
        pituitaryD = commonJsonParser.getBigDecimalValue(pituitaryD.setScale(3, RoundingMode.HALF_UP));
        // E 神经部细胞核数量 E 个 无
        Integer mucosaCountE = getOrganAreaCount(jsonTask, "106080");
        // F 中间部细胞核数量 F 个 无
        Integer mucosaCountF = getOrganAreaCount(jsonTask, "106082");
        // G 远侧部细胞核数量 G 个 无
        // Integer mucosaCountG = getOrganAreaCount(jsonTask, "106084");
        // 组织轮廓面积H
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());
        BigDecimal pituitaryH = new BigDecimal(slideArea);
        //BigDecimal pituitaryH = getOrganArea(jsonTask, "106111").getStructureAreaNum();
        pituitaryH = commonJsonParser.getBigDecimalValue(pituitaryH.setScale(3, RoundingMode.HALF_UP));
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();

        indicatorResultsMap.put("神经部面积", createIndicator(String.valueOf(pituitaryA.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "10607F"));
        indicatorResultsMap.put("中间部面积", createIndicator(String.valueOf(pituitaryB.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "106081"));
        indicatorResultsMap.put("远侧部面积", createIndicator(String.valueOf(pituitaryC.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "106083"));
        indicatorResultsMap.put("红细胞面积", createIndicator(String.valueOf(pituitaryD.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "106004"));
        indicatorResultsMap.put("神经部细胞核数量", createIndicator(String.valueOf(mucosaCountE), PIECE, "10607F,106080"));
        indicatorResultsMap.put("中间部细胞核数量", createIndicator(String.valueOf(mucosaCountF), PIECE, "10607F,106082"));
        //indicatorResultsMap.put("远侧部细胞核数量", createIndicator(String.valueOf(mucosaCountG), PIECE, "106084"));

        //1 神经部面积占比	   %	Pars nervosa area%	1=A/H
        BigDecimal pituitaryA_H = getProportion(pituitaryA, pituitaryH);
        indicatorResultsMap.put("神经部面积占比", createNameIndicator("Pars nervosa area%", String.valueOf(pituitaryA_H.setScale(3, RoundingMode.HALF_UP)), PERCENTAGE, "10607F,106111"));
        //2 中间部面积占比	  %	   Pars intermedia area%	2=B/H
        BigDecimal pituitaryB_H = getProportion(pituitaryB, pituitaryH);
        indicatorResultsMap.put("中间部面积占比", createNameIndicator("Pars intermedia area%", String.valueOf(pituitaryB_H.setScale(3, RoundingMode.HALF_UP)), PERCENTAGE, "106081,106111"));

        //3 远侧部面积占比	 %	  Pars distalis area%	3=C/H
        BigDecimal pituitaryC_H = getProportion(pituitaryC, pituitaryH);
        indicatorResultsMap.put("远侧部面积占比", createNameIndicator("Pars distalis area%", String.valueOf(pituitaryC_H.setScale(3, RoundingMode.HALF_UP)), PERCENTAGE, "106083,106111"));

        //4 红细胞面积占比	 %	 Erythrocyte area%	4=D/H
        BigDecimal pituitaryD_H = getProportion(pituitaryD, pituitaryH);
        indicatorResultsMap.put("红细胞面积占比", createNameIndicator("Erythrocyte area%", String.valueOf(pituitaryD_H.setScale(3, RoundingMode.HALF_UP)), PERCENTAGE, "106004,106111"));

        //5 神经部细胞核密度 个/平方毫米	Nucleus density of pars nervosa	5=E/A
        BigDecimal pituitaryE_A = commonJsonParser.getProportionMultiply(new BigDecimal(mucosaCountE), pituitaryA);
        indicatorResultsMap.put("神经部细胞核密度", createNameIndicator("Erythrocyte area pars nervosa", String.valueOf(pituitaryE_A), SQ_MM_PIECE, "106080,10607F"));

        //6 中间部细胞核密度 个/平方毫米	Nucleus density of pars intermedia	6=F/B
        BigDecimal pituitaryF_B = commonJsonParser.getProportionMultiply(new BigDecimal(mucosaCountF), pituitaryB);
        indicatorResultsMap.put("中间部细胞核密度", createNameIndicator("Nucleus density of pars intermedi", String.valueOf(pituitaryF_B.setScale(3, RoundingMode.HALF_UP)), SQ_MM_PIECE, "106082,106081"));

        //7 远侧部细胞核密度个/平方毫米	Nucleus density of 7=G/C
        //BigDecimal pituitaryG_C = commonJsonParser.getProportionMultiply(new BigDecimal(mucosaCountG), pituitaryC);
        //indicatorResultsMap.put("远侧部细胞核密度", createNameIndicator("Nucleus density of pars distalis", String.valueOf(pituitaryG_C.setScale(3, RoundingMode.HALF_UP)), SQ_MM_PIECE, "106084,106083"));
        //8 垂体面积	 8=H
        indicatorResultsMap.put("垂体面积", createNameIndicator("Pituitary gland area", String.valueOf(pituitaryH.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "106111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Pituitary";
    }


    @Override
    public void getCustomOutLine(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal pituitaryH = new BigDecimal(singleSlide.getArea());
        indicatorResultsMap.put("垂体面积", createNameIndicator("Pituitary gland area", String.valueOf(pituitaryH.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "106111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
