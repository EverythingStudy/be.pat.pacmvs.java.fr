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
 * 狗 胆囊
 */
@Slf4j
@Component
public class Gallbladder_3ParserStrategyImpl extends AbstractCustomParserStrategy {

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
        log.info("Gallbladder_3ParserStrategyImpl init");
    }

    @Override
    public String getAlgorithmCode() {
        return "Gall_bladder_3";
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("狗胆囊结构指标面积开始：");

        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());

        // A: 黏膜上皮面积 (10^3 平方微米)（累加多个数据）
        BigDecimal mucosalEpitheliumArea = commonJsonParser.getOrganArea(jsonTask, "311035").getStructureAreaNum();

        // B: 黏膜上皮细胞核数量（累加多个数据）
        Integer nucleusCount = commonJsonParser.getOrganAreaCount(jsonTask, "311036");

        // C: 组织轮廓面积 (10^3 平方微米)（累加多个数据）
        BigDecimal contourArea = new BigDecimal(singleSlide.getArea());

        // A: 黏膜上皮面积
        indicatorResultsMap.put(
                "黏膜上皮面积",
                new IndicatorAddIn("", mucosalEpitheliumArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "311035")
        );

        // B: 黏膜上皮细胞核数量
        indicatorResultsMap.put(
                "黏膜上皮细胞核数量",
                new IndicatorAddIn("", nucleusCount.toString(), String.valueOf(nucleusCount), CommonConstant.NUMBER_1, "311036")
        );

        // C: 组织轮廓面积
        /*indicatorResultsMap.put(
                "组织轮廓面积",
                new IndicatorAddIn("", contourArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "311111")
        );*/

        if (contourArea.signum() != 0) {
            // 1: 黏膜上皮面积占比 (%) = A / C
            indicatorResultsMap.put(
                    "黏膜上皮面积占比",
                    new IndicatorAddIn("Mucosal epithelium area%", getProportion(mucosalEpitheliumArea, contourArea).toString(), PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("311035", "311111"))
            );
        }

        if (mucosalEpitheliumArea.signum() != 0) {
            // 2: 黏膜上皮细胞核密度 (个/10^3 平方微米) = B / A
            BigDecimal nucleusDensity = new BigDecimal(nucleusCount)
                    .divide(mucosalEpitheliumArea, 6, RoundingMode.HALF_UP)
                    .setScale(3, RoundingMode.HALF_UP);
            indicatorResultsMap.put(
                    "黏膜上皮细胞核密度",
                    new IndicatorAddIn("Nucleus density of mucosal epithelium", nucleusDensity.toString(), "个/10³平方微米", CommonConstant.NUMBER_0, areaUtils.getStructureIds("311036", "311035"))
            );
        }

        // 3: 胆囊面积 (10^3 平方微米) = C
        indicatorResultsMap.put(
                "胆囊面积",
                new IndicatorAddIn("Gallbladder area", contourArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_UM_THOUSAND, CommonConstant.NUMBER_0, "311111")
        );

        // 调用AI预测服务保存结果
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
