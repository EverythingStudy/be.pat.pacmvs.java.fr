package cn.staitech.fr.service.strategy.json.impl.dog.neural;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author wanglibei
 * @version V1.0
 * @ClassName: SpinalCordParserStrategyImpl
 * @Description-d:脊髓
 * @date 2025年7月22日
 */
@Slf4j
@Component("Spinal_cord_3")
public class CanidaeSpinalCordParserStrategyImpl extends AbstractCustomParserStrategy {
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private AreaUtils areaUtils;
    @Resource
    private CommonJsonCheck commonJsonCheck;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("SpinalCordParserStrategyImpl init");
    }


    @Override
    public void alculationIndicators(JsonTask jsonTask) {

        log.info("大鼠脊髓构指标计算开始");

        //G 灰质面积（全片）	G	mm2
        BigDecimal bigDecimalA = commonJsonParser.getOrganArea(jsonTask, "3390B3").getStructureAreaNum();
        //H 白质面积（全片）	H	mm2	已扣除灰质
        BigDecimal bigDecimalB = commonJsonParser.getOrganArea(jsonTask, "3390B2").getStructureAreaNum();

        //I 中央管面积（全片）	I	103 mm2
        BigDecimal bigDecimalC = commonJsonParser.getOrganArea(jsonTask, "3390B4").getStructureAreaNum();

        //J 室管膜细胞核数量（全片）	D	个	单个脊髓内数据相加输出
        //Integer mucosaCountD = commonJsonParser.getOrganAreaCount(jsonTask, "3390B5");
        //K	红细胞面积（全片）	mm2
        BigDecimal bigDecimalE = commonJsonParser.getOrganArea(jsonTask, "339004").getStructureAreaNum();
        //L	组织轮廓面积（全片）	mm2
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());
        BigDecimal bigDecimalF = new BigDecimal(slideArea);

        Map<String, IndicatorAddIn> indicatorResultsMap = new LinkedHashMap<>();

        indicatorResultsMap.put("灰质面积（单个）", new IndicatorAddIn("", bigDecimalA.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1, "3390B3"));
        indicatorResultsMap.put("白质面积（单个）", new IndicatorAddIn("", bigDecimalB.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "1", "3390B2"));
        indicatorResultsMap.put("中央管面积（单个）", new IndicatorAddIn("", areaUtils.convertToSquareMicrometer(bigDecimalC.toString()), MULTIPLIED_SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "3390B4"));
        //indicatorResultsMap.put("室管膜细胞核数量（单个）", new IndicatorAddIn("", mucosaCountD.toString(), PIECE, "1",areaUtils.getStructureIds("3390B4","3390B5")));
        indicatorResultsMap.put("红细胞面积（单个）", new IndicatorAddIn("", bigDecimalE.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1, "339004"));


        //灰质面积占比（全片）7=G/(G+H)   Gray matter area（all）
        BigDecimal af1 = commonJsonParser.getProportion(bigDecimalA, bigDecimalF);
        indicatorResultsMap.put("灰质面积占比（单个）", new IndicatorAddIn("Gray matter area（per）", String.valueOf(af1), PERCENTAGE, CommonConstant.NUMBER_0, "3390B3,339111"));

        //白质面积占比（全片）White matter area（all）  8=H/(G+H)
        BigDecimal bf2 = commonJsonParser.getProportion(bigDecimalB, bigDecimalF);
        indicatorResultsMap.put("白质面积占比（单个）", new IndicatorAddIn("White matter area（per）", String.valueOf(bf2), PERCENTAGE, CommonConstant.NUMBER_0, "3390B2,339111"));

        //中央管面积占比（全片）Central canal area（all）9=I/(G+H)
        BigDecimal ca3 = commonJsonParser.getProportion(bigDecimalC, bigDecimalA);
        indicatorResultsMap.put("中央管面积占比（单个）", new IndicatorAddIn("Central canal area（per）", String.valueOf(ca3), PERCENTAGE, CommonConstant.NUMBER_0, "3390B3,3390B4"));

        //红细胞面积占比（单个）
        BigDecimal ef5 = commonJsonParser.getProportion(bigDecimalE, bigDecimalF);

        indicatorResultsMap.put("红细胞面积占比（单个）", new IndicatorAddIn("Erythrocyte area%（per）", String.valueOf(ef5), PERCENTAGE, CommonConstant.NUMBER_0, "339004,339111"));

        //	脊髓面积	6	平方毫米	Spinal cord area（all）	12=G+H
        indicatorResultsMap.put("脊髓面积（单个）", new IndicatorAddIn("Spinal cord area（per）", String.valueOf(bigDecimalF.setScale(3, RoundingMode.HALF_UP)), SQ_MM, CommonConstant.NUMBER_0, areaUtils.getStructureIds("339111")));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);

    }

    @Override
    public String getAlgorithmCode() {
        return "Spinal_cord_3";
    }
}
