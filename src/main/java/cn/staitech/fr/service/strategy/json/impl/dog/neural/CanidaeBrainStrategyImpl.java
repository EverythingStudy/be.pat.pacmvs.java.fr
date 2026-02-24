package cn.staitech.fr.service.strategy.json.impl.dog.neural;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import cn.staitech.fr.utils.DecimalUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 大脑
 */

@Slf4j
@Component("CanidaeBrainStrategy")
public class CanidaeBrainStrategyImpl  extends AbstractCustomParserStrategy {
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
    @Autowired
    private AreaUtils areaUtils;

    @Override
    public void parseJson(JsonTask jsonTask, JsonFile jsonFileS) {
        commonJsonParser.parseJson(jsonTask, jsonFileS);
    }

    @Override
    public boolean checkJson(JsonTask jsonTask, List<JsonFile> jsonFileList) {
        return commonJsonCheck.checkJson(jsonTask, jsonFileList);
    }

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("CanidaeBrainStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("大鼠大脑 332-BR 指标计算开始……{}", jsonTask);
        Map<String, IndicatorAddIn> map = new HashMap<>();

        // A 脉络丛面积	mm2	无
        BigDecimal choroidOPlexusAreaAnnotation = commonJsonParser.getOrganArea(jsonTask, "33209C").getStructureAreaNum();
        // B 血管外红细胞面积	103 μm2	无 (查询血管外红细胞面积)
        BigDecimal extravascularErythrocyteArea = commonJsonParser.getInsideOrOutside(jsonTask, "332003", "332004", false).getStructureAreaNum();
        // C 血管内红细胞面积	mm2	无 (查询血管内红细胞面积)
        BigDecimal intravascularErythrocyteArea = commonJsonParser.getInsideOrOutside(jsonTask, "332003", "332004", true).getStructureAreaNum();
        // D:精细轮廓总面积（大鼠大脑）- 平方毫米
        String accurateArea = singleSlideMapper.selectById(jsonTask.getSingleId()).getArea();
        BigDecimal accurateAreaBigDecimal = new BigDecimal(accurateArea);

        // 算法输出指标 -------------------------------------------------------------
        // A
        map.put("脉络丛面积", new IndicatorAddIn("Choroid Plexus area", DecimalUtils.setScale3(choroidOPlexusAreaAnnotation), SQ_MM, CommonConstant.NUMBER_1, "33209C"));
        // B
        map.put("血管外红细胞面积", new IndicatorAddIn("Extravascular Erythrocyte area", areaUtils.convertToSquareMicrometer(extravascularErythrocyteArea.toString()), SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "332003,332004"));
        // C
        map.put("血管内红细胞面积", new IndicatorAddIn("Intravascular Erythrocyte area", areaUtils.convertToSquareMicrometer(intravascularErythrocyteArea.toString()), SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "332003,332004"));

        // 产品呈现指标 -------------------------------------------------------------
        if (accurateAreaBigDecimal.compareTo(BigDecimal.ZERO) != 0) {
            // 1 脉络丛面积占比	%	Choroid Plexus area %	1=A/D	无
            map.put("脉络丛面积占比", new IndicatorAddIn("Choroid Plexus area %", getProportion(choroidOPlexusAreaAnnotation, accurateAreaBigDecimal).toString(), PERCENTAGE, areaUtils.getStructureIds("33209C", "332111")));
            // 2 血管外红细胞面积占比	%	Extravascular Erythrocyte area%	2=B/D	无
            map.put("血管外红细胞面积占比", new IndicatorAddIn("Extravascular Erythrocyte area%", getProportion(extravascularErythrocyteArea, accurateAreaBigDecimal).toString(), PERCENTAGE, "332003,332004,332111"));
            // 3 血管内红细胞面积占比%	Intravascular Erythrocyte area%	3=C/D	无
            map.put("血管内红细胞面积占比", new IndicatorAddIn("Intravascular Erythrocyte area%", getProportion(intravascularErythrocyteArea, accurateAreaBigDecimal).toString(), PERCENTAGE, "332003,332004,332111"));
        }

        // D 大脑面积	4	平方毫米	Brain area	4=D	无
        map.put("大脑面积", new IndicatorAddIn("Brain area", DecimalUtils.setScale3(accurateAreaBigDecimal), CommonConstant.SQUARE_MILLIMETRE, "132111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
    }

    @Override
    public String getAlgorithmCode() {
        return "CanidaeBrainStrategy";
    }
}
