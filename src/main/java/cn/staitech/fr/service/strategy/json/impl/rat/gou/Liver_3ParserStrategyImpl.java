package cn.staitech.fr.service.strategy.json.impl.rat.gou;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
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
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 狗 肝脏
 */
@Slf4j
@Component
public class Liver_3ParserStrategyImpl extends AbstractCustomParserStrategy {
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
    @Resource
    private AreaUtils areaUtils;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("Liver_3ParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("狗肝脏结构指标计算开始：");

        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        String accurateAreaStr = singleSlideMapper.selectById(jsonTask.getSingleId()).getArea();
        BigDecimal accurateArea = new BigDecimal(accurateAreaStr);

        // 获取各项指标
        BigDecimal portalAreaPer = commonJsonParser.getOrganAreaMicron(jsonTask, "312145"); // A: 门管区面积（单个）
        Integer bileDuctCountPer = commonJsonParser.getOrganAreaCount(jsonTask, "31214A"); // B: 胆管数量（单个门管区）
        BigDecimal bileDuctAreaPer = commonJsonParser.getOrganArea(jsonTask, "31214A").getStructureAreaNum(); // C: 胆管面积（单个门管区）


        // 获取门管区轮廓
        List<Annotation> portalList = commonJsonParser.getStructureContourList(jsonTask, "312145");

        /**
         * 计算门管区相关指标
         * 遍历所有门管区，计算每个门管区内的胆管密度和胆管面积占比
         *
         * 计算逻辑：
         * 1. 胆管密度 = 胆管数量 / (门管区面积 × 1000)，单位：个/10³平方微米
         * 2. 胆管面积占比 = (胆管面积 / 门管区面积) × 100%，单位：%
         *
         * @param portalList 门管区标注列表，包含每个门管区的轮廓和面积信息
         * @param jsonTask JSON 任务对象，包含任务 ID、机构 ID 等上下文信息
         * @return portalDensityPer 胆管密度列表（个/10³平方微米）
         * @return portalDensityAreaPer 胆管面积占比列表（%）
         */
        List<BigDecimal> portalDensityAreaPer = new ArrayList<>();
        List<BigDecimal> portalDensityPer = new ArrayList<>();

        /*
         * 处理门管区数据集合
         * 当门管区列表非空时，遍历每个门管区并提取其内部的胆管数据
         */
        if (CollectionUtils.isNotEmpty(portalList)) {
            for (Annotation annotation : portalList) {
                // 获取单个门管区的面积（单位：10³平方微米）
                BigDecimal singlePortalAreaNum = annotation.getStructureAreaNum();

                /*
                 * 根据门管区轮廓查询其内部的胆管标注数据
                 * 参数说明：
                 * - jsonTask: 任务上下文
                 * - annotation.getContour(): 门管区轮廓坐标
                 * - "31214A": 胆管结构代码
                 * - true: 查询轮廓内部的数据
                 */
                Annotation bileDuctData = commonJsonParser.getContourInsideOrOutside(jsonTask, annotation.getContour(), "31214A", true);

                // 获取胆管数量（单个门管区内的胆管个数）
                Integer bileDuctCount = bileDuctData.getCount();

                // 获取胆管总面积（单个门管区内所有胆管面积之和，单位：10³平方微米）
                BigDecimal bileDuctArea = bileDuctData.getStructureAreaNum();

                /*
                 * 有效性校验与指标计算
                 * 仅当门管区面积不为 0 时进行计算，避免除零异常
                 * 计算结果分别添加到对应的列表中供后续统计分析使用
                 */
                if (singlePortalAreaNum.compareTo(BigDecimal.ZERO) != 0) {
                    /*
                     * 计算胆管密度（个/10³平方微米）
                     * 公式：胆管数量 ÷ (门管区面积 × 1000)
                     * 注意：此处将面积单位从 10³平方微米转换为平方微米进行归一化
                     */
                    portalDensityPer.add(commonJsonParser.bigDecimalDivideCheck(new BigDecimal(bileDuctCount), singlePortalAreaNum.multiply(new BigDecimal(1000))));

                    /*
                     * 计算胆管面积占比（%）
                     * 公式：(胆管面积 ÷ 门管区面积) × 100%
                     * 该方法已内置百分比转换和精度处理
                     */
                    portalDensityAreaPer.add(commonJsonParser.getProportion(bileDuctArea, singlePortalAreaNum));
                }
            }
        }

        //胆管密度（单个）置信区间
        String portalDensityPerConfidenceInterval = MathUtils.getConfidenceInterval(portalDensityPer);
        //胆管面积占比（单个）置信区间
        String portalDensityAreaPerConfidenceInterval = MathUtils.getConfidenceInterval(portalDensityAreaPer);

        /*
         * 处理肝细胞核数据并计算面积置信区间
         *
         * 处理流程：
         * 1. 查询所有肝细胞核的轮廓标注数据（结构代码：312149）
         * 2. 提取每个肝细胞核的面积值并收集到列表中
         * 3. 基于样本数据计算 95% 置信区间和均数±标准差
         *
         * 统计指标说明：
         * - 置信区间：反映肝细胞核面积的离散程度和可靠性
         * - 均数±标准差：描述肝细胞核面积的集中趋势和变异程度
         */

        /*
         * 获取肝细胞核轮廓列表
         * 参数 "312149" 代表肝细胞核的结构标识代码
         */
        List<Annotation> hepatocyteNucleusList = commonJsonParser.getStructureContourList(jsonTask, "312149");

        /*
         * 存储所有肝细胞核的面积数据
         * 用于后续的统计学分析
         */
        List<BigDecimal> hepatocyteNucleusAreaList = new ArrayList<>();

        /*
         * 遍历肝细胞核列表，提取每个肝细胞核的面积值
         * 面积单位：10³平方微米
         */
        for (Annotation annotation : hepatocyteNucleusList) {
            hepatocyteNucleusAreaList.add(annotation.getStructureAreaNum());
        }

        /*
         * 计算肝细胞核面积的置信区间
         * 该方法返回格式：包含 95% 置信区间和均数±标准差的字符串
         * 用于产品端展示肝细胞核面积的统计学特征
         */
        String hepatocyteNucleusAreaConfidenceInterval = MathUtils.getConfidenceInterval(hepatocyteNucleusAreaList);

        BigDecimal centralVeinArea = commonJsonParser.getOrganAreaMicron(jsonTask, "312146"); // D: 中央静脉面积
        BigDecimal venaCavaArea = commonJsonParser.getOrganAreaMicron(jsonTask, "312147"); // E: 大静脉面积
        Integer hepatocyteNucleusCount = commonJsonParser.getOrganAreaCount(jsonTask, "312149"); // F: 肝细胞核数量
        //BigDecimal hepatocyteNucleusAreaPer = commonJsonParser.getOrganArea(jsonTask, "312149").getStructureAreaNum(); // G: 肝细胞核面积（单个）
        Integer sinusNucleusCount = commonJsonParser.getOrganAreaCount(jsonTask, "31214D"); // H: 窦内细胞核数量
        BigDecimal totalPortalArea = commonJsonParser.getOrganAreaMicron(jsonTask, "312145"); // L: 门管区面积（全片）
        Integer totalBileDuctCount = commonJsonParser.getOrganAreaCount(jsonTask, "31214A"); // J: 胆管数量（全片）
        BigDecimal totalBileDuctArea = commonJsonParser.getOrganAreaMicron(jsonTask, "31214A"); // K: 胆管面积（全片）

        // 算法输出指标
        //indicatorResultsMap.put("门管区面积（单个）", new IndicatorAddIn("", DecimalUtils.setScale3(portalAreaPer), MULTIPLIED_SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "312145"));
        Annotation annotationC = new Annotation();
        annotationC.setAreaName("门管区面积（单个）");
        annotationC.setAreaUnit(MULTIPLIED_SQ_UM_THOUSAND);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "312145", annotationC, 1);

