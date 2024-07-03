package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.Category;
import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.CategoryMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.service.strategy.json.ParserStrategy;
import cn.staitech.fr.utils.AreaUtils;
import cn.staitech.fr.utils.DecimalUtils;
import cn.staitech.fr.utils.MathUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

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
 * @Description: Json Parser 大鼠甲状腺 Thyroid_gland
 */
@Slf4j
@Component("Thyroid_gland")
public class ThyroidGlandParserStrategyImpl implements ParserStrategy {
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
    @Resource
    private AnnotationMapper annotationMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Override
    public void parseJson(JsonTask jsonTask, JsonFile jsonFileS) {
        commonJsonParser.parseJson(jsonTask, jsonFileS);
    }

    @Override
    public boolean checkJson(JsonTask jsonTask, List<JsonFile> jsonFileList) {
        return commonJsonCheck.checkJson(jsonTask, jsonFileList);
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("指标计算开始-大鼠甲状腺");
        Map<String, IndicatorAddIn> map = new HashMap<>();
        //        甲状腺
        // 甲状腺滤泡	107088
        // 甲状腺滤泡腔	10708A
        // 血管	107003
        // 红细胞	107004
        // 肥大细胞	10708D
        // 滤泡上皮细胞核	107089
        // 组织轮廓	107111
        // 甲状旁腺组织轮廓	108111

        // 算法输出指标	指标代码（仅限本文档）	单位（保留小数点后三位）	备注
        // 甲状腺滤泡面积（单个）	A	103平方微米	单个甲状腺滤泡面积
        // 甲状腺滤泡腔面积（单个）	B	103平方微米	若单个甲状腺滤泡内有多个滤泡腔，则相加输出
        // 血管面积	C	103平方微米	若多个数据则相加输出
        BigDecimal vesselArea = commonJsonParser.getOrganAreaMicron(jsonTask, "107003");
        //        血管内红细胞面积	D	平方微米	若多个数据则相加输出 (查询血管内红细胞面积)
        BigDecimal intravascularErythrocyteArea = commonJsonParser.getInsideOrOutside(jsonTask, "107003", "107004", true).getStructureAreaNum();
        //        血管外红细胞面积	E	平方微米	若多个数据则相加输出 (查询血管外红细胞面积)
        BigDecimal extravascularErythrocyteArea = commonJsonParser.getInsideOrOutside(jsonTask, "107003", "107004", false).getStructureAreaNum();
        //        肥大细胞数量	F	个
        Integer densityOfMastCells = commonJsonParser.getOrganAreaCount(jsonTask, "10708D");
        //        滤泡上皮细胞核数量（单个）	G	个	单个甲状腺滤泡内细胞核数量
        Integer nucleusOfFollicular = commonJsonParser.getOrganAreaCount(jsonTask, "107089");
        //       组织轮廓面积	H	平方毫米	若多个数据则相加输出(H:精细轮廓总面积（甲状腺）-平方毫米  )
        String accurateArea = singleSlideMapper.selectById(jsonTask.getSingleId()).getArea();
        BigDecimal accurateAreaDecimal = new BigDecimal(accurateArea);

        // 甲状旁腺组织轮廓面积	I	103平方微米	若多个数据则相加输出(I:甲状旁腺组织轮廓面积-平方毫米)
        Annotation annotationI = new Annotation();
        annotationI.setSingleSlideId(jsonTask.getSingleId());
        LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        categoryLambdaQueryWrapper.eq(Category::getOrganEn, "Parathyroid").eq(Category::getSpecies, 1);
        Category category = categoryMapper.selectOne(categoryLambdaQueryWrapper);
        annotationI.setCategoryId(category.getCategoryId());

        // 查询轮廓内的轮廓总面积->平方微米|getSpinalCordAnno() 查询精细轮廓列表
        BigDecimal parathyroidGlandArea = new BigDecimal(0);
        if (annotationMapper.stUnionContourArea(annotationI) != null) {
            parathyroidGlandArea = new BigDecimal(annotationMapper.stUnionContourArea(annotationI).getArea());
        }

        log.info("甲状旁腺面积{} 平方微米", parathyroidGlandArea);

        // 计算置信区间和均数±标准差呈现  -------------------------------------------------------------
        // 甲状腺滤泡面积（单个）	1	103平方微米	Thyroid follicle area (per)	1=A	以95%置信区间和均数±标准差呈现
        List<BigDecimal> list1 = new ArrayList<>();

        // 甲状腺滤泡腔面积（单个）	2	103平方微米	Thyroid follicular lumen area (per)	2=B	以95%置信区间和均数±标准差呈现
        List<BigDecimal> list2 = new ArrayList<>();

        // 甲状腺滤泡上皮面积占比（单个）	3	%	Thyroid follicular epithelium area%(per)	3=(A-B)/A	以95%置信区间和均数±标准差呈现
        List<BigDecimal> list3 = new ArrayList<>();

        // 滤泡上皮细胞核密度（单个）	8	个/103平方微米	Nucleus density of follicular cell (per)	8=G/(A-B) 	以95%置信区间和均数±标准差呈现
        List<BigDecimal> list8 = new ArrayList<>();

        List<Annotation> structureContourList = commonJsonParser.getStructureContourList(jsonTask, "107088");

        if (CollectionUtils.isNotEmpty(structureContourList)) {
            for (Annotation annotation : structureContourList) {

                // 甲状腺滤泡面积（单个）	A	103平方微米	单个甲状腺滤泡（107088）面积
                BigDecimal structureAreaNumA = annotation.getStructureAreaNum().multiply(new BigDecimal(1000));
                list1.add(structureAreaNumA);

                // 甲状腺滤泡内 滤泡腔
                Annotation contourInsideOrOutsideB = commonJsonParser.getContourInsideOrOutside(jsonTask, annotation.getContour(), "10708A", true);
                // 甲状腺滤泡内 滤泡上皮细胞核
                Annotation contourInsideOrOutsideG = commonJsonParser.getContourInsideOrOutside(jsonTask, annotation.getContour(), "107089", true);

                // 甲状腺滤泡腔面积（单个）	B	103平方微米	若单个甲状腺滤泡内有多个滤泡腔，则相加输出 (true在里面）
                BigDecimal structureAreaNumB = contourInsideOrOutsideB.getStructureAreaNum().multiply(new BigDecimal(1000));
                list2.add(structureAreaNumB);

                // A-B
                BigDecimal subtractAB = structureAreaNumA.subtract(structureAreaNumB);

                // G 滤泡上皮细胞核数量（单个）	G	个	单个甲状腺滤泡内细胞核数量
                Integer count = contourInsideOrOutsideG.getCount();

                // 3=(A-B)/A 甲状腺滤泡上皮面积占比（单个）	3	%	Thyroid follicular epithelium area%(per)	3=(A-B)/A	以95%置信区间和均数±标准差呈现
                if (structureAreaNumA.compareTo(BigDecimal.ZERO) != 0) {
                    list3.add(subtractAB.divide(structureAreaNumA, 7, RoundingMode.HALF_UP).multiply(new BigDecimal(100)));
                    // list3.add(subtractAB.divide(structureAreaNumA, 7, RoundingMode.HALF_UP));
                }
                // 滤泡上皮细胞核密度（单个）	8	个/103平方微米	Nucleus density of follicular cell (per)	8=G/(A-B) 	以95%置信区间和均数±标准差呈现
                if (subtractAB.compareTo(BigDecimal.ZERO) != 0) {
                    list8.add(new BigDecimal(count).divide(subtractAB, 7, RoundingMode.HALF_UP));
                }
            }
        }

        String confidenceInterval1 = MathUtils.getConfidenceInterval(list1);
        String confidenceInterval2 = MathUtils.getConfidenceInterval(list2);
        String confidenceInterval3 = MathUtils.getConfidenceInterval(list3);
        String confidenceInterval8 = MathUtils.getConfidenceInterval(list8);

        // B
        Annotation annotationByB = new Annotation();
        annotationByB.setAreaName("甲状腺滤泡腔面积（单个）");
        annotationByB.setAreaUnit("×10³平方微米");
        commonJsonParser.putAnnotationDynamicData(jsonTask, "107088", "10708A", annotationByB, 1);
        // G
        Annotation annotationByG = new Annotation();
        annotationByG.setCountName("滤泡上皮细胞核数量（单个）");
        annotationByG.setCountUnit("个");
        commonJsonParser.putAnnotationDynamicData(jsonTask, "107088", "107089", annotationByG);

        // A
        //  1：面积转10（3）平方微米  2:平方微米 （默认平方毫米）
        Annotation annotationC = new Annotation();
        annotationC.setAreaName("甲状腺滤泡面积（单个）");
        annotationC.setAreaUnit("×10³平方微米");
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "107088", annotationC, 1);
        map.put("甲状腺滤泡面积（单个）", new IndicatorAddIn());
        // B
        map.put("甲状腺滤泡腔面积（单个）", new IndicatorAddIn());
        // C
        map.put("血管面积", new IndicatorAddIn("Vessel area%", DecimalUtils.setScale3(vesselArea), "×10³平方微米", CommonConstant.NUMBER_1));
        // D
        map.put("血管内红细胞面积", new IndicatorAddIn("Intravascular erythrocyte area", areaUtils.convertToMicrometer(intravascularErythrocyteArea.toString()), "平方微米", CommonConstant.NUMBER_1));
        // E
        map.put("血管外红细胞面积", new IndicatorAddIn("Extravascular erythrocyte area", areaUtils.convertToMicrometer(extravascularErythrocyteArea.toString()), "平方微米", CommonConstant.NUMBER_1));
        // F
        map.put("肥大细胞数量", new IndicatorAddIn("Density of mast cells", densityOfMastCells.toString(), "个", CommonConstant.NUMBER_1));
        // G
        map.put("滤泡上皮细胞核数量（单个）", new IndicatorAddIn());

