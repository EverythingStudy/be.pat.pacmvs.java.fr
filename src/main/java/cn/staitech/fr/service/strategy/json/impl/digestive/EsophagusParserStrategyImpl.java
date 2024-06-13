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
 * @Author wudi
 * @Date 2024/5/16 15:26
 * @desc 食管
 */
@Slf4j
@Component("Esophagus")
public class EsophagusParserStrategyImpl extends AbstractCustomParserStrategy {

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
        log.info("食管结构指标米面积计算开始：");
        //组织轮廓面积
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal bigDecimal = new BigDecimal(0);
        if(ObjectUtil.isNotEmpty(singleSlide)&& StringUtils.isNotEmpty(singleSlide.getArea())){
            bigDecimal = bigDecimal.add(new BigDecimal(singleSlide.getArea()));
        }
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        BigDecimal area = areaUtils.getOrganArea(jsonTask, "10F120");
        BigDecimal organArea = commonJsonParser.getOrganAreaMicron(jsonTask, "10F12E");
        BigDecimal organArea1 = commonJsonParser.getOrganAreaMicron(jsonTask, "10F12F");
        BigDecimal organArea2 = commonJsonParser.getOrganAreaMicron(jsonTask, "10F13B");
        BigDecimal organArea3 = commonJsonParser.getOrganArea(jsonTask, "10F00C").getStructureAreaNum();

        indicatorResultsMap.put("食管面积", new IndicatorAddIn("Tissue contour area", bigDecimal.subtract(area).setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米"));
        indicatorResultsMap.put("食管腔面积", new IndicatorAddIn("Esophageal cavity area", area.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("角质层面积", new IndicatorAddIn("Area of stratum corneum", organArea.setScale(3, RoundingMode.HALF_UP).toString(), "10³平方微米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("颗粒层+棘层+基底层面积", new IndicatorAddIn("Granular layer+spinous layer+basal layer", organArea1.setScale(3, RoundingMode.HALF_UP).toString(), "10³平方微米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("黏膜固有层+黏膜肌层+黏膜下层面积", new IndicatorAddIn("Mucosal lamina propria+mucosal muscle layer+submucosal submucosal area", organArea2.setScale(3, RoundingMode.HALF_UP).toString(), "10³平方微米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("肌层面积", new IndicatorAddIn("Muscle layer area", organArea3.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("组织轮廓面积", new IndicatorAddIn("Organizational contour area", singleSlide.getArea(), "平方毫米", CommonConstant.NUMBER_1));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
        //aiForecastService.addOutIndicators(jsonTask.getSingleId(), indicatorResultsMap);

    }
}
