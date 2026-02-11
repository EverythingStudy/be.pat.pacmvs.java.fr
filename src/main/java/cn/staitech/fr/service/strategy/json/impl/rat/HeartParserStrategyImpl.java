package cn.staitech.fr.service.strategy.json.impl.rat;

import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.service.strategy.json.OutlineCustom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chen
 * @date 2020/5/27
 * @ description 心脏
 */
@Slf4j
@Component("Heart")
public class HeartParserStrategyImpl extends AbstractCustomParserStrategy implements OutlineCustom {
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private CommonJsonCheck commonJsonCheck;

    @Override
    public void parseJson(JsonTask jsonTask, JsonFile jsonFileS) {
        commonJsonParser.parseJson(jsonTask, jsonFileS);
    }

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("SpleenParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        //A 组织面积 mm2
        BigDecimal organArea = commonJsonParser.getOrganArea(jsonTask, "15E003").getStructureAreaNum();
        //B 轮廓面积 mm2
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal pituitaryB = new BigDecimal(singleSlide.getArea());
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        // A 血管面积 mm2
        indicatorResultsMap.put("血管面积", createIndicator(organArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "15E003"));
        //1 血管面积占比 % 1=A/B
        indicatorResultsMap.put("血管面积占比", createNameIndicator("Vessel area%", getProportion(organArea, pituitaryB).toString(), PERCENTAGE, "15E003,15E111"));
        //2 心脏面积 mm2
        indicatorResultsMap.put("心脏面积", createNameIndicator("Heart", String.valueOf(pituitaryB.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "15E111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Heart";
    }

    @Override
    public void getCustomOutLine(JsonTask jsonTask) {
        //A 组织面积 mm2
        BigDecimal organArea = commonJsonParser.getOrganArea(jsonTask, "15E003").getStructureAreaNum();
        //B 轮廓面积 mm2
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal pituitaryB = new BigDecimal(singleSlide.getArea());
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        // A 血管面积 mm2
        indicatorResultsMap.put("组织面积", createIndicator(organArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "15E003"));
        //1 血管面积占比 % 1=A/B
        indicatorResultsMap.put("血管面积占比", createNameIndicator("Vessel area%", getProportion(organArea, pituitaryB).toString(), SQ_MM, "15E003,15E111"));
        //2 心脏面积 mm2
        indicatorResultsMap.put("心脏面积", createNameIndicator("Heart", String.valueOf(pituitaryB.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "15E111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
