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
import cn.staitech.fr.service.strategy.json.OutlineCustom;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wanglibei
 * @version V1.0
 * @ClassName: SternumParserStrategyImpl
 * @Description:大鼠胸骨-7I
 * @date 2024年5月13日
 */
@Slf4j
@Component("Sternum")
public class SternumParserStrategyImpl extends AbstractCustomParserStrategy implements OutlineCustom {

    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private CommonJsonCheck commonJsonCheck;
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Autowired
    private AreaUtils areaUtils;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("SternumParserStrategyImpl init");
    }


    @Override
    public void alculationIndicators(JsonTask jsonTask) {

        log.info("大鼠胸骨构指标计算开始");

        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        Map<String, IndicatorAddIn> indicatorResultsMapSecond = new HashMap<>();

        //        红系细胞核数量	A	个	无
//        Integer mucosaCountA = commonJsonParser.getOrganAreaCount(jsonTask, "14E011");
//        mucosaCountA = commonJsonParser.getIntegerValue(mucosaCountA);
//        //        粒系细胞数量	B	个	无
//        Integer mucosaCountB = commonJsonParser.getOrganAreaCount(jsonTask, "14E01A");
//        mucosaCountB = commonJsonParser.getIntegerValue(mucosaCountB);

        //        巨核系细胞数量	C	个	无
        Integer mucosaCountC = commonJsonParser.getOrganAreaCount(jsonTask, "14E022");
        mucosaCountC = commonJsonParser.getIntegerValue(mucosaCountC);

        // D 所有红细胞轮廓面积之和 单位：103 μm2
        BigDecimal bigDecimalD = getOrganArea(jsonTask, "14E004").getStructureAreaNum().setScale(3, BigDecimal.ROUND_HALF_UP);
        // E 所有脂肪细胞轮廓面积之和 单位：103 μm2
        BigDecimal bigDecimalMicrometerE = getOrganArea(jsonTask, "14E012").getStructureAreaNum().setScale(3, BigDecimal.ROUND_HALF_UP);
        // F  组织轮廓面积 mm2
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal bigDecimalF = new BigDecimal(singleSlide.getArea());
        //bigDecimalF = getOrganArea(jsonTask, "14E111").getStructureAreaNum();
        //2 红细胞面积占比  %	2=D/F
        BigDecimal erythrocyteArea = BigDecimal.ZERO;
        if (bigDecimalF.compareTo(BigDecimal.ZERO) != 0) {
            erythrocyteArea = commonJsonParser.getProportion(bigDecimalD, bigDecimalF);
        }
        //3 脂肪细胞面积占比   %	3=E/F
        BigDecimal adipocyteArea = BigDecimal.ZERO;
        if (bigDecimalF.compareTo(BigDecimal.ZERO) != 0) {
            adipocyteArea = commonJsonParser.getProportion(bigDecimalMicrometerE, bigDecimalF);
        }

        //4 巨核系细胞密度  个/mm2	4=C/F
        BigDecimal densityOfMegakaryocyte = BigDecimal.ZERO;
        if (bigDecimalF.compareTo(BigDecimal.ZERO) != 0) {
            densityOfMegakaryocyte = commonJsonParser.getProportion(new BigDecimal(mucosaCountC), bigDecimalF);
        }
        indicatorResultsMap.put("巨核系细胞数量", new IndicatorAddIn("", String.valueOf(mucosaCountC), "个", CommonConstant.NUMBER_1, "14E022"));
        indicatorResultsMap.put("红细胞面积", new IndicatorAddIn("", String.valueOf(areaUtils.convertToSquareMicrometer(bigDecimalD.toString())), SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "14E004"));
        indicatorResultsMap.put("脂肪细胞面积", new IndicatorAddIn("", String.valueOf(areaUtils.convertToSquareMicrometer(bigDecimalMicrometerE.toString())), SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "14E012"));


        //AI指标保存
        /**
         if(BigDecimalA_G.compareTo(BigDecimal.ZERO) != 0) {
         indicatorResultsMap.put("骨髓腔面积", new IndicatorAddIn("Bone marrow area", String.valueOf(BigDecimalA_G.setScale(3, RoundingMode.HALF_UP)), "平方毫米", "0"));
         }
         if(BigDecimalC_B.compareTo(BigDecimal.ZERO) != 0) {
         indicatorResultsMap.put("粒红比", new IndicatorAddIn("Myelocyte:erythropoiesis ratio", String.valueOf(BigDecimalC_B.setScale(3, RoundingMode.HALF_UP)), "无", "0"));
         }
         if(BigDecimalE_A_G.compareTo(BigDecimal.ZERO) != 0) {
         indicatorResultsMap.put("红细胞面积占比", new IndicatorAddIn("Erythrocyte area%", String.valueOf(BigDecimalE_A_G.setScale(3, RoundingMode.HALF_UP)), "%", "0"));
         }

         if(bigDecimalC_A_G.compareTo(BigDecimal.ZERO) != 0) {
         indicatorResultsMap.put("粒系细胞密度", new IndicatorAddIn("Density of myelocyte", String.valueOf(bigDecimalC_A_G.setScale(3, RoundingMode.HALF_UP)), "个/平方毫米", "0"));
         }
         if(bigDecimalB_A_G.compareTo(BigDecimal.ZERO) != 0) {
         indicatorResultsMap.put("红系细胞核密度", new IndicatorAddIn("Nucleus density of erythropoiesis", String.valueOf(bigDecimalB_A_G.setScale(3, RoundingMode.HALF_UP)), "个/平方毫米", "0"));
         }
         */

        indicatorResultsMap.put("红细胞面积占比", new IndicatorAddIn("Erythrocyte area%", String.valueOf(erythrocyteArea.setScale(3, RoundingMode.HALF_UP)), "%", "14E004,14E111"));
        indicatorResultsMap.put("脂肪细胞面积占比", new IndicatorAddIn("Adipocyte area%", String.valueOf(adipocyteArea.setScale(3, RoundingMode.HALF_UP)), PERCENTAGE, CommonConstant.NUMBER_0, "14E012,14E111"));

        indicatorResultsMap.put("巨核系细胞密度", new IndicatorAddIn("Density of megakaryocyte", String.valueOf(densityOfMegakaryocyte.setScale(3, RoundingMode.HALF_UP)), SQ_MM_PIECE, CommonConstant.NUMBER_0, "14E022,14E111"));

        indicatorResultsMap.put("胸骨面积", new IndicatorAddIn("Sternum area", String.valueOf(bigDecimalF.setScale(3, RoundingMode.HALF_UP)), SQ_MM, CommonConstant.NUMBER_0, "14E111"));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);

    }

    @Override
    public String getAlgorithmCode() {
        return "Sternum";
    }

    @Override
    public void getCustomOutLine(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal pituitaryH = new BigDecimal(singleSlide.getArea());
        indicatorResultsMap.put("胸骨面积", createNameIndicator("Sternum area", String.valueOf(pituitaryH.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "14E111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
