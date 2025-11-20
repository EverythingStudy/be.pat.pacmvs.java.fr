package cn.staitech.fr.service.strategy.json.impl;

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
import java.util.HashMap;
import java.util.Map;

/**
 * 大鼠-骨和骨髓,胫骨
 *
 * @author admin
 */
@Slf4j
@Service("Tibia")
public class BoneWithBoneMarrowTibiaStrategyImpl extends AbstractCustomParserStrategy {
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
        log.info("BoneWithBoneMarrowFemurStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        //A 生长骨骺板面积
        BigDecimal organArea = commonJsonParser.getOrganArea(jsonTask, "151010").getStructureAreaNum();
        //D 巨核系细胞数量
        Integer organAreaCount = commonJsonParser.getOrganAreaCount(jsonTask, "151022");
        //E 红细胞面积
        BigDecimal organAreaE = commonJsonParser.getOrganArea(jsonTask, "151004").getStructureAreaNum();
        //F 脂肪细胞面积
        BigDecimal organAreaF = commonJsonParser.getOrganArea(jsonTask, "151012").getStructureAreaNum();
        //G 组织轮廓
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal bigDecimalG = new BigDecimal(singleSlide.getArea());

        //5 巨核系细胞密度 个/mm2 5=D/G
        BigDecimal densityOfMegakaryocyte = new BigDecimal(new BigDecimal(organAreaCount).divide(bigDecimalG, 3, BigDecimal.ROUND_HALF_UP).toString());
        //6 红细胞面积占比 % 6=E/G
        BigDecimal erythrocyteArea = new BigDecimal(organAreaE.divide(bigDecimalG, 3, BigDecimal.ROUND_HALF_UP).toString()).multiply(new BigDecimal("100"));
        //7 脂肪细胞面积占比 % 7=F/G
        BigDecimal adipocyteArea = new BigDecimal(organAreaF.divide(bigDecimalG, 3, BigDecimal.ROUND_HALF_UP).toString()).multiply(new BigDecimal("100"));
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        indicatorResultsMap.put("巨核系细胞数量", new IndicatorAddIn("巨核系细胞数量", organAreaCount.toString(), PIECE, CommonConstant.NUMBER_1, "151022"));
        indicatorResultsMap.put("红细胞面积", new IndicatorAddIn("红细胞面积", areaUtils.convertToSquareMicrometer(organAreaE.toString()), SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "151004"));
        indicatorResultsMap.put("脂肪细胞面积", new IndicatorAddIn("脂肪细胞面积", areaUtils.convertToSquareMicrometer(organAreaF.toString()), SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "151012"));

        indicatorResultsMap.put("生长骨骺板面积", new IndicatorAddIn("Physis area", areaUtils.convertToSquareMicrometer(organArea.toString()), SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "151010"));
        indicatorResultsMap.put("巨核系细胞密度", new IndicatorAddIn("Density of megakaryocyte", densityOfMegakaryocyte.toString(), SQ_MM_PIECE, CommonConstant.NUMBER_0, "151022"));
        indicatorResultsMap.put("红细胞面积占比", new IndicatorAddIn("Erythrocyte area", erythrocyteArea.toString(), PERCENTAGE, CommonConstant.NUMBER_0, "151004"));
        indicatorResultsMap.put("脂肪细胞面积占比", new IndicatorAddIn("Adipocyte area", adipocyteArea.toString(), PERCENTAGE, CommonConstant.NUMBER_0, "151012"));
        indicatorResultsMap.put("胫骨面积", new IndicatorAddIn("Femur area", bigDecimalG.setScale(3, BigDecimal.ROUND_HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_0, "151111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }


    @Override
    public String getAlgorithmCode() {
        return "Tibia";
    }


}
