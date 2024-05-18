package cn.staitech.fr.service.strategy.json.impl.digestive;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author wudi
 * @Date 2024/5/13 10:05
 * @desc 精囊腺
 */
@Slf4j
@Component("Seminal_vesicles")
public class SeminalVesicleGlandParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private AnnotationMapper annotationMapper;
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;


    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        log.info("SeminalVesicleGlandParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("精囊腺结构指标计算开始");

        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal organArea = commonJsonParser.getOrganArea(jsonTask, "12D074").getStructureAreaNum();
        BigDecimal organArea1 = commonJsonParser.getOrganArea(jsonTask, "12D0E9").getStructureAreaNum();

        indicatorResultsMap.put("精囊腺面积", new IndicatorAddIn("Seminal vesicle area", singleSlide.getArea(), "平方毫米"));
        indicatorResultsMap.put("腺上皮面积（全片）", new IndicatorAddIn("Acinar epithelial area (all)", organArea.toString(), "平方毫米"));
        indicatorResultsMap.put("腺腔面积（全片）", new IndicatorAddIn("Glandular cavity area (all)", organArea1.toString(), "平方毫米", CommonConstant.NUMBER_1));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);



    }

    @Override
    public String getAlgorithmCode() {
        return "Seminal_vesicles";
    }
}