        // 产品呈现指标 -------------------------------------------------------------
        // 甲状腺滤泡面积（单个）	1	103平方微米	Thyroid follicle area (per)	1=A	以95%置信区间和均数±标准差呈现
        map.put("甲状腺滤泡面积（单个）", new IndicatorAddIn("Thyroid follicle area (per)", confidenceInterval1, "×10³平方微米"));
        // 甲状腺滤泡腔面积（单个）	2	103平方微米	Thyroid follicular lumen area (per)	2=B	以95%置信区间和均数±标准差呈现
        map.put("甲状腺滤泡腔面积（单个）", new IndicatorAddIn("Thyroid follicular lumen area (per)", confidenceInterval2, "×10³平方微米"));
        // 甲状腺滤泡上皮面积占比（单个）	3	%	Thyroid follicular epithelium area%(per)	3=(A-B)/A	以95%置信区间和均数±标准差呈现
        map.put("甲状腺滤泡上皮面积占比（单个）", new IndicatorAddIn("Thyroid follicular epithelium area%(per)", confidenceInterval3, "%"));

        // H-I 平方毫米  accurateAreaDecimal（平方毫米）  parathyroidGlandArea（平方微米）->平方毫米
        BigDecimal hSubtractI = accurateAreaDecimal.subtract(parathyroidGlandArea.divide(new BigDecimal(1000000), 10, RoundingMode.HALF_UP));

