package cn.staitech.fr.service.strategy.json.impl.digestive;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;

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
 * @ClassName: StomachParserStrategyImpl
 * @Description-d:胃
 * @date 2025年7月21日
 */
@Slf4j
@Component("Stomach")
public class StomachParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonCheck commonJsonCheck;
    @Autowired
    private AreaUtils areaUtils;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("StomachParserStrategyImpl init");
    }

    @Override
    public String getAlgorithmCode() {
        return "Stomach";
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("大鼠胃结构指标计算：");
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        //A前胃肌层面积
        BigDecimal organArea = commonJsonParser.getOrganArea(jsonTask, "110023").getStructureAreaNum();
        //B前胃角质层
        BigDecimal organArea1 = commonJsonParser.getOrganAreaMicron(jsonTask, "11012E");
        //C前胃有核层面积
        BigDecimal organArea3 = commonJsonParser.getOrganAreaMicron(jsonTask, "11012F");
        //D腺胃黏膜上皮+固有层面积
        BigDecimal organArea2 = commonJsonParser.getOrganArea(jsonTask, "110035").getStructureAreaNum();
        //E前胃面积
        BigDecimal organArea5 = commonJsonParser.getOrganArea(jsonTask, "11013F").getStructureAreaNum();
        //F腺胃面积
        BigDecimal organArea6 = commonJsonParser.getOrganArea(jsonTask, "110140").getStructureAreaNum();
        //G腺胃肌层
        BigDecimal organArea4 = commonJsonParser.getOrganArea(jsonTask, "110024").getStructureAreaNum();

        indicatorResultsMap.put("前胃肌层面积", new IndicatorAddIn("", organArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1, "110023"));
        indicatorResultsMap.put("前胃角质层面积", new IndicatorAddIn("", areaUtils.convertToSquareMicrometer(organArea1.toString()), SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "11012E"));
        indicatorResultsMap.put("前胃有核层面积", new IndicatorAddIn("", areaUtils.convertToSquareMicrometer(organArea3.toString()), SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "11012F"));
        indicatorResultsMap.put("腺胃黏膜上皮+固有层面积", new IndicatorAddIn("", organArea2.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1, "110035"));
        indicatorResultsMap.put("前胃面积", new IndicatorAddIn("", organArea5.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1, "11013F"));
        indicatorResultsMap.put("腺胃面积", new IndicatorAddIn("", organArea6.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1, "110140"));
        indicatorResultsMap.put("腺胃肌层面积", new IndicatorAddIn("", organArea4.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1, "110024"));
        if (organArea5.signum() == 0) {
            indicatorResultsMap.put("前胃肌层面积占比", new IndicatorAddIn("Forestomach Muscularis area%", "0", PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("110023", "11013F")));
            indicatorResultsMap.put("前胃角质层面积占比", new IndicatorAddIn("Stratum Corneum  area%", "0", PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("11012E", "11013F")));
            indicatorResultsMap.put("前胃有核层面积占比", new IndicatorAddIn("Nucleated cell laye area%", "0", PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("11012F", "11013F")));
        } else {
            indicatorResultsMap.put("前胃肌层面积占比", new IndicatorAddIn("Forestomach Muscularis area%", getProportion(organArea, organArea5).toString(), PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("110023", "11013F")));
            indicatorResultsMap.put("前胃角质层面积占比", new IndicatorAddIn("Stratum Corneum  area%", getProportion(organArea1, organArea5).toString(), PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("11012E", "11013F")));
            indicatorResultsMap.put("前胃有核层面积占比", new IndicatorAddIn("Nucleated cell laye area%", getProportion(organArea3, organArea5).toString(), PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("11012F", "11013F")));
        }
        if (organArea6.signum() == 0) {
            indicatorResultsMap.put("腺胃肌层面积占比", new IndicatorAddIn("Glandular Muscularis area%", "0", PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("110024", "110140")));
            indicatorResultsMap.put("腺胃黏膜上皮+固有层面积占比", new IndicatorAddIn("Glandular area%", "0", PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("110035", "110140")));
        } else {
            indicatorResultsMap.put("腺胃肌层面积占比", new IndicatorAddIn("Glandular Muscularis area%", getProportion(organArea4, organArea6).toString(), PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("110024", "110140")));
            indicatorResultsMap.put("腺胃黏膜上皮+固有层面积占比", new IndicatorAddIn("Glandular area%", getProportion(organArea2, organArea6).toString(), PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("110035", "110140")));

        }
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
