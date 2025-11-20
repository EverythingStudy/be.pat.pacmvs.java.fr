package cn.staitech.fr.service.strategy.json.impl.digestive;

import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.service.strategy.json.OutlineCustom;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @ClassName: EsophagusParserStrategyImpl
 * @Description-d:食管
 * @date 2025年7月21日
 */
@Slf4j
@Component("Esophagus")
public class EsophagusParserStrategyImpl extends AbstractCustomParserStrategy implements OutlineCustom {

    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Autowired
    private AreaUtils areaUtils;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private CommonJsonCheck commonJsonCheck;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("EsophagusParserStrategyImpl init");
    }

    @Override
    public String getAlgorithmCode() {
        return "Esophagus";
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("食管结构指标面积计算开始：");
        //F 组织轮廓面积 10F111
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal bigDecimal = new BigDecimal(0);
        if (ObjectUtil.isNotEmpty(singleSlide) && StringUtils.isNotEmpty(singleSlide.getArea())) {
            bigDecimal = bigDecimal.add(new BigDecimal(singleSlide.getArea()));
        }
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        //A 食管腔面积 mm2
        BigDecimal area = areaUtils.getOrganArea(jsonTask, "10F120");
        //B 角质层面积 103 μm2
        BigDecimal organArea = commonJsonParser.getOrganAreaMicron(jsonTask, "10F12E");
        //C 颗粒层+棘层+基底层面积
        BigDecimal organArea1 = commonJsonParser.getOrganAreaMicron(jsonTask, "10F12F");
        //D 黏膜固有层+黏膜肌层+黏膜下层面积
        BigDecimal organArea2 = commonJsonParser.getOrganAreaMicron(jsonTask, "10F13B");
        //E 肌层面积
        BigDecimal organArea3 = commonJsonParser.getOrganArea(jsonTask, "10F00C").getStructureAreaNum();

        indicatorResultsMap.put("食管腔面积", new IndicatorAddIn("Esophageal cavity area", area.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1, "10F120"));
        indicatorResultsMap.put("角质层面积", new IndicatorAddIn("Area of stratum corneum", organArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "10F12E"));
        indicatorResultsMap.put("颗粒层+棘层+基底层面积", new IndicatorAddIn("Granular layer+spinous layer+basal layer", organArea1.setScale(3, RoundingMode.HALF_UP).toString(), SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "10F12F"));
        indicatorResultsMap.put("黏膜固有层+黏膜肌层+黏膜下层面积", new IndicatorAddIn("Mucosal lamina propria+mucosal muscle layer+submucosal submucosal area", organArea2.setScale(3, RoundingMode.HALF_UP).toString(), SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "10F13B"));
        indicatorResultsMap.put("肌层面积", new IndicatorAddIn("Muscle layer area", organArea3.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1, "10F00C"));
        //indicatorResultsMap.put("组织轮廓面积", new IndicatorAddIn("Organizational contour area", bigDecimal.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1, "10F111"));
        // F-A
        BigDecimal subtract = bigDecimal.subtract(area);
        if (subtract.signum() != 0) {
            //B/(F-A)
            indicatorResultsMap.put("角质层面积占比", new IndicatorAddIn("Stratum Corneum area%", getProportion(organArea, new BigDecimal(areaUtils.convertToSquareMicrometer(subtract.toString()))).toString(), PERCENTAGE, areaUtils.getStructureIds("10F12E", "10F111", "10F120")));
            //C/(F-A)
            indicatorResultsMap.put("颗粒层+棘层+基底层面积占比", new IndicatorAddIn("Nucleated cell layer area%", getProportion(organArea1, new BigDecimal(areaUtils.convertToSquareMicrometer(subtract.toString()))).toString(), PERCENTAGE, areaUtils.getStructureIds("10F12F", "10F111", "10F120")));
            //D/(F-A)
            indicatorResultsMap.put("黏膜固有层+黏膜肌层+黏膜下层面积占比", new IndicatorAddIn("Subepithelium area %", getProportion(organArea2, new BigDecimal(areaUtils.convertToSquareMicrometer(subtract.toString()))).toString(), PERCENTAGE, areaUtils.getStructureIds("10F13B", "10F111", "10F120")));
            //E/(F-A)
            indicatorResultsMap.put("肌层面积占比", new IndicatorAddIn("Muscularis area%", getProportion(organArea3, subtract).toString(), PERCENTAGE, areaUtils.getStructureIds("10F00C", "10F111", "10F120")));
        }
        //F-A
        indicatorResultsMap.put("食管面积", new IndicatorAddIn("Tissue contour area", subtract.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, areaUtils.getStructureIds("10F111", "10F120")));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public void getCustomOutLine(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal bigDecimal = new BigDecimal(singleSlide.getArea());
        //A食管腔面积
        BigDecimal area = areaUtils.getOrganArea(jsonTask, "10F120");
        indicatorResultsMap.put("食管面积", createNameIndicator("Tissue contour area", bigDecimal.subtract(area).setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, areaUtils.getStructureIds("10F111", "10F120")));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
