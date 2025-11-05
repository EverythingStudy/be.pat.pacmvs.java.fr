package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.OrganTagMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.ImageService;
import cn.staitech.fr.service.SlideService;
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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: wangfeng
 * @create: 2024-05-10 14:18:48
 * @Description: Json Parser 大鼠-内分泌系统-甲状腺 Thyroid_gland
 */
@Slf4j
@Component("Thyroid_gland")
public class ThyroidGlandParserStrategyImpl extends AbstractCustomParserStrategy {
    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private ImageService imageService;
    @Resource
    private SlideService slideService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private CommonJsonCheck commonJsonCheck;
    @Resource
    private AreaUtils areaUtils;
    @Resource
    private AnnotationMapper annotationMapper;

    @Resource
    private OrganTagMapper organTagMapper;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.debug("AdrenalGlandParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("指标计算开始-大鼠甲状腺");
        Map<String, IndicatorAddIn> map = new HashMap<>();
        // D 血管面积		103平方微米	若多个数据则相加输出
        BigDecimal vesselAreaD = getOrganAreaMicron(jsonTask, "107003");
        // E 血管内红细胞面积	E	平方微米	若多个数据则相加输出 (查询血管内红细胞面积)
        BigDecimal intravascularErythrocyteArea = getInsideOrOutside(jsonTask, "107003", "107004", true).getStructureAreaNum();
        // F 血管外红细胞面积	F	平方微米	若多个数据则相加输出 (查询血管外红细胞面积)
        BigDecimal extravascularErythrocyteArea = getInsideOrOutside(jsonTask, "107003", "107004", false).getStructureAreaNum();
        // G 肥大细胞数量	G	个
        Integer densityOfMastCells = getOrganAreaCount(jsonTask, "10708D");
        // H 滤泡上皮细胞核数量（单个）	H	个	单个甲状腺滤泡内细胞核数量
        Integer nucleusOfFollicular = getOrganAreaCount(jsonTask, "107089");
        // I 组织轮廓面积	I	平方毫米
        String accurateArea = singleSlideMapper.selectById(jsonTask.getSingleId()).getArea();
        BigDecimal accurateAreaDecimal = new BigDecimal(accurateArea);
        //BigDecimal accurateAreaDecimal = getOrganAreaMicron(jsonTask, "107111");

        // 计算置信区间和均数±标准差呈现  -------------------------------------------------------------
        // 1 甲状腺滤泡面积（单个）	1	103平方微米	Thyroid follicle area (per)	1=A	以95%置信区间和均数±标准差呈现
        List<BigDecimal> list1 = new ArrayList<>();

        // 2 甲状腺滤泡腔面积（单个）	2	103平方微米	Thyroid follicular lumen area (per)	2=B	以95%置信区间和均数±标准差呈现
        List<BigDecimal> list2 = new ArrayList<>();

        // 3 甲状腺滤泡上皮面积占比（单个）	3	%	Thyroid follicular epithelium area%(per)	3=(A-B)/A	以95%置信区间和均数±标准差呈现
        List<BigDecimal> list3 = new ArrayList<>();

        // 滤泡上皮细胞核密度（单个）	8	个/103平方微米	Nucleus density of follicular cell (per)	8=G/(A-B) 	以95%置信区间和均数±标准差呈现
        List<BigDecimal> list8 = new ArrayList<>();

        List<Annotation> structureContourList = getStructureContourList(jsonTask, "107088");

        if (CollectionUtils.isNotEmpty(structureContourList)) {
            for (Annotation annotation : structureContourList) {

                // A 甲状腺滤泡面积（单个）	A	103平方微米	单个甲状腺滤泡（107088）面积
                BigDecimal structureAreaNumA = annotation.getStructureAreaNum().multiply(new BigDecimal(1000));
                list1.add(structureAreaNumA);

                // 甲状腺滤泡内 滤泡腔
                Annotation contourInsideOrOutsideB = commonJsonParser.getContourInsideOrOutside(jsonTask, annotation.getContour(), "10708A", true);
                // 甲状腺滤泡内 滤泡上皮细胞核
                Annotation contourInsideOrOutsideG = commonJsonParser.getContourInsideOrOutside(jsonTask, annotation.getContour(), "107089", true);

                // B 甲状腺滤泡腔面积（单个）	B	103平方微米	若单个甲状腺滤泡内有多个滤泡腔，则相加输出 (true在里面）
                BigDecimal structureAreaNumB = contourInsideOrOutsideB.getStructureAreaNum().multiply(new BigDecimal(1000));
                list2.add(structureAreaNumB);

                // A-B
                BigDecimal subtractAB = structureAreaNumA.subtract(structureAreaNumB);
                // C 3=(A-B)/A 甲状腺滤泡上皮面积占比（单个）	3	%	Thyroid follicular epithelium area%(per)	3=(A-B)/A	以95%置信区间和均数±标准差呈现
                if (structureAreaNumA.compareTo(BigDecimal.ZERO) != 0) {
                    list3.add(subtractAB.divide(structureAreaNumA, 7, RoundingMode.HALF_UP).multiply(new BigDecimal(100)));

                }
                // H 滤泡上皮细胞核数量（单个）	H	个	单个甲状腺滤泡内细胞核数量
                Integer count = contourInsideOrOutsideG.getCount();

                // 8 滤泡上皮细胞核密度（单个）	8	个/103平方微米	Nucleus density of follicular cell (per)	8=H/(A-B) 	以95%置信区间和均数±标准差呈现
                if (subtractAB.compareTo(BigDecimal.ZERO) != 0) {
                    list8.add(new BigDecimal(count).divide(subtractAB, 7, RoundingMode.HALF_UP));
                }
            }
        }

        // A 滤泡面积（单个）
        Annotation annotationA = new Annotation();
        annotationA.setAreaName("甲状腺滤泡面积（单个）");
        annotationA.setAreaUnit(SQ_UM_THOUSAND);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "107088", annotationA, 1);
        // B 滤泡腔面积（单个）
        Annotation annotationByB = new Annotation();
        annotationByB.setAreaName("甲状腺滤泡腔面积（单个）");
        annotationByB.setAreaUnit(SQ_UM_THOUSAND);
        commonJsonParser.putAnnotationDynamicData(jsonTask, "107088", "10708A", annotationByB, 1);
        Annotation annotationC = new Annotation();
        annotationC.setAreaName("滤泡上皮面积占比（单个）");
        annotationC.setAreaUnit(SQ_UM_THOUSAND);
        commonJsonParser.putAnnotationDynamicData(jsonTask, "107088", "10708A", annotationC, 1);
        // H 滤泡上皮细胞核数量（单个）
        Annotation annotationByH = new Annotation();
        annotationByH.setCountName("滤泡上皮细胞核数量（单个）");
        annotationByH.setCountUnit("个");
        commonJsonParser.putAnnotationDynamicData(jsonTask, "107088", "107089", annotationByH);

