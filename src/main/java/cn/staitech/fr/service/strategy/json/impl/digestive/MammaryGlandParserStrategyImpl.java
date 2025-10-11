package cn.staitech.fr.service.strategy.json.impl.digestive;

import cn.hutool.core.collection.CollectionUtil;
import cn.staitech.fr.constant.CommonConstant;
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
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        // 结构编码 皮肤 -------------------------------------------------------------
        // 结构	编码
        // 表皮角质层	121096
        // 表皮基底层+棘层+颗粒层	121097
        // 毛囊	121098
        // 皮脂腺	121099
        // 组织轮廓	121111
        //乳腺腺泡和导管数量
        Integer organAreaCount = commonJsonParser.getOrganAreaCount(jsonTask, "17A06C");
        //淋巴结A
        BigDecimal organAreaA = commonJsonParser.getOrganArea(jsonTask, "17A005").getStructureAreaNum();
        //皮肤B (皮肤面积	J	平方毫米	此数据使用乳腺中皮肤数据)
        BigDecimal organAreaJ = commonJsonParser.getOrganArea(jsonTask, "17A0C3").getStructureAreaNum();

        //f结缔组织面积
        //BigDecimal organArea2 = commonJsonParser.getOrganArea(jsonTask, "12303F").getStructureAreaNum();
        //I结缔组织面积
        //Integer organAreaCount2 = commonJsonParser.getOrganAreaCount(jsonTask, "1230C7");
        //H-组织轮廓
        BigDecimal h = new BigDecimal(singleSlide.getArea());

        // A皮肤-表皮角质层面积
        BigDecimal organArea3 = commonJsonParser.getOrganArea(jsonTask, "17A096").getStructureAreaNum();

        // B皮肤-表皮基底层+棘层+颗粒层面积
        BigDecimal organArea4 = commonJsonParser.getOrganArea(jsonTask, "17A097").getStructureAreaNum();
        // D皮肤-毛囊数量
        //Integer areaCount = commonJsonParser.getOrganAreaCount(jsonTask, "121098");
        // E皮肤-皮脂腺面积
        //BigDecimal organAreaE = commonJsonParser.getOrganAreaMicron(jsonTask, "121099");
        // F皮肤-皮脂腺数量
        //Integer organAreaCount1 = commonJsonParser.getOrganAreaCount(jsonTask, "121099");
        // H皮肤-毛囊面积（全片）
        //BigDecimal organAreaH = commonJsonParser.getOrganArea(jsonTask, "121098").getStructureAreaNum();
        //L乳腺腺泡/导管面积（全片）
        BigDecimal organArea1 = commonJsonParser.getOrganArea(jsonTask, "17A06C").getStructureAreaNum();
        // 毛囊密度 个/mm2 4=D/G
//        BigDecimal divide = new BigDecimal(0);
//        if (ObjectUtil.isNotEmpty(organAreaG) && !ObjectUtil.equals(organAreaG, new BigDecimal(0))) {
//            BigDecimal decimal = new BigDecimal(areaCount);
//            divide = decimal.divide(organAreaG, 3, RoundingMode.HALF_UP);
//        }
        // 皮脂腺密度 个/mm2 5=F/G
