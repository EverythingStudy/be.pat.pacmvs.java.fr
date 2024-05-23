package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
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
 * 骨骼肌-MU
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
        Map<String, IndicatorAddIn> resultsMap = new HashMap<>();

        // 获取各种指标
        BigDecimal organAreaB = areaUtils.getOrganArea(jsonTask, "15C027");// B间质面积
        BigDecimal organAreaC = areaUtils.getOrganArea(jsonTask, "15C003");// C血管面积
        BigDecimal organAreaD = areaUtils.getOrganArea(jsonTask, "15C004");// D红细胞面积
        Annotation annotation = commonJsonParser.getInsideOrOutside(jsonTask,"15C003","15C004",true);
        BigDecimal organAreaE = annotation.getStructureAreaNum();// E血管内红细胞面积
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());// F精细轮廓总面积

        // 算法输出指标
        resultsMap.put("肌纤维面积（单个）", createDefaultIndicator());// A肌纤维面积（单个）
        resultsMap.put("间质面积", createIndicator(areaUtils.convertToSquareMicrometer(organAreaB.toString()), SQ_UM_THOUSAND));
        resultsMap.put("血管面积", createIndicator(areaUtils.convertToSquareMicrometer(organAreaC.toString()), SQ_UM_THOUSAND));
        resultsMap.put("红细胞面积", createIndicator(areaUtils.convertToMicrometer(organAreaD.toString()), SQ_UM));
        resultsMap.put("血管内红细胞面积", createIndicator(areaUtils.convertToMicrometer(organAreaE.toString()), SQ_UM));

        // 产品呈现指标
        resultsMap.put("骨骼肌面积", createNameIndicator("Skeletal muscle area", areaUtils.convertToSquareMicrometer(slideArea), SQ_UM_THOUSAND));
        aiForecastService.addAiForecast(jsonTask.getSingleId(),  resultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Muscle";
    }
}
