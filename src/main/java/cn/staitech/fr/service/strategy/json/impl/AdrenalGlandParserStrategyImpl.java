package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chenly
 * @version 1.0
 * @description -u 大鼠-内分泌系统-肾上腺
 * @date 2024/5/13 10:06:53
 */
@Slf4j
@Service("AdrenalGland")
public class AdrenalGlandParserStrategyImpl extends AbstractCustomParserStrategy {
    @Resource
    private SingleSlideMapper singleSlideMapper;
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
        log.debug("AdrenalGlandParserStrategyImpl init");
    }

    /**
     * 皮质	10103D
     * 髓质	10103E
     * 肾上腺实质细胞核	101068
     * 红细胞	101004
     * 组织轮廓	101111
     * 算法输出指标	指标代码（仅限本文档）	单位（保留小数点后三位）	备注
     * 皮质面积	A	平方毫米
     * 髓质面积	B	平方毫米
     * 皮质细胞核数量	C	个
     * 髓质细胞核数量	D	个
     * 红细胞面积（全片）	E	平方毫米
     * 组织轮廓面积	F	平方毫米
     * <p>
     * <p>
     * 产品呈现指标	指标代码（仅限本文档）	单位(保留小数点后三位)	English	计算方式	备注
     * 皮质面积占比	1	%	Cortex area %	1=A/F
     * 髓质面积占比	2	%	Medulla area%	2=B/F
     * 皮髓比	3	%	Cortex:Medulla ratio	3=A/B
     * 皮质细胞核密度	4	个/平方毫米	Nucleus density of adrenal cortex	4=C/A
     * 髓质细胞核密度	5	个/平方毫米	Nucleus density of adrenal medulla	5=D/B
     * 红细胞面积占比	6	%	Erythrocyte area%	6=E/F
     * 肾上腺面积	7	平方毫米	Adrenal gland area	7=F
     */

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal organArea = getOrganArea(jsonTask, "10103D").getStructureAreaNum();
        BigDecimal organArea1 = getOrganArea(jsonTask, "101004").getStructureAreaNum();
        BigDecimal organArea2 = getOrganArea(jsonTask, "10103E").getStructureAreaNum();
        Integer C = getInsideOrOutside(jsonTask, "10103D", "101068", true).getCount();
        Integer D = getInsideOrOutside(jsonTask, "10103E", "101068", true).getCount();
       // BigDecimal F = getOrganArea(jsonTask, "101111").getStructureAreaNum();
        //        组织轮廓面积	H	平方毫米	若多个数据则相加输出 (H:精细轮廓总面积（肝脏面积）-平方毫米)
        String accurateArea = singleSlide.getArea();
        BigDecimal F = new BigDecimal(accurateArea);
        indicatorResultsMap.put("皮质面积", createIndicator(organArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "10103D"));
        indicatorResultsMap.put("髓质面积", createIndicator(organArea2.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "10103E"));
       // indicatorResultsMap.put("皮质细胞核数量", createIndicator(String.valueOf(C), PIECE, "10103D,101068"));
        //indicatorResultsMap.put("髓质细胞核数量", createIndicator(String.valueOf(D), PIECE, "10103E,101068"));
        indicatorResultsMap.put("红细胞面积", createIndicator(organArea1.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "101004"));

        //皮质面积占比 % Cortex area %  1=A/F
        BigDecimal b1 = getProportion(organArea, F);
        indicatorResultsMap.put("皮质面积占比", createNameIndicator("Cortex area %", String.valueOf(b1), PERCENTAGE, "10103D,101111"));
        //2=B/F %
        BigDecimal b2 = commonJsonParser.getProportion(organArea2, F);
        indicatorResultsMap.put("髓质面积占比", createNameIndicator("Medulla area%", String.valueOf(b2), PERCENTAGE, "10103E,101111"));
        BigDecimal b3 = getProportion(organArea, organArea2);
        indicatorResultsMap.put("皮髓比", createNameIndicator("Cortex:Medulla ratio", String.valueOf(b3), "无", "10103D,10103E"));
        //4=C/A 个/mm2
        //BigDecimal b4 = bigDecimalDivideCheck(new BigDecimal(C), organArea);
        //indicatorResultsMap.put("皮质细胞核密度", createNameIndicator("Nucleus density of adrenal cortex", String.valueOf(b4), SQ_MM_PIECE, "10103D,101068"));
        //5=D/B 个/mm2
        //BigDecimal b5 = bigDecimalDivideCheck(new BigDecimal(D), organArea2);
       // indicatorResultsMap.put("髓质细胞核密度", createNameIndicator("Nucleus density of adrenal medulla", String.valueOf(b5), SQ_MM_PIECE, "10103E,101068"));
        //6=E/F %
        BigDecimal b6 = getProportion(organArea1, F);
        indicatorResultsMap.put("红细胞面积占比", createNameIndicator("Erythrocyte area%", String.valueOf(b6), PERCENTAGE, "101004,101111"));
        //7=F  mm2
        indicatorResultsMap.put("肾上腺面积", createNameIndicator("Adrenal gland area%", F.setScale(3, RoundingMode.DOWN).toString(), SQ_MM, "101111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Adrenal_gland";
    }
}
