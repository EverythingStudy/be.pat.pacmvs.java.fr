package cn.staitech.fr.service.strategy.json.impl;

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
 * 骨骼肌
 */
@Slf4j
@Service("Muscle")
public class MuscleParserStrategyImpl extends AbstractCustomParserStrategy {
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private AreaUtils areaUtils;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        log.info("MuscleParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();

        // todo A肌纤维面积（单个）
        // B间质面积
        BigDecimal organAreaB = areaUtils.getOrganArea(jsonTask, "15C027");
        // C血管面积
        BigDecimal organAreaC = areaUtils.getOrganArea(jsonTask, "15C003");
        // D红细胞面积
        BigDecimal organAreaD = areaUtils.getOrganArea(jsonTask, "15C004");
        // E血管内红细胞面积
        BigDecimal intravascularErythrocyteArea = commonJsonParser.getInsideOrOutside(jsonTask,"15C003","15C004",true).getStructureAreaNum();
        // F精细轮廓总面积-平方毫米
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());

        // 算法输出指标
        //indicatorResultsMap.put("肌纤维面积（单个）", new IndicatorAddIn("", organAreaA.toString(), "平方毫米", "1"));
        indicatorResultsMap.put("间质面积", new IndicatorAddIn("", areaUtils.convertToSquareMicrometer(organAreaB.toString()), "10³平方微米", "1"));
        indicatorResultsMap.put("血管面积", new IndicatorAddIn("", areaUtils.convertToSquareMicrometer(organAreaC.toString()), "10³平方微米", "1"));
        indicatorResultsMap.put("红细胞面积", new IndicatorAddIn("", organAreaD.toString(), "平方微米", "1"));
        indicatorResultsMap.put("血管内红细胞面积", new IndicatorAddIn("", intravascularErythrocyteArea.toString(), "平方毫米", "1"));

        // 单位换算
        String result = areaUtils.convertToSquareMicrometer(slideArea);

        // 产品呈现指标
        indicatorResultsMap.put("骨骼肌面积", new IndicatorAddIn("Skeletal muscle area", result, "10³平方微米"));
        /*
        indicatorResultsMap.put("肌纤维面积（单个）", new IndicatorAddIn("Muscle fiber area (per)", "", ""));
        indicatorResultsMap.put("间质面积占比", new IndicatorAddIn("Mesenchyme area %", "", ""));
        indicatorResultsMap.put("血管面积占比", new IndicatorAddIn("Vessel area%", "", ""));
        indicatorResultsMap.put("血管内红细胞面积占比", new IndicatorAddIn("Intravascular erythrocyte area%", "", ""));
        indicatorResultsMap.put("血管外红细胞面积占比", new IndicatorAddIn("Extravascular erythrocyte area%", "", ""));
        */
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Muscle";
    }
}