        /*indicatorResultsMap.put("胆管数量（单个门管区）", new IndicatorAddIn("", bileDuctCountPer.toString(), PIECE, CommonConstant.NUMBER_1, "31214A"));
        indicatorResultsMap.put("胆管面积（单个门管区）", new IndicatorAddIn("", DecimalUtils.setScale3(bileDuctAreaPer), SQ_MM, CommonConstant.NUMBER_1, "31214A"));*/

        Annotation annotationA = new Annotation();
        annotationA.setCountName("胆管数量（单个门管区）");
        annotationA.setCountUnit(PIECE);
        annotationA.setAreaName("胆管面积（单个门管区）");
        annotationA.setAreaUnit(SQ_MM);
        commonJsonParser.putAnnotationDynamicData(jsonTask, "312145", "31214A", annotationA, 1,true);

        indicatorResultsMap.put("中央静脉面积", new IndicatorAddIn("", DecimalUtils.setScale3(centralVeinArea), MULTIPLIED_SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "312146"));
        indicatorResultsMap.put("大静脉面积", new IndicatorAddIn("", DecimalUtils.setScale3(venaCavaArea), MULTIPLIED_SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "312147"));
        indicatorResultsMap.put("肝细胞核数量", new IndicatorAddIn("", hepatocyteNucleusCount.toString(), PIECE, CommonConstant.NUMBER_1, "312149"));
        //indicatorResultsMap.put("肝细胞核面积（单个）", new IndicatorAddIn("", DecimalUtils.setScale3(hepatocyteNucleusAreaPer), SQ_MM, CommonConstant.NUMBER_1, "312149"));
        Annotation annotationB = new Annotation();
        annotationB.setAreaName("肝细胞核面积（单个）");
        annotationB.setAreaUnit(SQ_MM);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "312149", annotationB, 1);

        indicatorResultsMap.put("窦内细胞核数量", new IndicatorAddIn("", sinusNucleusCount.toString(), PIECE, CommonConstant.NUMBER_1, "31214D"));
        //indicatorResultsMap.put("组织轮廓面积", new IndicatorAddIn("", DecimalUtils.setScale3(accurateArea), "平方毫米", CommonConstant.NUMBER_1, "312111"));
        indicatorResultsMap.put("胆管数量（全片）", new IndicatorAddIn("", totalBileDuctCount.toString(), PIECE, CommonConstant.NUMBER_1, "31214A"));
        indicatorResultsMap.put("胆管面积（全片）", new IndicatorAddIn("", DecimalUtils.setScale3(totalBileDuctArea), MULTIPLIED_SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "31214A"));
        indicatorResultsMap.put("门管区面积（全片）", new IndicatorAddIn("", DecimalUtils.setScale3(totalPortalArea), MULTIPLIED_SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "312145"));

        // 产品呈现指标
        indicatorResultsMap.put("肝脏面积", new IndicatorAddIn("Liver area", DecimalUtils.setScale3(accurateArea), SQ_MM, "312111"));

        if (portalAreaPer.compareTo(BigDecimal.ZERO) != 0) {
            // 胆管密度（单个）= B / A
            BigDecimal bileDuctDensityPer = commonJsonParser.bigDecimalDivideCheck(new BigDecimal(bileDuctCountPer), portalAreaPer.multiply(new BigDecimal(1000)));
            indicatorResultsMap.put("胆管密度（单个）", new IndicatorAddIn("Density of bile duct (per)", portalDensityPerConfidenceInterval, SQ_UM_PICE, areaUtils.getStructureIds("312145", "31214A")));

            // 胆管面积占比（单个）= C / A
            BigDecimal bileDuctAreaRatioPer = commonJsonParser.getProportion(bileDuctAreaPer, portalAreaPer);
            indicatorResultsMap.put("胆管面积占比（单个）", new IndicatorAddIn("Bile duct area% (per)", portalDensityAreaPerConfidenceInterval, PERCENTAGE, areaUtils.getStructureIds("312145", "31214A")));
        }

        if (accurateArea.compareTo(BigDecimal.ZERO) != 0) {
            // 静脉面积占比 = (D + E) / I
            BigDecimal veinAreaRatio = commonJsonParser.getProportion(centralVeinArea.add(venaCavaArea), accurateArea.multiply(new BigDecimal(1000)));
            indicatorResultsMap.put("静脉面积占比", new IndicatorAddIn("Vein area%", DecimalUtils.setScale3(veinAreaRatio), PERCENTAGE, areaUtils.getStructureIds("312146", "312147", "312111")));

            // 肝细胞核密度 = F / I
            BigDecimal hepatocyteNucleusDensity = commonJsonParser.bigDecimalDivideCheck(new BigDecimal(hepatocyteNucleusCount), accurateArea);
            indicatorResultsMap.put("肝细胞核密度", new IndicatorAddIn("Nucleus density of hepatocyte", DecimalUtils.setScale3(hepatocyteNucleusDensity), SQ_MM_PIECE, areaUtils.getStructureIds("312149", "312111")));

            // 窦内细胞核密度 = H / (I - L - D - E)
            BigDecimal sinusNucleusDensityDenominator = accurateArea.subtract(totalPortalArea.divide(new BigDecimal(1000)))
                    .subtract(centralVeinArea.divide(new BigDecimal(1000)))
                    .subtract(venaCavaArea.divide(new BigDecimal(1000)));
            BigDecimal sinusNucleusDensity = commonJsonParser.bigDecimalDivideCheck(new BigDecimal(sinusNucleusCount), sinusNucleusDensityDenominator);
            indicatorResultsMap.put("窦内细胞核密度", new IndicatorAddIn("Nucleus density of Sinus cell", DecimalUtils.setScale3(sinusNucleusDensity), SQ_MM_PIECE, areaUtils.getStructureIds("31214D", "312111")));

            // 胆管密度（全片）= J / I
            BigDecimal totalBileDuctDensity = commonJsonParser.bigDecimalDivideCheck(new BigDecimal(totalBileDuctCount), accurateArea);
            indicatorResultsMap.put("胆管密度（全片）", new IndicatorAddIn("Density of bile duct (all)", DecimalUtils.setScale3(totalBileDuctDensity), SQ_MM_PIECE, areaUtils.getStructureIds("31214A", "312111")));

            // 胆管面积占比（全片）= K / I
            BigDecimal totalBileDuctAreaRatio = commonJsonParser.getProportion(totalBileDuctArea, accurateArea.multiply(new BigDecimal(1000)));
            indicatorResultsMap.put("胆管面积占比（全片）", new IndicatorAddIn("Bile duct area% (all)", DecimalUtils.setScale3(totalBileDuctAreaRatio), PERCENTAGE, areaUtils.getStructureIds("31214A", "312111")));
        }

        indicatorResultsMap.put("肝细胞核面积（单个）", new IndicatorAddIn("Hepatocyte nucleus area (per)", hepatocyteNucleusAreaConfidenceInterval, SQ_UM, CommonConstant.NUMBER_0, "312149"));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
        log.info("狗肝脏结构指标计算结束");
    }

    @Override
    public String getAlgorithmCode() {
        return "Liver_3";
    }
}
