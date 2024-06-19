package cn.staitech.fr.service.strategy.json.impl.digestive;


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
 * @Date 2024/5/16 15:55
 * @desc 喉
 */
@Slf4j
@Component("Larynx")
public class LarynxParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
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
        log.info("LarynxParserStrategyImpl init");
    }

    @Override
    public String getAlgorithmCode() {
        return "Larynx";
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("大鼠喉结构指标面积开始：");

        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        //a
        BigDecimal organArea = commonJsonParser.getOrganArea(jsonTask, "10E035").getStructureAreaNum();
        //b
        BigDecimal organArea1 = commonJsonParser.getOrganArea(jsonTask, "10E133").getStructureAreaNum();
        //c
        BigDecimal bigDecimal = new BigDecimal(singleSlide.getArea());
        indicatorResultsMap.put("喉面积", new IndicatorAddIn("Larynx area", singleSlide.getArea(), "平方毫米"));
        indicatorResultsMap.put("黏膜上皮面积", new IndicatorAddIn("Muscular layer", organArea.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("腺体面积", new IndicatorAddIn("Glandular area", organArea1.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        if(bigDecimal.compareTo(BigDecimal.ZERO)== 0){
            indicatorResultsMap.put("黏膜上皮面积占比", new IndicatorAddIn("Mucous epithelium area%", "0.000", "%"));
            indicatorResultsMap.put("腺体面积占比", new IndicatorAddIn("Gland area%", "0.000", "%"));
        }else{
            indicatorResultsMap.put("黏膜上皮面积占比", new IndicatorAddIn("Mucous epithelium area%", organArea.divide(bigDecimal,3, RoundingMode.HALF_UP).toString(), "%"));
            indicatorResultsMap.put("腺体面积占比", new IndicatorAddIn("Gland area%", organArea1.divide(bigDecimal,3, RoundingMode.HALF_UP).toString(), "%"));

        }
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
        //aiForecastService.addOutIndicators(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
