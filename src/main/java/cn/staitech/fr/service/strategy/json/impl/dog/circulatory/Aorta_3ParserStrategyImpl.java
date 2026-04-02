package cn.staitech.fr.service.strategy.json.impl.dog.circulatory;

import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.service.strategy.json.OutlineCustom;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 犬 血液循环系统 - 主动脉指标计算
 * @author zhangy
 */
@Slf4j
@Component("Aorta_3")
public class Aorta_3ParserStrategyImpl extends AbstractCustomParserStrategy implements OutlineCustom {

    /** 犬主动脉 - 空腔结构ID */
    private static final String STRUCTURE_CAVITY = "35D113";
    /** 犬主动脉 - 组织轮廓结构ID */
    private static final String STRUCTURE_OUTLINE = "35D111";

    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private CommonJsonCheck commonJsonCheck;

    @Override
    public void parseJson(JsonTask jsonTask, JsonFile jsonFileS) {
        commonJsonParser.parseJson(jsonTask, jsonFileS);
    }

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("Dog AortaParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("犬主动脉指标计算开始 singleId={}", jsonTask.getSingleId());
        Map<String, IndicatorAddIn> indicatorResultsMap = buildAortaIndicators(jsonTask);
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
        log.info("犬主动脉指标计算完成");
    }

    @Override
    public String getAlgorithmCode() {
        return "Aorta_3";
    }

    @Override
    public void getCustomOutLine(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = buildAortaIndicators(jsonTask);
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    /**
     * A 空腔面积 mm² - 结构35D113数据相加输出
     * B 空腔周长 mm - 结构35D113数据相加输出
     * C 组织轮廓周长 mm - 单切片轮廓周长
     * D 组织轮廓面积 mm² - 单切片轮廓面积
     * 1 主动脉壁面积 mm² = D - A，保留三位小数
     * 2 主动脉壁平均厚度 µm = 2 * (D - A) / (B + C)，保留三位小数
     */
    private Map<String, IndicatorAddIn> buildAortaIndicators(JsonTask jsonTask) {
        // A 空腔面积 mm² - 结构35D113，数据相加输出
        Annotation cavityAnnotation = commonJsonParser.getOrganArea(jsonTask, STRUCTURE_CAVITY);
        BigDecimal cavityAreaA = ObjectUtil.isNotEmpty(cavityAnnotation) && cavityAnnotation.getStructureAreaNum() != null
                ? cavityAnnotation.getStructureAreaNum() : BigDecimal.ZERO;

        // B 空腔周长 mm - 结构35D113，数据相加输出
        BigDecimal cavityPerimeterB = ObjectUtil.isNotEmpty(cavityAnnotation) && cavityAnnotation.getStructurePerimeterNum() != null
                ? cavityAnnotation.getStructurePerimeterNum() : BigDecimal.ZERO;

        // C 组织轮廓周长 mm - 单切片轮廓周长
        // D 组织轮廓面积 mm² - 单切片轮廓面积
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal tissuePerimeterC = ObjectUtil.isNotEmpty(singleSlide) && StringUtils.isNotEmpty(singleSlide.getPerimeter())
                ? new BigDecimal(singleSlide.getPerimeter()) : BigDecimal.ZERO;
        BigDecimal tissueAreaD = ObjectUtil.isNotEmpty(singleSlide) && StringUtils.isNotEmpty(singleSlide.getArea())
                ? new BigDecimal(singleSlide.getArea()) : BigDecimal.ZERO;

        Map<String, IndicatorAddIn> result = new HashMap<>();

//        // 算法输出指标：空腔面积 A
//        result.put("空腔面积", createIndicator(cavityAreaA.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, STRUCTURE_CAVITY));
//
//        // 算法输出指标：空腔周长 B
//        result.put("空腔周长", createIndicator(cavityPerimeterB.setScale(3, RoundingMode.HALF_UP).toString(), MM, STRUCTURE_CAVITY));
//
//        // 算法输出指标：组织轮廓周长 C
//        result.put("组织轮廓周长", createIndicator(tissuePerimeterC.setScale(3, RoundingMode.HALF_UP).toString(), MM, STRUCTURE_OUTLINE));
//
//        // 算法输出指标：组织轮廓面积 D
//        result.put("组织轮廓面积", createIndicator(tissueAreaD.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, STRUCTURE_OUTLINE));

        // 主动脉壁面积 mm² = D - A
        BigDecimal wallArea = tissueAreaD.subtract(cavityAreaA);
        if (wallArea.compareTo(BigDecimal.ZERO) >= 0) {
            result.put("主动脉壁面积", createNameIndicator("Aorta wall area", wallArea.setScale(3, RoundingMode.HALF_UP).toString(),
                            SQ_MM, STRUCTURE_OUTLINE + "," + STRUCTURE_CAVITY));
        }

        // 主动脉壁平均厚度 µm = 2 * (D - A) / (B + C)
        // 单位是毫米，转换为微米
        BigDecimal totalPerimeter = cavityPerimeterB.add(tissuePerimeterC);
        if (wallArea.compareTo(BigDecimal.ZERO) > 0 && totalPerimeter.compareTo(BigDecimal.ZERO) > 0) {
            //2 * (D - A) / (B + C)，结果单位是毫米
            BigDecimal thicknessInMm = BigDecimal.valueOf(2)
                    .multiply(wallArea)
                    .divide(totalPerimeter, 6, RoundingMode.HALF_UP);
            //微米：毫米 * 1000 = 微米
            BigDecimal thicknessInUm = thicknessInMm.multiply(BigDecimal.valueOf(1000))
                    .setScale(3, RoundingMode.HALF_UP);

            result.put("主动脉壁平均厚度",
                    createNameIndicator("Average thickness of aorta wall", thicknessInUm.toString(),
                            UM, STRUCTURE_OUTLINE + "," + STRUCTURE_CAVITY));
        }

        return result;
    }
}
