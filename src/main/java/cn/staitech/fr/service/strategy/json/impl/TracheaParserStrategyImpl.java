package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
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
 * 大鼠-气管
 */
@Slf4j
@Service("Trachea")
public class TracheaParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    private AiForecastService aiForecastService;

    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private CommonJsonCheck commonJsonCheck;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("TracheaParserStrategyImpl init");
    }


    /**
     * 气管
     * 结构	编码
     * 气管腔	14D007
     * 黏膜上皮层	14D035
     * 黏膜上皮细胞核 	14D036
     * 软骨	14D00B
     * 肌层	14D00C
     * 组织轮廓	14D111
     * 算法输出指标	指标代码（仅限本文档）	单位(保留小数点后3位)	备注
     * 气管腔面积	A	平方毫米
     * 黏膜上皮层面积	B	平方毫米	以C型或双层环状输出
     * 黏膜上皮层周长	C	毫米
     * 黏膜上皮细胞核数量	D	个
     * 软骨面积	E	平方毫米	若多个数据则相加输出
     * 组织轮廓面积	F	平方毫米
     * <p>
     * 产品呈现指标	指标代码（仅限本文档）	单位(保留小数点后3位)	English	计算方式	备注
     * 黏膜上皮层平均厚度	1	微米	Average thickness of mucosal epithelium	1=2B/C
     * 黏膜上皮细胞核密度	2	个/平方毫米	Nucleus density of mucosal epithelium 	2=D/B
     * 软骨面积占比	3	%	Cartilage area%	3=E/（F-A）
     * 气管面积	4	平方毫米	Tracheal area	4=F-A
     */

    @Resource
    private AreaUtils areaUtils;

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        // 查询精细轮廓面积
//        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
//        String accurateArea = singleSlide.getArea();
        BigDecimal accurateArea = commonJsonParser.getOrganArea(jsonTask, "14D111").getStructureAreaNum();
        // 查询气管腔面积
        BigDecimal organArea = commonJsonParser.getOrganArea(jsonTask, "14D007").getStructureAreaNum();

        // 查询黏膜上皮层
        Annotation annotation = commonJsonParser.getOrganArea(jsonTask, "14D035");

        // 获取黏膜上皮层面积
        BigDecimal mucosaArea = annotation.getStructureAreaNum();

        // 获取黏膜上皮层周长
        BigDecimal mucosaPerimeter = annotation.getStructurePerimeterNum();

        // 获取黏膜上皮细胞核数量
        Integer mucosaCount = commonJsonParser.getOrganAreaCount(jsonTask, "14D036");

        // 查询软骨面积
        BigDecimal cartilageArea = commonJsonParser.getOrganArea(jsonTask, "14D00B").getStructureAreaNum();



        // 黏膜上皮层平均厚度
        BigDecimal multiplyArea2 = mucosaArea.multiply(BigDecimal.valueOf(2)).setScale(3, BigDecimal.ROUND_HALF_UP);

        BigDecimal averageThicknessOfMucosalEpithelium = multiplyArea2.divide(mucosaPerimeter, 6, RoundingMode.HALF_UP);

        // 黏膜上皮细胞核密度
        Double nucleusDensityOfMucosalEpithelium = mucosaCount / Double.parseDouble(String.valueOf(mucosaArea));

        // 使用组织轮廓面积减去气管腔面积=气管面积
        BigDecimal areaNum = accurateArea.subtract(organArea);
        // 软骨面积占比
        BigDecimal cartilageAreas = commonJsonParser.getProportion(cartilageArea, areaNum);

        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();

        indicatorResultsMap.put("气管腔面积", new IndicatorAddIn("气管腔面积", String.valueOf(organArea.setScale(3, RoundingMode.HALF_UP)), SQ_MM, CommonConstant.NUMBER_1));
        indicatorResultsMap.put("黏膜上皮层面积", new IndicatorAddIn("黏膜上皮层面积", String.valueOf(mucosaArea.setScale(3, RoundingMode.HALF_UP)), SQ_MM, CommonConstant.NUMBER_1));
        indicatorResultsMap.put("黏膜上皮层周长", new IndicatorAddIn("黏膜上皮层周长", String.valueOf(mucosaPerimeter.setScale(3, RoundingMode.HALF_UP)), MM, CommonConstant.NUMBER_1));
        indicatorResultsMap.put("黏膜上皮细胞核数量", new IndicatorAddIn("黏膜上皮细胞核数量", String.valueOf(mucosaCount), PIECE, CommonConstant.NUMBER_1));
        indicatorResultsMap.put("软骨面积", new IndicatorAddIn("软骨面积", String.valueOf(cartilageArea.setScale(3, RoundingMode.HALF_UP)), SQ_MM, CommonConstant.NUMBER_1));
        indicatorResultsMap.put("组织轮廓面积", new IndicatorAddIn("组织轮廓面积", accurateArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1));

        indicatorResultsMap.put("黏膜上皮层平均厚度", new IndicatorAddIn("Average thickness of mucosal epithelium", areaUtils.convertToSquareMicrometer(String.valueOf(averageThicknessOfMucosalEpithelium)), UM));
        indicatorResultsMap.put("黏膜上皮细胞核密度", new IndicatorAddIn("Nucleus density of mucosal epithelium", String.valueOf(BigDecimal.valueOf(nucleusDensityOfMucosalEpithelium).setScale(3, RoundingMode.HALF_UP)), SQ_MM_PIECE));
        indicatorResultsMap.put("软骨面积占比", new IndicatorAddIn("Cartilage area%", String.valueOf(cartilageAreas.setScale(3, RoundingMode.HALF_UP)), PERCENTAGE));
        indicatorResultsMap.put("气管面积", new IndicatorAddIn("Tracheal area", String.valueOf(areaNum.setScale(3, RoundingMode.HALF_UP)), SQ_MM));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Trachea";
    }
}