//        BigDecimal divide1 = new BigDecimal(0);
//        if (ObjectUtil.isNotEmpty(organAreaG) && !ObjectUtil.equals(organAreaG, new BigDecimal(0))) {
//            BigDecimal decimal = new BigDecimal(organAreaCount1);
//            divide1 = decimal.divide(organAreaG, 3, RoundingMode.HALF_UP);
//        }
        //c 12306C
        //map.put("乳腺腺泡和导管数量", createNameIndicator("Number of acinus and ducts", organAreaCount.toString(), PIECE, "12306C"));
        BigDecimal subtract = h.subtract(organAreaA).subtract(organAreaJ);
        if (subtract.signum() == 0) {
            //D/(H-A-B)
            map.put("乳腺腺泡和导管面积占比", createNameIndicator("Acinus and ducts area%", "0", PERCENTAGE, areaUtils.getStructureIds("17A111", "17A06C", "17A005", "17A0C3")));
            //(F-D)/(H-A-B)
//            map.put("结缔组织面积占比", new IndicatorAddIn("Connective tissue area%", "0", "%",areaUtils.getStructureIds("121099","121098","121098","123005","1230C3")));
        } else {
            BigDecimal divide2 = organArea1.divide(subtract, 7, RoundingMode.HALF_UP);
            map.put("乳腺腺泡和导管面积占比", createNameIndicator("Acinus and ducts area%", DecimalUtils.percentScale3(divide2), PERCENTAGE, areaUtils.getStructureIds("17A111", "17A06C", "17A005", "17A0C3")));
            //BigDecimal subtract1 = organArea2.subtract(organArea1);
//            map.put("结缔组织面积占比", new IndicatorAddIn("Connective tissue area%", DecimalUtils.percentScale3(subtract1.divide(subtract, 7, RoundingMode.HALF_UP)), "%",areaUtils.getStructureIds("121099","121098","121098","123005","1230C3")));
        }

        List<Annotation> structureContourList = commonJsonParser.getStructureContourList(jsonTask, "17A06C");
        List<BigDecimal> lists = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(structureContourList)) {
            for (Annotation annotation : structureContourList) {
                //G
                BigDecimal structureAreaNum = annotation.getStructureAreaNum();
                Annotation contourInsideOrOutside = commonJsonParser.getContourInsideOrOutside(jsonTask, annotation.getContour(), "1230C7", true);
                //E
                Integer count = contourInsideOrOutside.getCount();//数量

                if (structureAreaNum.signum() != 0) {
                    BigDecimal multiply = structureAreaNum.multiply(new BigDecimal(1000));
                    lists.add(new BigDecimal(count).divide(multiply, 10, RoundingMode.HALF_UP));
                }

            }
        }
        //String confidenceInterval = MathUtils.getConfidenceInterval(lists);
        //L       乳腺腺泡和导管面积（全片）        mm2        12306C        所有轮廓面积之和
        map.put("乳腺腺泡和导管面积（全片）", createIndicator(organArea1.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "17A06C"));
//        //O        乳腺腺泡和导管面积（单个）        103 μm2        12306C        单个轮廓分别计算面积        辅助二级指标11的计算，不显示在指标表格里，显示在单个“乳腺腺泡/导管”轮廓详情弹窗中        v2.6.1
//        map.put(" 乳腺腺泡和导管面积（单个）", createNameIndicator(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1, areaUtils.getStructureIds("12306C")));

        //E/G
//        map.put("腺泡或导管细胞核密度（单个）", new IndicatorAddIn("Nucleus density of acinus or ducts （per）", confidenceInterval, SQ_UM_PICE,areaUtils.getStructureIds("12306C","1230C7","12306C")));
        //BigDecimal divide2 = new BigDecimal(organAreaCount2).divide(organArea1, 3, RoundingMode.HALF_UP);
        //I/D
//        map.put("细胞核密度（全片）", new IndicatorAddIn("Nucleus density of mammary gland（all）", divide2.toString(), SQ_MM_PIECE,areaUtils.getStructureIds("12306C","1230C7","12306C")));
        //H-A-B
        map.put("乳腺面积", createNameIndicator("Mammary gland area", DecimalUtils.setScale3(h.subtract(organAreaA).subtract(organAreaJ)), SQ_MM, areaUtils.getStructureIds("17A111", "17A005", "17A0C3")));
        //a 123005
//        map.put("淋巴结面积", new IndicatorAddIn("Lymph node area", DecimalUtils.setScale3(organAreaA), SQ_MM, CommonConstant.NUMBER_1,"123005"));
        // 皮肤面积	G	平方毫米	此数据使用乳腺中皮肤数据 (乳腺皮肤公用)
        //b
//        map.put("皮肤面积", new IndicatorAddIn("Skin area", DecimalUtils.setScale3(organAreaB), SQ_MM, CommonConstant.NUMBER_1,"1230C3"));
        //c
        map.put("乳腺腺泡和导管数量", new IndicatorAddIn("Number of acinus and ducts", organAreaCount.toString(), "个", CommonConstant.NUMBER_1, "17A06C"));
        //d
        //map.put("腺泡和导管面积（全片）", new IndicatorAddIn("Breast acinar/ductal area (all)", DecimalUtils.setScale3(organArea1), SQ_MM, CommonConstant.NUMBER_1, "12306C"));
        //f
