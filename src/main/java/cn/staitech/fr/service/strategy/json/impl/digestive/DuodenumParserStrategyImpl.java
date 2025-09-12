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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 
* @ClassName: DuodenumParserStrategyImpl
* @Description-d:十二指肠
* @author wanglibei
* @date 2025年7月21日
* @version V1.0
 */
@Slf4j
@Component("Duodenum")
public class DuodenumParserStrategyImpl extends AbstractCustomParserStrategy {

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
    
    @Autowired
    private AreaUtils areaUtils;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("DuodenumParserStrategyImpl init");
    }

    @Override
    public String getAlgorithmCode() {
        return "Duodenum";
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("十二指肠结构面积计算：");
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        //组织轮廓 119111
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        //B肌层
        BigDecimal organArea = commonJsonParser.getOrganArea(jsonTask, "11900C").getStructureAreaNum();
        //A黏膜上皮+固有层
        BigDecimal organArea1 = commonJsonParser.getOrganArea(jsonTask, "11901E").getStructureAreaNum();
        //C 组织轮廓面积 119111
        String area = singleSlide.getArea();
//        BigDecimal areaDecimal = commonJsonParser.getOrganArea(jsonTask, "119111").getStructureAreaNum();
//        String area = areaDecimal.toString();
        if(new BigDecimal(area).signum() == 0){
        	// B/C
            indicatorResultsMap.put("肌层面积占比", new IndicatorAddIn("Muscular area%", area, "%",areaUtils.getStructureIds("11900C", "119111")));
            // A/C
            indicatorResultsMap.put("黏膜上皮和固有层面积占比", new IndicatorAddIn("Mucosal epithelium and lamina propria area%", area, "%",areaUtils.getStructureIds("11901E", "119111")));
        }else{
        	// B/C
            indicatorResultsMap.put("肌层面积占比", new IndicatorAddIn("Muscular area%", organArea.divide(new BigDecimal(area),5,RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(3).toString(), "%",areaUtils.getStructureIds("11900C", "119111")));
            // A/C
            indicatorResultsMap.put("黏膜上皮和固有层面积占比", new IndicatorAddIn("Mucosal epithelium and lamina propria area%", organArea1.divide(new BigDecimal(area),5,RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(3).toString(), "%",areaUtils.getStructureIds("11901E", "119111")));

        }
        //C
        indicatorResultsMap.put("十二指肠面积", new IndicatorAddIn("Duodenum area", new BigDecimal(singleSlide.getArea()).setScale(3,RoundingMode.HALF_UP).toString(), SQ_MM,"119111"));
        //11900C
        indicatorResultsMap.put("肌层面积", new IndicatorAddIn("Muscular layer", organArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1,"11900C"));
        //11901E
        indicatorResultsMap.put("黏膜上皮+固有层", new IndicatorAddIn("Mucosal epithelium+lamina propria", organArea1.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1,"11901E"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
        //aiForecastService.addOutIndicators(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
