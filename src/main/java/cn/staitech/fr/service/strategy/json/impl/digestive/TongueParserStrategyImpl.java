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
 * @Date 2024/5/16 16:02
 * @desc 大鼠舌
 */
@Slf4j
@Component("Tongue")
public class TongueParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private AiForecastService aiForecastService;

    @Resource
    private CommonJsonParser commonJsonParser;

    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private CommonJsonCheck commonJsonCheck;
    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("TongueParserStrategyImpl init");
    }

    @Override
    public String getAlgorithmCode() {
        return "Tongue";
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("大鼠舌结构指标面积计算：");
        //组织轮廓面积
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        BigDecimal organArea = commonJsonParser.getOrganAreaMicron(jsonTask, "10D12E");
        BigDecimal organArea1 = commonJsonParser.getOrganAreaMicron(jsonTask, "10D12F");
        BigDecimal organArea2 = commonJsonParser.getOrganArea(jsonTask, "10D01C").getStructureAreaNum();
        BigDecimal bigDecimal = new BigDecimal(singleSlide.getArea());
        if(bigDecimal.signum() == 0){
            indicatorResultsMap.put("角质层面积占比", new IndicatorAddIn("Stratum corneum area%", "0.000", "%"));
            indicatorResultsMap.put("颗粒层+棘层+基底细胞层面积占比", new IndicatorAddIn("Nucleated cell layer area%", "0.000", "%"));
            indicatorResultsMap.put("固有层和肌层面积占比", new IndicatorAddIn("Lamina propria and Muscularis area%", "0.000", "%"));
        }else{
            BigDecimal multiply = bigDecimal.multiply(new BigDecimal("1000"));
            indicatorResultsMap.put("角质层面积占比", new IndicatorAddIn("Stratum corneum area%", organArea.divide(multiply,3,RoundingMode.HALF_UP).toString(),"%"));
            indicatorResultsMap.put("颗粒层+棘层+基底细胞层面积占比", new IndicatorAddIn("Nucleated cell layer area%", organArea1.divide(multiply,3,RoundingMode.HALF_UP).toString(), "%"));
            indicatorResultsMap.put("固有层和肌层面积占比", new IndicatorAddIn("Lamina propria and Muscularis area%", organArea2.divide(bigDecimal,3,RoundingMode.HALF_UP).toString(), "%"));
        }

        indicatorResultsMap.put("舌面积", new IndicatorAddIn("Tongue area", singleSlide.getArea(), "平方毫米"));
        indicatorResultsMap.put("角质层面积", new IndicatorAddIn("Stratum corneum area", organArea.setScale(3, RoundingMode.HALF_UP).toString(), "10³平方微米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("颗粒层+棘层+基底细胞层面积", new IndicatorAddIn("Nucleated cell layer area", organArea1.setScale(3, RoundingMode.HALF_UP).toString(), "10³平方微米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("固有层+肌层面积", new IndicatorAddIn("Lamina propria and Muscularis area", organArea2.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
        //aiForecastService.addOutIndicators(jsonTask.getSingleId(), indicatorResultsMap);

    }
}
