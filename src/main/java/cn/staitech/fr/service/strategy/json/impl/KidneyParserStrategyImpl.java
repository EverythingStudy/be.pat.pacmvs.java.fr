package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.Project;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.ProjectMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.MathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author chenly
 * @version 1.0
 * @description 大鼠-肾脏
 * @date 2024/5/13 10:06:53
 */
@Slf4j
@Service("Kidney")
public class KidneyParserStrategyImpl extends AbstractCustomParserStrategy {
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private CommonJsonCheck commonJsonCheck;
    @Resource
    private ProjectMapper projectMapper;
    //默认对照组值
    private static final String DEFAULT_CONTROL_GROUP_VALUE = "1";

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.debug("KidneyParserStrategyImpl init");
    }

    /**
     * 结构	编码
     * 髓质	11B03E
     * 肾小球	11B02D
     * 球内细胞核	11B02E
     * 球内红细胞	11B02F
     * 肾小管	11B031
     * 组织轮廓	11B111
     * 算法输出指标	指标代码（仅限本文档）	单位（保留小数点后三位）	备注
     * 肾皮质面积	A	平方毫米	无
     * 肾小球面积（单个）	B	103平方微米	无
     * 球内细胞核数量（单个）	C	个	单个=单个肾小球
     * 球内红细胞面积（单个）	D	103平方微米	单个=单个肾小球
     * 肾小管数量	E	个	无
     * 肾小管面积（单个）	F	103平方微米	无
     * 组织轮廓面积	G	平方毫米	无
     * <p>
     * 产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后三位）	English	计算方式	备注
     * 肾脏面积	1	平方毫米	 Renal area	1=G
     * 髓质面积占比	2	%	Medulla area%	2=(G-A)/G
     * 肾小球面积（单个）	3	103平方微米	Glomerulus area (per)	3=B	以95%置信区间和均数±标准差呈现
     * 球内细胞核密度（单个）	4	个/平方毫米	Nucleus density of glomerulus (per)	4=C/B	单个为单个肾小球 以95%置信区间和均数±标准差呈现
     * 球内红细胞面积占比（单个）	5	个/平方毫米	Erythrocyte of glomerulus area% (per)	5=D/B	单个为单个肾小球 以95%置信区间和均数±标准差呈现
     * 皮质肾小管密度	6	个/平方毫米	 Density of renal cortical tubules	6=E/A
     * 肾小管面积(单个)	7	103平方微米	Renal tubule area (per)	7=F	以95%置信区间和均数±标准差呈现
     *
     * @param jsonTask
     */
    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Project special = projectMapper.selectById(jsonTask.getSpecialId());
        //String controlGroup = StringUtils.isNotEmpty(special.getControlGroup()) ? special.getControlGroup() : DEFAULT_CONTROL_GROUP_VALUE;
        //Integer countCa = singleSlideMapper.getCategoryIdCountByGroupCode(jsonTask.getCategoryId(), jsonTask.getSingleId(), controlGroup);
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        //皮质
        Annotation a11B03D = getOrganArea(jsonTask, "11B03D");
        BigDecimal b11B03D = a11B03D.getStructureAreaNum();
        // BigDecimal b11B111 = new BigDecimal(singleSlide.getArea());
        //组织轮廓-肾脏面积
        BigDecimal b11B111 = getOrganArea(jsonTask, "11B111").getStructureAreaNum();
        //肾小管数量
        Integer count = getOrganAreaCount(jsonTask, "11B031");
        //一级指标（算法输出指标）
        //A mm2 11B03D
        // indicatorResultsMap.put("肾皮质面积", createIndicator(b11B03D.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "11B03D"));
        //B、103平方微米 11B02D
        //indicatorResultsMap.put("肾小球面积（单个）", createDefaultIndicator("11B02D"));
        //C、个 11B02D、11B02E
        //indicatorResultsMap.put("球内细胞核数量（单个）", createDefaultIndicator("11B02D,11B02E"));
        //D、103平方微米 11B02D、11B02F
        //indicatorResultsMap.put("球内红细胞面积（单个）", createDefaultIndicator("11B02D,11B02F"));
        //E、个 11B031
        //indicatorResultsMap.put("肾小管数量", createIndicator(String.valueOf(count), "个", "11B031"));
        //F、103平方微米 11B031
        //indicatorResultsMap.put("肾小管面积（单个）", createDefaultIndicator("11B031"));
        //G、平方毫米 11B111
        //indicatorResultsMap.put("组织轮廓面积", createIndicator(new BigDecimal(singleSlide.getArea()).setScale(3, RoundingMode.DOWN).toString(), SQ_MM, "11B111"));
        //1=G
        indicatorResultsMap.put("肾脏面积", createNameIndicator("", new BigDecimal(singleSlide.getArea()).setScale(3, RoundingMode.DOWN).toString(), SQ_MM, "11B111"));
        //2=(G-A)/G
        //indicatorResultsMap.put("髓质面积占比", createNameIndicator("Medulla area%", String.valueOf(getProportion(b11B111.subtract(b11B03D), b11B111)), PERCENTAGE, "11B111,11B03D"));
        //肾小球面积（单个）3=B
        List<Annotation> bs = getStructureContourList(jsonTask, "11B02D");
        List<BigDecimal> bsb = bs.stream().map(annotation -> {
            BigDecimal temp = annotation.getStructureAreaNum();
            return temp.multiply(BigDecimal.valueOf(1000));
        }).collect(Collectors.toList());
        indicatorResultsMap.put("肾小球面积（单个）", createNameIndicator("Acinar epithelial area% (per)", MathUtils.getConfidenceInterval(bsb, bsb.size()), SQ_UM_THOUSAND, "11B02D"));
        //4=C/B
        List<BigDecimal> cb = new ArrayList<>();
        for (Annotation annotation : bs) {
            Annotation temp = getContourInsideOrOutside(jsonTask, annotation.getContour(), "11B02E", true);
            Integer c = temp.getCount();
            if (c != null && c > 0) {
                BigDecimal temp1 = BigDecimal.valueOf(c);
                BigDecimal result = commonJsonParser.bigDecimalDivideCheck(temp1, annotation.getStructureAreaNum());
                cb.add(result);
            }
        }
        // indicatorResultsMap.put("球内细胞核密度（单个）", createComplexIndicator(cb, "Nucleus density of glomerulus (per)", SQ_MM_PIECE, CommonConstant.NUMBER_0, "11B02D,11B02E"));
        //5=D/B
        List<BigDecimal> db = bs.stream().map(annotation -> {
            Annotation temp = getContourInsideOrOutside(jsonTask, annotation.getContour(), "11B02F", true);
            BigDecimal c = temp.getStructureAreaNum();
            BigDecimal result = BigDecimal.valueOf(0);
            if (c != null) {
                result = commonJsonParser.bigDecimalDivideCheck(c, annotation.getStructureAreaNum());
            }
            return result;
        }).collect(Collectors.toList());
        //indicatorResultsMap.put("球内红细胞面积占比（单个）", createComplexIndicator(db, "Erythrocyte of glomerulus area% (per)", PERCENTAGE, CommonConstant.NUMBER_0, "11B02D,11B02F"));
        //6=E/A
        BigDecimal b1 = BigDecimal.ZERO;
        if (count != 0 && b11B03D.compareTo(BigDecimal.ZERO) != 0) {
            b1 = commonJsonParser.bigDecimalDivideCheck(BigDecimal.valueOf(count), b11B03D);
        }
        //indicatorResultsMap.put("皮质肾小管密度", createNameIndicator("Density of renal cortical tubules", String.valueOf(b1), SQ_MM_PIECE, "11B031,11B03D"));
        //7=F
        List<Annotation> f = getStructureContourList(jsonTask, "11B031");
        if (CollectionUtils.isNotEmpty(f)) {
            List<BigDecimal> fb = f.stream().map(annotation -> {
                BigDecimal temp = annotation.getStructureAreaNum();
                return temp.multiply(BigDecimal.valueOf(1000));
            }).collect(Collectors.toList());
            //indicatorResultsMap.put("肾小管面积(单个)", createComplexIndicator(fb, "Renal tubule area (per)", SQ_UM_THOUSAND, CommonConstant.NUMBER_0, "11B031"));
        }
        Annotation annotationBy = new Annotation();
//        annotationBy.setCountName("球内细胞核数量（单个）");
//        commonJsonParser.putAnnotationDynamicData(jsonTask, "11B02D", "11B02E", annotationBy);
//        annotationBy.setCountName(null);
//
//
//        annotationBy.setAreaName("球内红细胞面积（单个）");
//        annotationBy.setAreaUnit(SQ_UM_THOUSAND);
//        commonJsonParser.putAnnotationDynamicData(jsonTask, "11B02D", "11B02F", annotationBy, 1);
//        annotationBy.setCountName(null);
        annotationBy.setAreaName("肾小球面积（单个）");
        annotationBy.setAreaUnit(SQ_UM_THOUSAND);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "11B02D", annotationBy, 1);
//        annotationBy.setCountName(null);
//        annotationBy.setAreaName("肾小管面积（单个）");
//        annotationBy.setAreaUnit(SQ_UM_THOUSAND);
//        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "11B031", annotationBy, 1);
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Kidney";
    }
}
