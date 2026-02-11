package cn.staitech.fr.service.strategy.json.impl.rat.gou;

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
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 狗 喉结构指标
 */
@Slf4j
@Component
public class Larynx_3ParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private AreaUtils areaUtils;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private CommonJsonCheck commonJsonCheck;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("Larynx_3ParserStrategyImpl init");
    }

    @Override
    public String getAlgorithmCode() {
        return "Larynx_3";
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("狗喉结构指标面积开始：");

        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());

        // A: 黏膜上皮面积 mm²（累加多个数据）
        BigDecimal mucosalEpitheliumArea = commonJsonParser.getOrganArea(jsonTask, "30E035").getStructureAreaNum();

        // B: 腺体面积 mm²（累加多个数据）
        BigDecimal glandArea = commonJsonParser.getOrganArea(jsonTask, "30E133").getStructureAreaNum();

        // C: 组织轮廓面积 mm²（累加多个数据）
        BigDecimal contourArea = new BigDecimal(singleSlide.getArea());


        // A: 黏膜上皮面积
        indicatorResultsMap.put(
                "黏膜上皮面积",
                new IndicatorAddIn("", mucosalEpitheliumArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1, "30E035")
        );

        // B: 腺体面积
        indicatorResultsMap.put(
                "腺体面积",
                new IndicatorAddIn("", glandArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1, "30E133")
        );

        indicatorResultsMap.put(
                "腺体面积",
                new IndicatorAddIn("", contourArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1, "30E111")
        );

        if (contourArea.signum() != 0) {
            // 1: 黏膜上皮面积占比 (%) = A / C
            indicatorResultsMap.put(
                    "黏膜上皮面积占比",
                    new IndicatorAddIn("Mucosal epithelium area%", getProportion(mucosalEpitheliumArea, contourArea).toString(), PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("30E035", "30E111"))
            );

            // 2: 腺体面积占比 (%) = B / C
            indicatorResultsMap.put(
                    "腺体面积占比",
                    new IndicatorAddIn("Gland area%", getProportion(glandArea, contourArea).toString(), PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("30E133", "30E111"))
            );
        }

        // 3: 喉面积 mm² = C
        indicatorResultsMap.put(
                "喉面积",
                new IndicatorAddIn("Larynx area", contourArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_0, "30E111")
        );

        // 调用AI预测服务保存结果
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