        //        血管面积占比	4	%	Vessel area%	4=C/(H-I) 	运算前注意统一单位  C 103平方微米/平方毫米
        if (hSubtractI.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal vesselAreaRate = vesselArea.divide(hSubtractI.multiply(new BigDecimal(1000)), 7, RoundingMode.HALF_UP);
            map.put("血管面积占比", new IndicatorAddIn("Vessel area", DecimalUtils.percentScale3(vesselAreaRate), "%"));

            //        血管内红细胞面积占比	5	%	Intravascular erythrocyte area%	5=D/(H-I) 	运算前注意统一单位
            BigDecimal intravascularErythrocyteAreaRate = intravascularErythrocyteArea.divide(hSubtractI, 7, RoundingMode.HALF_UP);
            map.put("血管内红细胞面积占比", new IndicatorAddIn("Intravascular erythrocyte area%", DecimalUtils.percentScale3(intravascularErythrocyteAreaRate), "%"));

            //        血管外红细胞面积占比	6	%	Extravascular erythrocyte area%	6=E/(H-I) 	运算前注意统一单位
            BigDecimal extravascularErythrocyteAreaRate = extravascularErythrocyteArea.divide(hSubtractI, 7, RoundingMode.HALF_UP);
            map.put("血管外红细胞面积占比", new IndicatorAddIn("Extravascular erythrocyte area%", DecimalUtils.percentScale3(extravascularErythrocyteAreaRate), "%"));

            //        肥大细胞密度	7	个/平方毫米	Density of mast cells	7=F/(H-I) 	运算前注意统一单位
            BigDecimal densityOfMastCellsRate = new BigDecimal(densityOfMastCells).divide(hSubtractI, 7, RoundingMode.HALF_UP);
            map.put("肥大细胞密度", new IndicatorAddIn("Density of mast cells", DecimalUtils.setScale3(densityOfMastCellsRate), "个/平方毫米"));
        } else {
            map.put("血管面积占比", new IndicatorAddIn("Vessel area", "0.000", "%"));
            map.put("血管内红细胞面积占比", new IndicatorAddIn("Intravascular erythrocyte area%", "0.000", "%"));
            map.put("血管外红细胞面积占比", new IndicatorAddIn("Extravascular erythrocyte area%", "0.000", "%"));
            map.put("肥大细胞密度", new IndicatorAddIn("Density of mast cells", "0.000", "个/平方毫米"));
        }

        // 滤泡上皮细胞核密度（单个）	8	个/103平方微米	Nucleus density of follicular cell (per)	8=G/(A-B) 	以95%置信区间和均数±标准差呈现
        map.put("滤泡上皮细胞核密度（单个）", new IndicatorAddIn("Nucleus density of follicular cell (per)", confidenceInterval8, "个/10³平方微米"));

        // H 甲状腺面积	9	平方毫米	Thyroid gland area	9=H	当前甲状腺面积是甲状腺和甲状旁腺的面积总和
        map.put("甲状腺面积", new IndicatorAddIn("Thyroid gland area", DecimalUtils.setScale3(accurateAreaDecimal), "平方毫米"));

        // 甲状旁腺组织轮廓面积	I	103平方微米	若多个数据则相加输出
        // 甲状旁腺面积	10	103平方微米	Parathyroid gland area	10=I
        // map.put("甲状旁腺面积", new IndicatorAddIn("Parathyroid gland area", parathyroidGlandArea.divide(new BigDecimal(1000), 3, RoundingMode.HALF_UP).toString(), "×10³平方微米"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
        log.info("指标计算结束-大鼠甲状腺");
    }
}