//        map.put("结缔组织面积", new IndicatorAddIn("Connective tissue area", DecimalUtils.setScale3(organArea2), SQ_MM, CommonConstant.NUMBER_1,"12303F"));
        //h
//        map.put("组织轮廓面积", new IndicatorAddIn("Organizational contour area", DecimalUtils.setScale3(h), SQ_MM, CommonConstant.NUMBER_1,"123111"));
        //i
//        map.put("细胞核数量（全片）", new IndicatorAddIn("Number of breast cell nuclei (all)", organAreaCount2.toString(), "个", CommonConstant.NUMBER_1,areaUtils.getStructureIds("12306C","1230C7")));
        Annotation annotation1 = new Annotation();
        annotation1.setAreaName("乳腺腺泡/导管面积（单个）");
        annotation1.setAreaUnit(SQ_UM_THOUSAND);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "17A06C", annotation1, 1);
        //g
        map.put("腺泡和导管面积（单个）", new IndicatorAddIn("17A06C"));
        //e
//        map.put("细胞核数量（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1,areaUtils.getStructureIds("12306C","1230C7")));
//        Annotation annotationBy = new Annotation();
//        annotationBy.setCountName("乳腺细胞核数量（单个）");
//        commonJsonParser.putAnnotationDynamicData(jsonTask, "17A06C", "1230C7", annotationBy);

        // 算法输出指标 皮肤 -------------------------------------------------------------
        // 算法输出指标	指标代码（仅限本文档）	单位（保留小数点后三位）	备注
        // 表皮角质层面积	A	平方毫米	若多个数据则相加输出
        //
//        map.put("表皮角质层面积", new IndicatorAddIn("Epidermal stratum corneum area", DecimalUtils.setScale3(organArea3), SQ_MM, CommonConstant.NUMBER_1));
        map.put("表皮角质层面积", createIndicator(DecimalUtils.setScale3(organArea3).toString(), SQ_MM, "17A096"));
        // 表皮基底层+棘层+颗粒层面积	B	平方毫米	若多个数据则相加输出
//        map.put("表皮基底层+棘层+颗粒层面积", new IndicatorAddIn("Area of basal layer+spinous layer+granular layer of epidermis", DecimalUtils.setScale3(organArea4), SQ_MM, CommonConstant.NUMBER_1));
        map.put("表皮基底层+棘层+颗粒层面积", createIndicator(DecimalUtils.setScale3(organArea4).toString(), SQ_MM, "17A097"));

        // 毛囊面积（单个）	C	103平方微米	单个毛囊面积输出
        //  1：面积转10（3）平方微米  2:平方微米 （默认平方毫米）
//        Annotation annotationC = new Annotation();
//        annotationC.setAreaName("毛囊面积（单个）");
//        annotationC.setAreaUnit(SQ_UM_THOUSAND);
//        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "121098", annotationC, 1);
//        map.put("毛囊面积（单个）", new IndicatorAddIn());

        // 毛囊数量	D	个	无
//        map.put("毛囊数量", new IndicatorAddIn("Number of mucous sacs", areaCount.toString(), "个", CommonConstant.NUMBER_1));

        // 皮脂腺面积	E	103平方微米	数据相加输出
//        map.put("皮脂腺面积", new IndicatorAddIn("Sebaceous gland area", DecimalUtils.setScale3(organAreaE), SQ_UM_THOUSAND, CommonConstant.NUMBER_1));

        // 皮脂腺数量	F	个	无
//        map.put("皮脂腺数量", new IndicatorAddIn("Number of sebaceous glands", organAreaCount1.toString(), "个", CommonConstant.NUMBER_1));

        // 毛囊面积（全片）	H	平方毫米	无
//        map.put("毛囊面积（全片）", new IndicatorAddIn("Mucous sac area (all)", DecimalUtils.setScale3(organAreaH), SQ_MM, CommonConstant.NUMBER_1));

        // 产品呈现指标 皮肤 -------------------------------------------------------------

        // 毛囊面积（单个）	3	103平方微米	Hair follicle area（per）	3=C	以95%置信区间和均数±标准差呈现 均值±标准差；中间95%数据分布区间
