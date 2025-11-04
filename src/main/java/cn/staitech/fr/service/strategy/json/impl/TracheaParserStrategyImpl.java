package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 大鼠-气管
 */
@Slf4j
@Service("Trachea")
public class TracheaParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private CommonJsonCheck commonJsonCheck;
    @Resource
    private AreaUtils areaUtils;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("TracheaParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        // 查询精细轮廓面积
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal accurateArea = new BigDecimal(singleSlide.getArea());
        //BigDecimal accurateArea = commonJsonParser.getOrganArea(jsonTask, "14D111").getStructureAreaNum();
        // A 所有气管腔面积之和
        BigDecimal organArea = commonJsonParser.getOrganArea(jsonTask, "14D007").getStructureAreaNum();

        // 查询黏膜上皮层
        Annotation annotation = commonJsonParser.getOrganArea(jsonTask, "14D035");

        // B 所有黏膜上皮层面积
        BigDecimal mucosaArea = annotation.getStructureAreaNum();

        // C 非嵌套轮廓：所有轮廓周长之和;嵌套轮廓：所有轮廓的外环周长+内环周长
        BigDecimal mucosaPerimeter = annotation.getStructurePerimeterNum();

        // D 获取黏膜上皮细胞核数量
        Integer mucosaCount = commonJsonParser.getOrganAreaCount(jsonTask, "14D036");

        // E 所有软骨轮廓面积之和
        BigDecimal cartilageArea = commonJsonParser.getOrganArea(jsonTask, "14D00B").getStructureAreaNum();


        // 1 黏膜上皮层平均厚度 单位:μm 1=2B/C
        BigDecimal multiplyArea2 = mucosaArea.multiply(BigDecimal.valueOf(2)).setScale(3, BigDecimal.ROUND_HALF_UP);

        BigDecimal averageThicknessOfMucosalEpithelium = multiplyArea2.divide(mucosaPerimeter, 6, RoundingMode.HALF_UP);

        // 2 黏膜上皮细胞核密度 单位:个/μm2 2=D/B
        Double nucleusDensityOfMucosalEpithelium = mucosaCount / Double.parseDouble(String.valueOf(mucosaArea));

        // 4 气管面积 单位:mm 4=F-A
        BigDecimal areaNum = accurateArea.subtract(organArea);
        // 3 软骨面积占比 单位: %  3=E/（F-A）
        BigDecimal cartilageAreas = commonJsonParser.getProportion(cartilageArea, areaNum);

        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();

        indicatorResultsMap.put("气管腔面积", new IndicatorAddIn("气管腔面积", String.valueOf(organArea.setScale(3, RoundingMode.HALF_UP)), SQ_MM, CommonConstant.NUMBER_1, "14D007"));
        indicatorResultsMap.put("黏膜上皮层面积", new IndicatorAddIn("黏膜上皮层面积", String.valueOf(mucosaArea.setScale(3, RoundingMode.HALF_UP)), SQ_MM, CommonConstant.NUMBER_1, "14D035"));
        indicatorResultsMap.put("黏膜上皮层周长", new IndicatorAddIn("黏膜上皮层周长", String.valueOf(mucosaPerimeter.setScale(3, RoundingMode.HALF_UP)), MM, CommonConstant.NUMBER_1, "14D035"));
        indicatorResultsMap.put("黏膜上皮细胞核数量", new IndicatorAddIn("黏膜上皮细胞核数量", String.valueOf(mucosaCount), PIECE, CommonConstant.NUMBER_1, "14D036"));
        indicatorResultsMap.put("软骨面积", new IndicatorAddIn("软骨面积", String.valueOf(cartilageArea.setScale(3, RoundingMode.HALF_UP)), SQ_MM, CommonConstant.NUMBER_1, "14D00B"));

        indicatorResultsMap.put("黏膜上皮层平均厚度", new IndicatorAddIn("Average thickness of mucosal epithelium", areaUtils.convertToSquareMicrometer(String.valueOf(averageThicknessOfMucosalEpithelium)), UM, CommonConstant.NUMBER_0, "14D035"));
        indicatorResultsMap.put("黏膜上皮细胞核密度", new IndicatorAddIn("Nucleus density of mucosal epithelium", String.valueOf(BigDecimal.valueOf(nucleusDensityOfMucosalEpithelium).setScale(3, RoundingMode.HALF_UP)), SQ_MM_PIECE, CommonConstant.NUMBER_0, "14D036,14D035"));
        indicatorResultsMap.put("软骨面积占比", new IndicatorAddIn("Cartilage area%", String.valueOf(cartilageAreas.setScale(3, RoundingMode.HALF_UP)), PERCENTAGE, CommonConstant.NUMBER_0, "14D00B,14D111,14D007"));
        indicatorResultsMap.put("气管面积", new IndicatorAddIn("Tracheal area", String.valueOf(areaNum.setScale(3, RoundingMode.HALF_UP)), SQ_MM, CommonConstant.NUMBER_0, "14D007,14D111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Trachea";
    }
}

