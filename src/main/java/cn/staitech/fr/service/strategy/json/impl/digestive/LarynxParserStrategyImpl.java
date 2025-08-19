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
 * 
* @ClassName: LarynxParserStrategyImpl
* @Description-d:喉
* @author wanglibei
* @date 2025年7月21日
* @version V1.0
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
        //c 10E111
        BigDecimal bigDecimal = new BigDecimal(singleSlide.getArea());
        //c 10E111
        indicatorResultsMap.put("喉面积", new IndicatorAddIn("Larynx area", new BigDecimal(singleSlide.getArea()).setScale(3,RoundingMode.HALF_UP).toString(), "平方毫米","10E111"));
        //a 10E035
        indicatorResultsMap.put("黏膜上皮面积", new IndicatorAddIn("Muscular layer", organArea.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1,"10E035"));
        //b
        indicatorResultsMap.put("腺体面积", new IndicatorAddIn("Glandular area", organArea1.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1,"10E133"));
        if(bigDecimal.signum() == 0){
        	//A/C
            indicatorResultsMap.put("黏膜上皮面积占比", new IndicatorAddIn("Mucous epithelium area%", "0", "%",areaUtils.getStructureIds("10E035","10E111")));
            //B/C
            indicatorResultsMap.put("腺体面积占比", new IndicatorAddIn("Gland area%", "0", "%",areaUtils.getStructureIds("10E133","10E111")));
        }else{
            indicatorResultsMap.put("黏膜上皮面积占比", new IndicatorAddIn("Mucous epithelium area%", organArea.divide(bigDecimal,5, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(3).toString(), "%",areaUtils.getStructureIds("10E035","10E111")));
            indicatorResultsMap.put("腺体面积占比", new IndicatorAddIn("Gland area%", organArea1.divide(bigDecimal,5, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(3).toString(), "%",areaUtils.getStructureIds("10E133","10E111")));

        }
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
        //aiForecastService.addOutIndicators(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
