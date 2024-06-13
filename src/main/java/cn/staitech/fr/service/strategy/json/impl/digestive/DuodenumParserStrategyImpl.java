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
 * @Date 2024/5/16 14:33
 * @desc 十二指肠
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
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal organArea = commonJsonParser.getOrganArea(jsonTask, "11900C").getStructureAreaNum();
        BigDecimal organArea1 = commonJsonParser.getOrganArea(jsonTask, "11901E").getStructureAreaNum();

        indicatorResultsMap.put("十二指肠面积", new IndicatorAddIn("Duodenum area", singleSlide.getArea(), "平方毫米"));
        indicatorResultsMap.put("肌层", new IndicatorAddIn("Muscular layer", organArea.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("黏膜上皮+固有层", new IndicatorAddIn("Mucosal epithelium+lamina propria", organArea1.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
        //aiForecastService.addOutIndicators(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
