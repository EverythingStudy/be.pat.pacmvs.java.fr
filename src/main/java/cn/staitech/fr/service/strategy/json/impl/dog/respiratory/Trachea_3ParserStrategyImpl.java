package cn.staitech.fr.service.strategy.json.impl.dog.respiratory;

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
 * 狗-气管
 * @author jiazx
 */
@Slf4j
@Service("Trachea_3")
public class Trachea_3ParserStrategyImpl extends AbstractCustomParserStrategy {

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
        log.info("Trachea_3ParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("指标计算开始-气管");

        // 查询精细轮廓面积
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        //组织轮廓面积 A
        BigDecimal accurateArea = new BigDecimal(singleSlide.getArea());

        //黏膜上皮层面积 B
        Annotation annotation = commonJsonParser.getOrganArea(jsonTask, "34D035");

        // B 所有黏膜上皮层面积
        BigDecimal mucosaArea = annotation.getStructureAreaNum();

        // C 黏膜上皮层周长
        BigDecimal mucosaPerimeter = annotation.getStructurePerimeterNum();

        // D 获取黏膜上皮细胞核数量
        Integer mucosaCount = commonJsonParser.getOrganAreaCount(jsonTask, "34D036");

        // 1 黏膜上皮层平均厚度 单位:μm 1=2B/C
        BigDecimal multiplyArea2 = mucosaArea.multiply(BigDecimal.valueOf(2)).setScale(3, BigDecimal.ROUND_HALF_UP);

        BigDecimal averageThicknessOfMucosalEpithelium = multiplyArea2.divide(mucosaPerimeter, 6, RoundingMode.HALF_UP);

        // 2 黏膜上皮细胞核密度 单位:个/103 μm2  2=D/B
        Double nucleusDensityOfMucosalEpithelium = mucosaCount / Double.parseDouble(String.valueOf(mucosaArea));



        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        // 算法输出指标 -------------------------------------------------------------
        indicatorResultsMap.put("黏膜上皮层面积", new IndicatorAddIn("黏膜上皮层面积", String.valueOf(mucosaArea.setScale(3, RoundingMode.HALF_UP)), MULTIPLIED_SQ_UM, CommonConstant.NUMBER_1, "34D035"));
        indicatorResultsMap.put("黏膜上皮层周长", new IndicatorAddIn("黏膜上皮层周长", String.valueOf(mucosaPerimeter.setScale(3, RoundingMode.HALF_UP)), MM, CommonConstant.NUMBER_1, "34D035"));
        indicatorResultsMap.put("黏膜上皮细胞核数量", new IndicatorAddIn("黏膜上皮细胞核数量", String.valueOf(mucosaCount), PIECE, CommonConstant.NUMBER_1, "34D036"));

        // 产品呈现指标 -------------------------------------------------------------
        indicatorResultsMap.put("黏膜上皮层平均厚度", new IndicatorAddIn("Average thickness of mucosal epithelium", areaUtils.convertToSquareMicrometer(String.valueOf(averageThicknessOfMucosalEpithelium)), UM, CommonConstant.NUMBER_0, "14D035"));
        indicatorResultsMap.put("黏膜上皮细胞核密度", new IndicatorAddIn("Nucleus density of mucosal epithelium", String.valueOf(BigDecimal.valueOf(nucleusDensityOfMucosalEpithelium).setScale(3, RoundingMode.HALF_UP)), SQ_UM_PICE, CommonConstant.NUMBER_0, "34D036,34D035"));
        indicatorResultsMap.put("气管面积", new IndicatorAddIn("Tracheal area", String.valueOf(accurateArea.setScale(3, RoundingMode.HALF_UP)), SQ_MM, CommonConstant.NUMBER_0, "34D111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
        log.info("指标计算结束-气管");
    }

    @Override
    public String getAlgorithmCode() {
        return "Trachea_3";
    }
}