        // D 血管面积 103 μm2
        map.put("血管面积", createIndicator(areaUtils.convertToSquareMicrometer(vesselAreaD.toString()), SQ_UM_THOUSAND, "107003"));
        // E 血管内红细胞面积 μm2
        map.put("血管内红细胞面积", createIndicator(areaUtils.convertToMicrometer(intravascularErythrocyteArea.toString()), SQ_MM, "107003,107004"));
        // F  血管外红细胞面积 μm2
        map.put("血管外红细胞面积", createIndicator(areaUtils.convertToMicrometer(extravascularErythrocyteArea.toString()), SQ_MM, "107003,107004"));
        // G 肥大细胞数量 个
        map.put("肥大细胞数量", createIndicator(densityOfMastCells.toString(), PIECE, "10708D"));

        // 产品呈现指标 -------------------------------------------------------------
        // 1 甲状腺滤泡面积（单个）		103平方微米	Thyroid follicle area (per)	1=A	以95%置信区间和均数±标准差呈现
        map.put("甲状腺滤泡面积（单个）", new IndicatorAddIn(MathUtils.getConfidenceInterval(list1), "Thyroid follicle area (per)", SQ_UM_THOUSAND, CommonConstant.NUMBER_0, "107088"));
        // 2 甲状腺滤泡腔面积（单个）	103平方微米	Thyroid follicular lumen area (per)	2=B	以95%置信区间和均数±标准差呈现
        map.put("甲状腺滤泡腔面积（单个）", new IndicatorAddIn(MathUtils.getConfidenceInterval(list2), "Thyroid follicular lumen area (per)", SQ_UM_THOUSAND, CommonConstant.NUMBER_0, "107088,10708A"));
        // 3 甲状腺滤泡上皮面积占比（单个）	%	Thyroid follicular epithelium area%(per)	3=(A-B)/A	以95%置信区间和均数±标准差呈现
        map.put("甲状腺滤泡上皮面积占比（单个）", new IndicatorAddIn(MathUtils.getConfidenceInterval(list3), "Thyroid follicular epithelium area%(per)", PERCENTAGE, CommonConstant.NUMBER_0, "107088,10708A"));
        //I 单位转化为平方微米
        BigDecimal hSubtractI = accurateAreaDecimal.multiply(new BigDecimal(1000000));
        if (hSubtractI.compareTo(BigDecimal.ZERO) != 0) {
            // 4 血管面积占比	%	Vessel area%	4=D/I 	运算前注意统一单位  D 103平方微米 I平方微米
            BigDecimal vesselAreaRate = getProportion(vesselAreaD.multiply(new BigDecimal(1000)), hSubtractI);
            map.put("血管面积占比", createNameIndicator("Vessel area", DecimalUtils.percentScale3(vesselAreaRate), PERCENTAGE, "107003,107111"));

            //5 血管内红细胞面积占比	%	Intravascular erythrocyte area%	5=E/I 	运算前注意统一单位
            BigDecimal intravascularErythrocyteAreaRate = intravascularErythrocyteArea.divide(hSubtractI, 7, RoundingMode.HALF_UP);
            map.put("血管内红细胞面积占比", createNameIndicator("Intravascular erythrocyte area%", DecimalUtils.percentScale3(intravascularErythrocyteAreaRate), PERCENTAGE, "107003,107004,107111"));

            // 6 血管外红细胞面积占比	%	Extravascular erythrocyte area%	6=F/I 	运算前注意统一单位
            BigDecimal extravascularErythrocyteAreaRate = extravascularErythrocyteArea.divide(hSubtractI, 7, RoundingMode.HALF_UP);
            map.put("血管外红细胞面积占比", createNameIndicator("Extravascular erythrocyte area%", DecimalUtils.percentScale3(extravascularErythrocyteAreaRate), PERCENTAGE, "107003,107004,107111"));

            // 7 肥大细胞密度		个/平方毫米	Density of mast cells	7=G/I 	运算前注意统一单位
            BigDecimal densityOfMastCellsRate = new BigDecimal(densityOfMastCells).divide(hSubtractI, 7, RoundingMode.HALF_UP);
            map.put("肥大细胞密度", createNameIndicator("Density of mast cells", DecimalUtils.setScale3(densityOfMastCellsRate), SQ_MM_PIECE, "10708D,107111"));
        } else {
            map.put("血管面积占比", createNameIndicator("Vessel area", "0.000", "%"));
            map.put("血管内红细胞面积占比", createNameIndicator("Intravascular erythrocyte area%", "0.000", PERCENTAGE));
            map.put("血管外红细胞面积占比", createNameIndicator("Extravascular erythrocyte area%", "0.000", PERCENTAGE));
            map.put("肥大细胞密度", createNameIndicator("Density of mast cells", "0.000", SQ_MM_PIECE));
        }
        // 8 滤泡上皮细胞核密度（单个）	个/103平方微米	Nucleus density of follicular cell (per)	8=H/(A-B) 	以95%置信区间和均数±标准差呈现
        map.put("滤泡上皮细胞核密度（单个）", new IndicatorAddIn(MathUtils.getConfidenceInterval(list8), "Nucleus density of follicular cell (per)", SQ_UM_THOUSAND, CommonConstant.NUMBER_0, "107088,107089,10708A"));

        // 9 甲状腺面积		平方毫米	Thyroid gland area	9=I	当前甲状腺面积是甲状腺和甲状旁腺的面积总和
        map.put("甲状腺面积", createNameIndicator("Thyroid gland area", DecimalUtils.setScale3(accurateAreaDecimal), SQ_MM, "107111"));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
        log.info("指标计算结束-大鼠甲状腺");
    }

    @Override
    public String getAlgorithmCode() {
        return "Thyroid_gland";
    }
}