//        List<Annotation> skinStructureContourList = commonJsonParser.getStructureContourList(jsonTask, "121098");
//        List<BigDecimal> skinLists = new ArrayList<>();
//        if (CollectionUtil.isNotEmpty(skinStructureContourList)) {
//            for (Annotation annotation : skinStructureContourList) {
//                // 默认平方毫米 转 103平方微米
//                BigDecimal areaNum = annotation.getStructureAreaNum().multiply(new BigDecimal(1000));
//                skinLists.add(areaNum);
//            }
//        }
        // String confidenceHairFollicleArea = MathUtils.getConfidenceInterval(skinLists);


        // 产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后三位）	English	计算方式	备注
        // if (organAreaG.compareTo(BigDecimal.ZERO) != 0) {
        // 表皮角质层面积占比	1	%	Stratum corneum area%	1=A/G
        BigDecimal stratumCorneumAreaRate = organArea3.divide(organAreaJ, 7, RoundingMode.HALF_UP);
        map.put("表皮角质层面积占比", createNameIndicator("Stratum corneum area%", DecimalUtils.percentScale3(stratumCorneumAreaRate), PERCENTAGE, "17A096,17A0C3"));

        // 表皮基底层+棘层+颗粒层面积占比	2	%	 Nucleated cell layer area%	2=B/G
        BigDecimal nucleatedCellLayerAreaRate = organArea4.divide(organAreaJ, 7, RoundingMode.HALF_UP);
        map.put("表皮基底层+棘层+颗粒层面积占比", createNameIndicator("Nucleated cell layer area%", DecimalUtils.percentScale3(nucleatedCellLayerAreaRate), PERCENTAGE, "17A097,17A0C3"));

        // 皮脂腺面积占比	6	%	Sebaceous glands area%	6=E/G	运算前注意统一单位
//            BigDecimal sebaceousGlandsAreaRate = organAreaE.divide(organAreaG.multiply(new BigDecimal(1000)), 7, RoundingMode.HALF_UP);
//            map.put("皮脂腺面积占比", new IndicatorAddIn("Sebaceous glands area%", DecimalUtils.percentScale3(sebaceousGlandsAreaRate), "%"));

        // 毛囊面积占比	7	%	Hair follicles area%	7=H/G
//            BigDecimal hairFolliclesAreaRate = organAreaH.divide(organAreaG, 7, RoundingMode.HALF_UP);
//            map.put("毛囊面积占比", new IndicatorAddIn("Hair follicles area%", DecimalUtils.percentScale3(hairFolliclesAreaRate), "%"));
//        } else {
//            map.put("表皮角质层面积占比", new IndicatorAddIn("Stratum corneum area%", "0", "%"));
//            map.put("表皮基底层+棘层+颗粒层面积占比", new IndicatorAddIn("Nucleated cell layer area%", "0", "%"));
//            map.put("皮脂腺面积占比", new IndicatorAddIn("Sebaceous glands area%", "0", "%"));
//            map.put("毛囊面积占比", new IndicatorAddIn("Hair follicles area%", "0", "%"));
        // }

        // 毛囊面积（单个）	3	103平方微米	Hair follicle area（per）	3=C	以95%置信区间和均数±标准差呈现
//        map.put("毛囊面积（单个）", new IndicatorAddIn("Hair follicle area（per）", confidenceHairFollicleArea, SQ_UM_THOUSAND));

        // 毛囊密度	4	个/平方毫米	Density of hair follicles 	4=D/G
//        map.put("毛囊密度", new IndicatorAddIn("Mucous sac density", divide.toString(), SQ_MM_PIECE));

        // 皮脂腺密度	5	个/平方毫米	Density of Sebaceous glands 	5=F/G
//        map.put("皮脂腺密度", new IndicatorAddIn("Sebaceous gland density", divide1.toString(), SQ_MM_PIECE));

        // 皮肤面积	8	平方毫米	Skin area	8=G  此数据使用乳腺中皮肤数据
        map.put("皮肤面积", createNameIndicator("Skin area", DecimalUtils.setScale3(organAreaJ), SQ_MM, "17A0C3"));


        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);

        log.info("指标计算结束-乳腺皮肤");
    }

    @Override
    public String getAlgorithmCode() {
        return "IntegumentMammaryGland";
    }
}
