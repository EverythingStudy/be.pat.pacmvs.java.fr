package cn.staitech.fr.service.strategy.json.impl.dog.musbonesjoint;

import cn.staitech.fr.constant.CommonConstant;
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
 * 狗-股骨
 *
 * @author jiazx
 */
@Slf4j
@Service("Femur_3")
public class Bone_3WithBoneMarrowFemurStrategyImpl extends AbstractCustomParserStrategy {
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
        log.info("Bone_3WithBoneMarrowFemurStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("指标计算开始：股骨");

        //B 关节软骨面积
        BigDecimal organArea = commonJsonParser.getOrganArea(jsonTask, "35000B").getStructureAreaNum();
        //C 关节软骨周长
        BigDecimal organAreaC = commonJsonParser.getOrganArea(jsonTask, "35000B").getStructurePerimeterNum();

        //A 组织轮廓面积
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal bigDecimalA = new BigDecimal(singleSlide.getArea());

        //1 关节软骨面积占比 1=B/A
        BigDecimal articular = commonJsonParser.getProportion(organArea, bigDecimalA);
        //3 关节软骨厚度 3=2B/C
        BigDecimal multiplyArea2 = organArea.multiply(BigDecimal.valueOf(2)).setScale(3, BigDecimal.ROUND_HALF_UP);
        BigDecimal thickness = bigDecimalDivideCheck(multiplyArea2, organAreaC);


        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        indicatorResultsMap.put("关节软骨面积", new IndicatorAddIn("巨核系细胞数量", String.valueOf(organArea.setScale(3, RoundingMode.HALF_UP)), SQ_MM, CommonConstant.NUMBER_1, "35000B"));
        indicatorResultsMap.put("关节软骨周长", new IndicatorAddIn("关节软骨周长", String.valueOf(organAreaC.setScale(3, RoundingMode.HALF_UP)), MM, CommonConstant.NUMBER_1, "35000B"));


        indicatorResultsMap.put("关节软骨面积占比", new IndicatorAddIn("Articular cartilage area%", articular.toString(), PERCENTAGE, CommonConstant.NUMBER_0, "35000B,350111"));
        indicatorResultsMap.put("股骨面积", new IndicatorAddIn("Femur area", organArea.setScale(3, BigDecimal.ROUND_HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_0, "350111"));
        indicatorResultsMap.put("关节软骨厚度", new IndicatorAddIn("Average thickness of articular cartilage", thickness.toString(), UM, CommonConstant.NUMBER_0, "35000B"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);

        log.info("指标计算结束：股骨");
    }


    @Override
    public String getAlgorithmCode() {
        return "Femur_3";
    }


}
