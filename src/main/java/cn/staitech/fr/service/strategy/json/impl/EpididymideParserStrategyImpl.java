package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


/**
 * 附睾-EP
 */
@Slf4j
@Service("Epididymide")
public class EpididymideParserStrategyImpl extends AbstractCustomParserStrategy {
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private AreaUtils areaUtils;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        log.info("EpididymideParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();

        // todo A输出小管/附睾管黏膜上皮面积（单个）
        // B输出小管/附睾管黏膜上皮面积（全片）
        BigDecimal organAreaB = commonJsonParser.getOrganAreaMicron(jsonTask, "12F0F5");
        // C输出小管/附睾管黏膜上皮周长（单个）
        Annotation annotation = commonJsonParser.getOrganArea(jsonTask, "12F0F5");
        BigDecimal structurePerimeterNum = annotation.getStructurePerimeterNum();
        // todo D输出小管/附睾管管腔面积（单个）
        // E输出小管/附睾管管腔面积（全片）
        BigDecimal organAreaE = commonJsonParser.getOrganAreaMicron(jsonTask, "12F0F4");
        // todo F精子面积（单个）
        // G精子面积（全片）
        BigDecimal organAreaG = commonJsonParser.getOrganAreaMicron(jsonTask, "12F0F7");
        // todo H黏膜上皮细胞核数量（单个）
        // I血管面积
        BigDecimal organAreaI = commonJsonParser.getOrganAreaMicron(jsonTask, "12F003");
        // J组织轮廓面积-平方毫米
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());

        // 算法输出指标
        indicatorResultsMap.put("输出小管/附睾管黏膜上皮面积（全片）", new IndicatorAddIn("", organAreaB.toString(), "平方毫米", "1"));
        indicatorResultsMap.put("输出小管/附睾管黏膜上皮周长（单个）", new IndicatorAddIn("", structurePerimeterNum.toString(), "毫米", "1"));
        indicatorResultsMap.put("输出小管/附睾管管腔面积（全片）", new IndicatorAddIn("", organAreaE.toString(), "平方毫米", "1"));
        indicatorResultsMap.put("精子面积（全片）", new IndicatorAddIn("", organAreaG.toString(), "平方毫米", "1"));
        indicatorResultsMap.put("血管面积", new IndicatorAddIn("", organAreaI.toString(), "平方毫米", "1"));
        /*
         indicatorResultsMap.put("输出小管/附睾管黏膜上皮面积（单个）", new IndicatorAddIn("", "", "10³平方微米", "1"));
         indicatorResultsMap.put("输出小管/附睾管管腔面积（单个）", new IndicatorAddIn("", "", "10³平方微米", "1"));
         indicatorResultsMap.put("精子面积（单个）", new IndicatorAddIn("", "", "10³平方微米", "1"));
         indicatorResultsMap.put("黏膜上皮细胞核数量（单个）", new IndicatorAddIn("", areaCount.toString(), "个", "1"));
         */

        // 产品呈现指标
        indicatorResultsMap.put("附睾面积", new IndicatorAddIn("Epididymal area", slideArea, "平方毫米"));
        /*
        indicatorResultsMap.put("输出小管和附睾管面积占比（全片）", new IndicatorAddIn("Efferent ducts and epididymal ducts area%（all）", "", "%"));
        indicatorResultsMap.put("间质面积占比", new IndicatorAddIn("Mesenchyme area%", "", "%"));
        indicatorResultsMap.put("黏膜上皮面积占比（单个）", new IndicatorAddIn("Mucosal epithelium area% (per)", "", "%"));
        indicatorResultsMap.put("精子面积占比（单个）", new IndicatorAddIn("Sperm area% (per)", "", "%"));
        indicatorResultsMap.put("精子面积占比（全片）", new IndicatorAddIn("Sperm area% (all)", "", "%"));
        indicatorResultsMap.put("黏膜上皮细胞核密度（单个）", new IndicatorAddIn("Mucosal epithelial nucleus% (per)", "", "个/毫米"));
        indicatorResultsMap.put("血管相对面积", new IndicatorAddIn("Vessel area%", "", "%"));
        indicatorResultsMap.put("黏膜上皮厚度（单个）", new IndicatorAddIn("Average thickness of mucosal epithelium (per)", "", "微米"));
        */

        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Epididymide";
    }
}
