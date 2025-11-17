package cn.staitech.fr.service.strategy.json.impl.digestive;

import cn.hutool.core.collection.CollectionUtil;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import cn.staitech.fr.utils.DecimalUtils;
import cn.staitech.fr.utils.MathUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wanglibei
 * @version V1.0
 * @ClassName: MammaryGlandParserStrategyImpl
 * @Description-d:大鼠-乳腺-皮肤
 * @date 2025年7月22日
 */
@Slf4j
@Component("IntegumentMammaryGland")
public class MammaryGlandParserStrategyImpl extends AbstractCustomParserStrategy {

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

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("MammaryGlandParserStrategyImpl init");
    }

    /**
     * 指标计算
     *
     * @param jsonTask
     */
    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("指标计算开始-乳腺皮肤");
        Map<String, IndicatorAddIn> map = new HashMap<>();

        // A皮肤-表皮角质层面积 mm2
        BigDecimal organArea3 = commonJsonParser.getOrganArea(jsonTask, "17A096").getStructureAreaNum();
        // B皮肤-表皮基底层+棘层+颗粒层面积 mm2
        BigDecimal organArea4 = commonJsonParser.getOrganArea(jsonTask, "17A097").getStructureAreaNum();

        // D皮肤-毛囊数量 个
        Integer areaCount = commonJsonParser.getOrganAreaCount(jsonTask, "17A098");
        // E皮肤-皮脂腺面积 103 μm2
        BigDecimal organAreaE = commonJsonParser.getOrganAreaMicron(jsonTask, "17A099");
        // F皮肤-皮脂腺数量 个
        Integer organAreaCount1 = commonJsonParser.getOrganAreaCount(jsonTask, "17A099");
        // H皮肤-毛囊面积（全片） mm2
        BigDecimal organAreaH = commonJsonParser.getOrganArea(jsonTask, "17A098").getStructureAreaNum();
        //I 淋巴结面积 mm2
        BigDecimal organAreaA = commonJsonParser.getOrganArea(jsonTask, "17A005").getStructureAreaNum();
        //J 皮肤面积	mm2
        BigDecimal organAreaJ = commonJsonParser.getOrganArea(jsonTask, "17A0C3").getStructureAreaNum();
        //K 乳腺腺泡和导管数量 个
        Integer organAreaCount = commonJsonParser.getOrganAreaCount(jsonTask, "17A06C");
        //L乳腺腺泡/导管面积（全片） mm2
        BigDecimal organArea1 = commonJsonParser.getOrganArea(jsonTask, "17A06C").getStructureAreaNum();
        //N 乳腺结缔组织面积 mm2
        BigDecimal organNArea = commonJsonParser.getOrganArea(jsonTask, "17A03F").getStructureAreaNum();
        //P 乳腺细胞核数量（全片） 个
        Integer areaPCount = commonJsonParser.getOrganAreaCount(jsonTask, "17A0C7");
        //Q 皮肤与乳腺面积 mm2
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal h = new BigDecimal(singleSlide.getArea());

        map.put("表皮角质层面积", createIndicator(DecimalUtils.setScale3(organArea3).toString(), SQ_MM, "17A096"));
        map.put("表皮基底层+棘层+颗粒层面积", createIndicator(DecimalUtils.setScale3(organArea4).toString(), SQ_MM, "17A097"));
        // C毛囊面积（单个） 103 μm2
        Annotation annotationC = new Annotation();
        annotationC.setAreaName("毛囊面积（单个）");
        annotationC.setAreaUnit(SQ_UM_THOUSAND);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "17A098", annotationC, 1);
        map.put("毛囊数量", createIndicator(areaCount.toString(), PIECE, "17A098"));
        map.put("皮脂腺面积", createIndicator(DecimalUtils.setScale3(organAreaE), SQ_UM_THOUSAND, "17A099"));
        // F 皮脂腺数量	F	个	无
        map.put("皮脂腺数量", createIndicator(organAreaCount1.toString(), PIECE, "17A099"));
        // H 毛囊面积（全片）	H	平方毫米	无
        map.put("毛囊面积（全片）", createIndicator(DecimalUtils.setScale3(organAreaH), SQ_MM, "17A098"));
        // L 乳腺腺泡和导管面积（全片）        mm2        12306C        所有轮廓面积之和
        map.put("乳腺腺泡和导管面积（全片）", createIndicator(organArea1.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "17A06C"));
        //M 乳腺细胞核数量（单个）个
        Annotation annotationM = new Annotation();
        annotationM.setCountName("乳腺细胞核数量（单个）");
        commonJsonParser.putAnnotationDynamicData(jsonTask, "17A06C", "17A0C7", annotationM);
        map.put("乳腺结缔组织面积", createIndicator(organNArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "17A03F"));
        //O 乳腺腺泡和导管面积（单个）
        Annotation annotationO = new Annotation();
        annotationO.setCountName("乳腺腺泡和导管面积（单个）");
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "17A06C", annotationO, 1);
        map.put("乳腺细胞核数量（全片）", createIndicator(areaPCount.toString(), PIECE, "17A06C,17A0C7"));

        if (organAreaJ.compareTo(new BigDecimal(0)) != 0) {
            // 1 表皮角质层面积占比	 %	Stratum corneum area%	1=A/J
            map.put("表皮角质层面积占比", createNameIndicator("Stratum corneum area%", getProportion(organArea3, organAreaJ), PERCENTAGE, "17A096,17A0C3"));
            // 2 表皮基底层+棘层+颗粒层面积占比	%	 Nucleated cell layer area%	2=B/J
            map.put("表皮基底层+棘层+颗粒层面积占比", createNameIndicator("Nucleated cell layer area%", getProportion(organArea4, organAreaJ), PERCENTAGE, "17A097,17A0C3"));
            // 4 毛囊密度  个/mm2	Density of hair follicles 	4=D/J
            map.put("毛囊密度", createNameIndicator("Mucous sac density", bigDecimalDivideCheck(new BigDecimal(areaCount), organAreaJ), SQ_MM_PIECE, "17A098,17A0C3"));
            // 5 皮脂腺密度	个/mm2	Density of Sebaceous glands 	5=F/J
            map.put("皮脂腺密度", createNameIndicator("Sebaceous gland density", bigDecimalDivideCheck(new BigDecimal(organAreaCount1), organAreaJ), SQ_MM_PIECE, "17A099,17A0C3"));
            // 6 皮脂腺面积占比 %	Sebaceous glands area%	6=E/J	运算前注意统一单位
            map.put("皮脂腺面积占比", createNameIndicator("Sebaceous glands area%", getProportion(organAreaE, new BigDecimal(areaUtils.convertToSquareMicrometer(organAreaJ.toString()))), PERCENTAGE, "121099,17A0C3"));
            // 7 毛囊面积占比	%	Hair follicles area%	7=H/J
            map.put("毛囊面积占比", createNameIndicator("Hair follicles area%", getProportion(organAreaH, organAreaJ), PERCENTAGE, "17A098,17A0C3"));
        }
        // 3 毛囊面积（单个）103平方微米	Hair follicle area（per）	3=C	以95%置信区间和均数±标准差呈现 均值±标准差；中间95%数据分布区间
        List<Annotation> skinStructureContourList = commonJsonParser.getStructureContourList(jsonTask, "17A098");
        List<BigDecimal> skinLists = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(skinStructureContourList)) {
            for (Annotation annotation : skinStructureContourList) {
                // 默认平方毫米 转 103平方微米
                BigDecimal areaNum = annotation.getStructureAreaNum().multiply(new BigDecimal(1000));
                skinLists.add(areaNum);
            }
        }
        map.put("毛囊面积（单个）", createNameIndicator("Hair follicle area（per）", MathUtils.getConfidenceInterval(skinLists), SQ_UM_THOUSAND, "17A098"));
        // 8 皮肤面积	 mm2	8=J
        map.put("皮肤面积", createNameIndicator("Skin area", organAreaJ, SQ_MM, "17A0C3"));
        // 9 乳腺腺泡和导管数量	 个	9=K
        map.put("乳腺腺泡和导管数量", createNameIndicator("Number of acinus and ducts", organAreaCount.toString(), PIECE, "17A06C"));
        //(Q-I-J)
        BigDecimal subtract = h.subtract(h).subtract(organAreaJ);
        if (subtract.compareTo(new BigDecimal(0)) != 0) {
            //10 乳腺腺泡和导管面积占比 %  10=L/(Q-I-J)
            map.put("乳腺腺泡和导管面积占比", createNameIndicator("Acinus and ducts area%", getProportion(organArea1, subtract), PERCENTAGE, areaUtils.getStructureIds("17A111", "17A06C", "17A005", "17A0C3")));
            //12 乳腺结缔组织面积占比 % 12=(N-L)/(Q-I-J)
            map.put("乳腺结缔组织面积占比", createNameIndicator("Connective tissue area%", getProportion(organNArea.subtract(organArea1), subtract), PERCENTAGE, areaUtils.getStructureIds("17A03F", "17A06C", "17A0C3", "17A005", "17A111")));
        }
        //11 乳腺腺泡或导管细胞核密度（单个）个/103 μm2  11=M/O
