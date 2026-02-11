package cn.staitech.fr.service.strategy.json.impl.dog.urology;

import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
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
 * 
* @ClassName: KidneyParserStrategyImpl
* @Description:犬-肾脏
* @author wanglibei
* @date 2026年2月11日
* @version V1.0
 */
@Slf4j
@Service("Kidney_3")
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

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        //Project special = projectMapper.selectById(jsonTask.getSpecialId());
        //String controlGroup = StringUtils.isNotEmpty(special.getControlGroup()) ? special.getControlGroup() : DEFAULT_CONTROL_GROUP_VALUE;
        //Integer countCa = singleSlideMapper.getCategoryIdCountByGroupCode(jsonTask.getCategoryId(), jsonTask.getSingleId(), controlGroup);
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        //皮质
        Annotation a11B03D = getOrganArea(jsonTask, "11B03D");
        BigDecimal b11B03D = a11B03D.getStructureAreaNum();
        BigDecimal b11B111 = new BigDecimal(0);
        if (ObjectUtil.isNotEmpty(singleSlide) && StringUtils.isNotEmpty(singleSlide.getArea())) {
        	b11B111 = b11B111.add(new BigDecimal(singleSlide.getArea()));
        }
//         BigDecimal b11B111 = new BigDecimal(singleSlide.getArea());
        //组织轮廓-肾脏面积
//        BigDecimal b11B111 = getOrganArea(jsonTask, "11B111").getStructureAreaNum();
        //肾小管数量
        Integer count = getOrganAreaCount(jsonTask, "11B031");
        //一级指标（算法输出指标）
        //A mm2 11B03D
         indicatorResultsMap.put("肾皮质面积", createIndicator(b11B03D.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "11B03D"));
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
        indicatorResultsMap.put("髓质面积占比", createNameIndicator("Medulla area%", String.valueOf(getProportion(b11B111.subtract(b11B03D), b11B111)), PERCENTAGE, "11B111,11B03D"));
        //肾小球面积（单个）3=B
        List<Annotation> bs = getStructureContourList(jsonTask, "11B02D");
        List<BigDecimal> bsb = bs.stream().map(annotation -> {
            BigDecimal temp = annotation.getStructureAreaNum();
            return temp.multiply(BigDecimal.valueOf(1000));
        }).collect(Collectors.toList());
        indicatorResultsMap.put("肾小球面积（单个）", createNameIndicator("Acinar epithelial area% (per)", MathUtils.getConfidenceInterval(bsb), SQ_UM_THOUSAND, "11B02D"));
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
