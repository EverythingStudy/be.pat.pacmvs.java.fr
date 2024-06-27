package cn.staitech.fr.service.strategy.json.impl.digestive;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
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
import cn.staitech.fr.utils.MathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
 * @Author wudi
 * @Date 2024/5/13 10:05
 * @desc 乳腺-皮肤
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

        //H-面积
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal h = new BigDecimal(0);
        if (ObjectUtil.isNotEmpty(singleSlide) && StringUtils.isNotEmpty(singleSlide.getArea())) {
            h = new BigDecimal(singleSlide.getArea());
        }

        // 结构编码 皮肤 -------------------------------------------------------------
        // 结构	编码
        // 表皮角质层	121096
        // 表皮基底层+棘层+颗粒层	121097
        // 毛囊	121098
        // 皮脂腺	121099
        // 组织轮廓	121111

        Integer organAreaCount = commonJsonParser.getOrganAreaCount(jsonTask, "12306C");
        //淋巴结A
        BigDecimal organAreaA = commonJsonParser.getOrganArea(jsonTask, "123005").getStructureAreaNum();
        //皮肤B
        BigDecimal organAreaB = commonJsonParser.getOrganArea(jsonTask, "1230C3").getStructureAreaNum();
        //D乳腺腺泡/导管面积（全片）
        BigDecimal organArea1 = commonJsonParser.getOrganArea(jsonTask, "12306C").getStructureAreaNum();
        //f结缔组织面积
        BigDecimal organArea2 = commonJsonParser.getOrganArea(jsonTask, "12303F").getStructureAreaNum();
        //I结缔组织面积
        Integer organAreaCount2 = commonJsonParser.getOrganAreaCount(jsonTask, "1230C7");
        // 表皮角质层面积
        BigDecimal organArea3 = commonJsonParser.getOrganArea(jsonTask, "121096").getStructureAreaNum();

        // 表皮基底层+棘层+颗粒层面积
        BigDecimal organArea4 = commonJsonParser.getOrganArea(jsonTask, "121097").getStructureAreaNum();
        // 毛囊数量
        Integer areaCount = commonJsonParser.getOrganAreaCount(jsonTask, "121098");
        // 皮脂腺面积
        BigDecimal organAreaE = commonJsonParser.getOrganAreaMicron(jsonTask, "121099");
        // 皮脂腺数量
        Integer organAreaCount1 = commonJsonParser.getOrganAreaCount(jsonTask, "121099");
        // 毛囊面积（全片）
        BigDecimal organAreaH = commonJsonParser.getOrganArea(jsonTask, "121098").getStructureAreaNum();
        // 毛囊密度
        BigDecimal divide = new BigDecimal(0);
        if (ObjectUtil.isNotEmpty(organAreaB) && !ObjectUtil.equals(organAreaB, new BigDecimal(0))) {
            BigDecimal decimal = new BigDecimal(areaCount);
            divide = decimal.divide(organAreaB, 3, RoundingMode.HALF_UP);
        }
        // 皮脂腺密度
        BigDecimal divide1 = new BigDecimal(0);
        if (ObjectUtil.isNotEmpty(organAreaB) && !ObjectUtil.equals(organAreaB, new BigDecimal(0))) {
            BigDecimal decimal = new BigDecimal(organAreaCount1);
            divide1 = decimal.divide(organAreaB, 3, RoundingMode.HALF_UP);
        }

        map.put("乳腺腺泡和导管数量", new IndicatorAddIn("Number of acinus and ducts", organAreaCount.toString(), "个"));
        BigDecimal subtract = h.subtract(organAreaA).subtract(organAreaB);
        if (subtract.signum() == 0) {
            map.put("乳腺腺泡和导管面积占比", new IndicatorAddIn("Acinus and ducts area%", "0.000", "%"));
            map.put("结缔组织面积占比", new IndicatorAddIn("Connective tissue area%", "0.000", "%"));

        } else {
            BigDecimal divide2 = organArea1.divide(subtract, 10, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(3);
            map.put("乳腺腺泡和导管面积占比", new IndicatorAddIn("Acinus and ducts area%", divide2.toString(), "%"));
            BigDecimal subtract1 = organArea2.subtract(organArea1);
            map.put("结缔组织面积占比", new IndicatorAddIn("Connective tissue area%", subtract1.divide(subtract, 10, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(3).toString(), "%"));

        }

        List<Annotation> structureContourList = commonJsonParser.getStructureContourList(jsonTask, "12306C");
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
        String confidenceInterval = MathUtils.getConfidenceInterval(lists);
        map.put("腺泡或导管细胞核密度（单个）", new IndicatorAddIn("Nucleus density of acinus or ducts （per）", confidenceInterval, "个/10³平方微米"));
        BigDecimal divide2 = new BigDecimal(organAreaCount2).divide(organArea1, 3, RoundingMode.HALF_UP);
        map.put("乳腺细胞核密度（全片）", new IndicatorAddIn("Nucleus density of mammary gland（all）", divide2.toString(), "个/平方毫米"));
        map.put("乳腺面积", new IndicatorAddIn("Mammary gland area", h.subtract(organAreaA).subtract(organAreaB).setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米"));

        map.put("淋巴结面积", new IndicatorAddIn("Lymph node area", organAreaA.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        // 皮肤面积	G	平方毫米	此数据使用乳腺中皮肤数据 (乳腺皮肤公用)
        map.put("皮肤面积", new IndicatorAddIn("Skin area", organAreaB.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        map.put("乳腺腺泡/导管面积（全片）", new IndicatorAddIn("Breast acinar/ductal area (all)", organArea1.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        map.put("结缔组织面积", new IndicatorAddIn("Connective tissue area", organArea2.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        map.put("组织轮廓面积", new IndicatorAddIn("Organizational contour area", h.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        map.put("乳腺细胞核数量（全片）", new IndicatorAddIn("Number of breast cell nuclei (all)", organAreaCount2.toString(), "个", CommonConstant.NUMBER_1));
        map.put("乳腺腺泡/导管面积（单个）", new IndicatorAddIn());

        map.put("乳腺细胞核数量（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1));
        Annotation annotationBy = new Annotation();
        annotationBy.setCountName("乳腺细胞核数量（单个）");
        commonJsonParser.putAnnotationDynamicData(jsonTask, "12306C", "1230C7", annotationBy);

        // 算法输出指标 皮肤 -------------------------------------------------------------
        // 算法输出指标	指标代码（仅限本文档）	单位（保留小数点后三位）	备注
        // 表皮角质层面积	A	平方毫米	若多个数据则相加输出
        map.put("表皮角质层面积", new IndicatorAddIn("Epidermal stratum corneum area", organArea3.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));

        // 表皮基底层+棘层+颗粒层面积	B	平方毫米	若多个数据则相加输出
        map.put("表皮基底层+棘层+颗粒层面积", new IndicatorAddIn("Area of basal layer+spinous layer+granular layer of epidermis", organArea4.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));

        // 毛囊面积（单个）	C	103平方微米	单个毛囊面积输出
        map.put("毛囊面积（单个）", new IndicatorAddIn());

        // 毛囊数量	D	个	无
        map.put("毛囊数量", new IndicatorAddIn("Number of mucous sacs", areaCount.toString(), "个", CommonConstant.NUMBER_1));

        // 皮脂腺面积	E	103平方微米	数据相加输出
        map.put("皮脂腺面积", new IndicatorAddIn("Sebaceous gland area", organAreaE.setScale(3, RoundingMode.HALF_UP).toString(), "10³平方微米", CommonConstant.NUMBER_1));

        // 皮脂腺数量	F	个	无
        map.put("皮脂腺数量", new IndicatorAddIn("Number of sebaceous glands", organAreaCount1.toString(), "个", CommonConstant.NUMBER_1));

        // 毛囊面积（全片）	H	平方毫米	无
        map.put("毛囊面积（全片）", new IndicatorAddIn("Mucous sac area (all)", organAreaH.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));

        // 产品呈现指标 皮肤 -------------------------------------------------------------

        // 毛囊面积（单个）	3	103平方微米	Hair follicle area（per）	3=C	以95%置信区间和均数±标准差呈现
        List<Annotation> skinStructureContourList = commonJsonParser.getStructureContourList(jsonTask, "121098");
        List<BigDecimal> skinLists = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(skinStructureContourList)) {
            for (Annotation annotation : skinStructureContourList) {
                // 默认平方毫米 转 103平方微米
                BigDecimal areaNum = annotation.getStructureAreaNum().multiply(new BigDecimal(1000));
                skinLists.add(areaNum);
            }
        }
        String confidenceHairFollicleArea = MathUtils.getConfidenceInterval(skinLists);

        // 产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后三位）	English	计算方式	备注

        if (organAreaB.compareTo(BigDecimal.ZERO) != 0) {
            // 表皮角质层面积占比	1	%	Stratum corneum area%	1=A/G
            String stratumCorneumAreaRate = organArea3.divide(organAreaB, 3, RoundingMode.HALF_UP).setScale(3, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(3).toString();
            map.put("表皮角质层面积占比", new IndicatorAddIn("Stratum corneum area%", stratumCorneumAreaRate, "%"));

            // 表皮基底层+棘层+颗粒层面积占比	2	%	 Nucleated cell layer area%	2=B/G
            String nucleatedCellLayerAreaRate = organArea4.divide(organAreaB, 3, RoundingMode.HALF_UP).setScale(3, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(3).toString();
            map.put("表皮基底层+棘层+颗粒层面积占比", new IndicatorAddIn("Nucleated cell layer area%", nucleatedCellLayerAreaRate, "%"));

            // 皮脂腺面积占比	6	%	Sebaceous glands area%	6=E/G	运算前注意统一单位
            String sebaceousGlandsAreaRate = organAreaE.divide(organAreaB, 3, RoundingMode.HALF_UP).setScale(3, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(3).toString();
            map.put("皮脂腺面积占比", new IndicatorAddIn("Sebaceous glands area%", sebaceousGlandsAreaRate, "%"));

            // 毛囊面积占比	7	%	Hair follicles area%	7=H/G
            String hairFolliclesAreaRate = organAreaH.divide(organAreaB, 3, RoundingMode.HALF_UP).setScale(3, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(3).toString();
            map.put("毛囊面积占比", new IndicatorAddIn("Hair follicles area%", hairFolliclesAreaRate, "%"));
        } else {
            map.put("表皮角质层面积占比", new IndicatorAddIn("Stratum corneum area%", "0.000", "%"));
            map.put("表皮基底层+棘层+颗粒层面积占比", new IndicatorAddIn("Nucleated cell layer area%", "0.000", "%"));
            map.put("皮脂腺面积占比", new IndicatorAddIn("Sebaceous glands area%", "0.000", "%"));
            map.put("毛囊面积占比", new IndicatorAddIn("Hair follicles area%", "0.000", "%"));
        }

        // 毛囊面积（单个）	3	103平方微米	Hair follicle area（per）	3=C	以95%置信区间和均数±标准差呈现
        map.put("毛囊面积（单个）", new IndicatorAddIn("Hair follicle area（per）", confidenceHairFollicleArea, "10³平方微米"));

        // 毛囊密度	4	个/平方毫米	Density of hair follicles 	4=D/G
        map.put("毛囊密度", new IndicatorAddIn("Mucous sac density", divide.toString(), "个/平方毫米"));

        // 皮脂腺密度	5	个/平方毫米	Density of Sebaceous glands 	5=F/G
        map.put("皮脂腺密度", new IndicatorAddIn("Sebaceous gland density", divide1.toString(), "个/平方毫米"));

        // 皮肤面积	8	平方毫米	Skin area	8=G  此数据使用乳腺中皮肤数据
        map.put("皮肤面积", new IndicatorAddIn("Skin area", organAreaB.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米"));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);

        log.info("指标计算结束-乳腺皮肤");
    }

    @Override
    public String getAlgorithmCode() {
        return "IntegumentMammaryGland";
    }
}
