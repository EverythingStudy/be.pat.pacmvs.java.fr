package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
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
 * @author wanglibei
 * @version V1.0
 * @ClassName: PituitaryParserStrategyImpl
 * @Description:大鼠-内分泌系统-垂体-7I
 * @date 2024年5月13日
 */
@Slf4j
@Component("Pituitary")
public class PituitaryParserStrategyImpl extends AbstractCustomParserStrategy {
    @Resource
    private AreaUtils areaUtils;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private CommonJsonCheck commonJsonCheck;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("PituitaryParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {

        log.info("大鼠垂体构指标计算开始");

        // 神经部: 10607F
        // 神经部细胞核（胶质细胞）: 106080
        // 中间部: 106081
        // 中间部细胞核:（嫌色细胞或嗜碱性细胞） 106082
        // 远侧部: 106083
        // 远侧部细胞核（嗜酸性细胞、嗜碱性细胞、嫌色细胞）: 106084
        // 红细胞: 106004
        // 组织轮廓 :106111

        // 神经部面积 A 平方毫米 若多个数据则相加输出
        BigDecimal pituitaryA = getOrganArea(jsonTask, "10607F").getStructureAreaNum();
        pituitaryA = pituitaryA.setScale(3, RoundingMode.HALF_UP);
        pituitaryA = commonJsonParser.getBigDecimalValue(pituitaryA);
        // 中间部面积 B 平方毫米 若多个数据则相加输出
        BigDecimal pituitaryB = getOrganArea(jsonTask, "106081").getStructureAreaNum();
        pituitaryB = pituitaryB.setScale(3, RoundingMode.HALF_UP);
        pituitaryB = commonJsonParser.getBigDecimalValue(pituitaryB);
        // 远侧部面积 C 平方毫米 若多个数据则相加输出
        BigDecimal pituitaryC = getOrganArea(jsonTask, "106083").getStructureAreaNum();
        pituitaryC = pituitaryC.setScale(3, RoundingMode.HALF_UP);
        pituitaryC = commonJsonParser.getBigDecimalValue(pituitaryC);
        // 红细胞面积 D 平方毫米 数据相加输出
        BigDecimal pituitaryD = getOrganArea(jsonTask, "106004").getStructureAreaNum();
        pituitaryD = pituitaryD.setScale(3, RoundingMode.HALF_UP);
        pituitaryD = commonJsonParser.getBigDecimalValue(pituitaryD);
        // 胸骨面积 ==>组织轮廓面积H
        BigDecimal pituitaryH = getOrganArea(jsonTask, "106111").getStructureAreaNum();
        pituitaryH = pituitaryH.setScale(3, RoundingMode.HALF_UP);
        pituitaryH = commonJsonParser.getBigDecimalValue(pituitaryH);
//		String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());
//		BigDecimal pituitaryH = BigDecimal.ZERO;
//		pituitaryH = new BigDecimal(slideArea);
        // 神经部细胞核数量 E 个 无
        Integer mucosaCountE = getOrganAreaCount(jsonTask, "106080");
        // 中间部细胞核数量 F 个 无
        Integer mucosaCountF = getOrganAreaCount(jsonTask, "106082");
        // 远侧部细胞核数量 G 个 无
        Integer mucosaCountG = getOrganAreaCount(jsonTask, "106084");

        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();

//        indicatorResultsMap.put("神经部面积", createIndicator(String.valueOf(pituitaryA.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "10607F"));
//        indicatorResultsMap.put("中间部面积", createIndicator(String.valueOf(pituitaryB.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "106081"));
//        indicatorResultsMap.put("远侧部面积", createIndicator(String.valueOf(pituitaryC.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "106083"));
//        indicatorResultsMap.put("红细胞面积", createIndicator(String.valueOf(pituitaryD.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "106004"));
//        indicatorResultsMap.put("神经部细胞核数量", createIndicator(String.valueOf(mucosaCountE), PIECE, "106080"));
//        indicatorResultsMap.put("中间部细胞核数量", createIndicator(String.valueOf(mucosaCountF), PIECE, "106082"));
//        indicatorResultsMap.put("远侧部细胞核数量", createIndicator(String.valueOf(mucosaCountG), PIECE, "106084"));

        //神经部面积占比	1	%	Pars nervosa area%	1=A/H
        BigDecimal pituitaryA_H = getProportion(pituitaryA, pituitaryH);
        //indicatorResultsMap.put("神经部面积占比", createNameIndicator("Pars nervosa area%", String.valueOf(pituitaryA_H.setScale(3, RoundingMode.HALF_UP)), PERCENTAGE, "10607F,106111"));
        //				中间部面积占比	2	%	Pars intermedia area%	2=B/H
        BigDecimal pituitaryB_H = getProportion(pituitaryB, pituitaryH);
        //indicatorResultsMap.put("中间部面积占比", createNameIndicator("Pars intermedia area%", String.valueOf(pituitaryB_H.setScale(3, RoundingMode.HALF_UP)), PERCENTAGE, "106081,106111"));

        //远侧部面积占比	3	%	Pars distalis area%	3=C/H
        BigDecimal pituitaryC_H = getProportion(pituitaryC, pituitaryH);
        //indicatorResultsMap.put("远侧部面积占比", createNameIndicator("Pars distalis area%", String.valueOf(pituitaryC_H.setScale(3, RoundingMode.HALF_UP)), PERCENTAGE, "106083,106111"));

        //红细胞面积占比	4	%	Erythrocyte area%	4=D/H
        BigDecimal pituitaryD_H = getProportion(pituitaryD, pituitaryH);
        //indicatorResultsMap.put("红细胞面积占比", createNameIndicator("Erythrocyte area%", String.valueOf(pituitaryD_H.setScale(3, RoundingMode.HALF_UP)), PERCENTAGE, "106004,106111"));

        //神经部细胞核密度	5	个/平方毫米	Nucleus density of pars nervosa	5=E/A
        BigDecimal pituitaryE_A = commonJsonParser.getProportionMultiply(new BigDecimal(mucosaCountE), pituitaryA);
        //indicatorResultsMap.put("神经部细胞核密度", createNameIndicator("Erythrocyte area pars nervosa", String.valueOf(pituitaryE_A), SQ_MM_PIECE, "106080,10607F"));

        //中间部细胞核密度	6	个/平方毫米	Nucleus density of pars intermedia	6=F/B
        BigDecimal pituitaryF_B = commonJsonParser.getProportionMultiply(new BigDecimal(mucosaCountF), pituitaryB);
        //indicatorResultsMap.put("中间部细胞核密度", createNameIndicator("Nucleus density of pars intermedi", String.valueOf(pituitaryF_B.setScale(3, RoundingMode.HALF_UP)), SQ_MM_PIECE, "106082,106081"));

        //远侧部细胞核密度	7	个/平方毫米	Nucleus density of 7=G/C
        BigDecimal pituitaryG_C = commonJsonParser.getProportionMultiply(new BigDecimal(mucosaCountG), pituitaryC);
        //indicatorResultsMap.put("远侧部细胞核密度", createNameIndicator("Nucleus density of pars distalis", String.valueOf(pituitaryG_C.setScale(3, RoundingMode.HALF_UP)), SQ_MM_PIECE, "106084,106083"));

        indicatorResultsMap.put("垂体面积", createNameIndicator("Pituitary gland area", String.valueOf(pituitaryH.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "106111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Pituitary";
    }
}