//        List<Annotation> structureContourList = commonJsonParser.getStructureContourList(jsonTask, "17A06C");
//        List<BigDecimal> lists = new ArrayList<>();
//        if (CollectionUtil.isNotEmpty(structureContourList)) {
//            for (Annotation annotation : structureContourList) {
//                //O 乳腺腺泡和导管面积（单个）
//                BigDecimal structureAreaNum = annotation.getStructureAreaNum();
//                //M 乳腺细胞核数量（单个）
//                Annotation contourInsideOrOutside = commonJsonParser.getContourInsideOrOutside(jsonTask, annotation.getContour(), "1230C7", true);
//                Integer count = contourInsideOrOutside.getCount();
//                if (structureAreaNum.signum() != 0) {
//                    BigDecimal multiply = structureAreaNum.multiply(new BigDecimal(1000));
//                    lists.add(new BigDecimal(count).divide(multiply, 10, RoundingMode.HALF_UP));
//                }
//
//            }
//        }
//        map.put("毛囊面积（单个）", createNameIndicator("Nucleus density of acinus or ducts （per）", MathUtils.getConfidenceInterval(lists), SQ_UM_PICE, "17A06C,17A0C7"));
        //14 乳腺面积 mm2 14=Q-I-J
        map.put("乳腺面积", createNameIndicator("Mammary gland area", subtract, SQ_MM, areaUtils.getStructureIds("17A111", "17A005", "17A0C3")));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);

        log.info("指标计算结束-乳腺皮肤");
    }

    @Override
    public String getAlgorithmCode() {
        return "IntegumentMammaryGland";
    }
}
