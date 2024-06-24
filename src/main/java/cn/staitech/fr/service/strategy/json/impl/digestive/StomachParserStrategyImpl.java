package cn.staitech.fr.service.strategy.json.impl.digestive;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author wudi
 * @Date 2024/5/16 15:19
 * @desc 楚雨xun
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
        //D腺胃黏膜上皮+固有层面积
        BigDecimal organArea2 = commonJsonParser.getOrganArea(jsonTask, "110035").getStructureAreaNum();
        //c前胃有核层面积
        BigDecimal organArea3 = commonJsonParser.getOrganAreaMicron(jsonTask, "11012F");
        //e前胃面积
        BigDecimal organArea5 = commonJsonParser.getOrganArea(jsonTask, "11013F").getStructureAreaNum();
        //f腺胃面积
        BigDecimal organArea6 = commonJsonParser.getOrganArea(jsonTask, "110140").getStructureAreaNum();
        //G腺胃肌层
        BigDecimal organArea4 = commonJsonParser.getOrganArea(jsonTask, "110024").getStructureAreaNum();


        indicatorResultsMap.put("前胃肌层面积", new IndicatorAddIn("Forestomach Muscularis area", organArea.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("前胃角质层面积", new IndicatorAddIn("Stratum Corneum area", organArea1.setScale(3, RoundingMode.HALF_UP).toString(), "10³平方微米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("腺胃黏膜上皮+固有层面积", new IndicatorAddIn("Glandular area", organArea2.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("前胃有核层面积", new IndicatorAddIn("Nucleated cell laye area", organArea3.setScale(3, RoundingMode.HALF_UP).toString(), "10³平方微米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("腺胃肌层面积", new IndicatorAddIn("Glandular Muscularis area", organArea4.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        if(organArea5.signum() == 0){
            indicatorResultsMap.put("前胃肌层面积占比", new IndicatorAddIn("Forestomach Muscularis area%","0.000", "%"));
            indicatorResultsMap.put("前胃角质层面积占比", new IndicatorAddIn("Stratum Corneum  area%","0.000", "%"));
            indicatorResultsMap.put("前胃有核层面积占比", new IndicatorAddIn("Nucleated cell laye area%","0.000", "%"));

        }else{
            indicatorResultsMap.put("前胃肌层面积占比", new IndicatorAddIn("Forestomach Muscularis area%",organArea.divide(organArea5,5,RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(3).toString(), "%"));
            BigDecimal multiply = organArea5.multiply(new BigDecimal("1000"));
            indicatorResultsMap.put("前胃角质层面积占比", new IndicatorAddIn("Stratum Corneum  area%",organArea1.divide(multiply,5,RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(3).toString(), "%"));
            indicatorResultsMap.put("前胃有核层面积占比", new IndicatorAddIn("Nucleated cell laye area%",organArea3.divide(multiply,5,RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(3).toString(), "%"));

        }
        if(organArea6.signum() == 0){
            indicatorResultsMap.put("腺胃肌层面积占比", new IndicatorAddIn("Glandular Muscularis area%","0.000", "%"));
            indicatorResultsMap.put("腺胃黏膜上皮+固有层面积占比", new IndicatorAddIn("Glandular area%","0.000", "%"));

        }else{
            indicatorResultsMap.put("腺胃肌层面积占比", new IndicatorAddIn("Glandular Muscularis area%",organArea4.divide(organArea6,5,RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(3).toString(), "%"));
            indicatorResultsMap.put("腺胃黏膜上皮+固有层面积占比", new IndicatorAddIn("Glandular area%",organArea2.divide(organArea6,5,RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(3).toString(), "%"));

        }
        indicatorResultsMap.put("前胃面积", new IndicatorAddIn("Forestomach area",organArea5.setScale(3,RoundingMode.HALF_UP).toString(), "平方毫米"));
        indicatorResultsMap.put("腺胃面积", new IndicatorAddIn("Glandular area",organArea6.setScale(3,RoundingMode.HALF_UP).toString(), "平方毫米"));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
        //aiForecastService.addOutIndicators(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
