package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.service.strategy.json.ParserStrategy;
import cn.staitech.fr.utils.MathUtils;
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
 * @Description: Json Parser 大鼠肝脏 Liver LI
 */
@Slf4j
@Component("Liver")
public class LiverParserStrategyImpl implements ParserStrategy {
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
        log.info("指标计算开始-大鼠肝脏");
        Map<String, IndicatorAddIn> map = new HashMap<>();

        //        肝脏

        // 结构编码 -------------------------------------------------------------
        //        结构	编码
        //        门管区	112145
        //        中央静脉	112146
        //        大静脉	112147
        //        肝细胞核	112149
        //        胆管	11214A
        //        窦内细胞核	11214D
        //        组织轮廓	112111
        //        112145.json  112146.json  112147.json  112149.json  11214A.json  11214D.json

        //        算法输出指标	指标代码（仅限本文档）	单位（保留小数点后三位）	备注
        //        门管区面积（单个）	A	103平方微米	单个门管区面积
        //        中央静脉面积	B	103平方微米	若多个数据则相加输出
        BigDecimal centralVeinsArea = commonJsonParser.getOrganAreaMicron(jsonTask, "112147");
        //        大静脉面积	C	103平方微米	若多个数据则相加输出
        BigDecimal venaCavaArea = commonJsonParser.getOrganAreaMicron(jsonTask, "112146");
        //        肝细胞核数量	D	个(肝细胞核数量 D 个 肝细胞核	112149)
        Integer nucleusCount = commonJsonParser.getOrganAreaCount(jsonTask, "112149");
        //        胆管数量（单个门管区）	E	个	单个门管区内胆管数量
        Integer bileDuctCount = commonJsonParser.getOrganAreaCount(jsonTask, "11214A");
        //        胆管面积（单个门管区）	F	103平方微米	若单个门管区内有多个胆管，则相加输出
        BigDecimal bileDuctArea = commonJsonParser.getOrganAreaMicron(jsonTask, "11214A");
        //        窦内细胞核数量	G	个
        Integer sinusNnucleusCount = commonJsonParser.getOrganAreaCount(jsonTask, "11214D");
        //        组织轮廓面积	H	平方毫米	若多个数据则相加输出 (H:精细轮廓总面积（肝脏面积）-平方毫米)
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        String accurateArea = singleSlide.getArea();
        BigDecimal accurateAreaDecimal = new BigDecimal(accurateArea);

        // 胆管密度（单个）	4	个/103平方微米	Density of bile duct (per)	4=E/A	单个为单个门管区  以95%置信区间和均数±标准差呈现
        // 胆管面积占比（单个）	5	%	Bile duct area%    (per)	5=F/A	单个为单个门管区  以95%置信区间和均数±标准差呈现； 运算前统一单位
        List<Annotation> structureContourList = commonJsonParser.getStructureContourList(jsonTask, "112145");
        List<BigDecimal> lists = new ArrayList<>();
        List<BigDecimal> listNum = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(structureContourList)) {
            for (Annotation annotation : structureContourList) {
                // A 门管区面积（单个）	A	103平方微米	单个门管区面积
                BigDecimal structureAreaNum = annotation.getStructureAreaNum().multiply(new BigDecimal(1000));

                Annotation contourInsideOrOutside = commonJsonParser.getContourInsideOrOutside(jsonTask, annotation.getContour(), "11214A", true);

                // E 胆管数量（单个门管区）	E	个	单个门管区内胆管数量
                Integer count = contourInsideOrOutside.getCount();

                // F 胆管面积（单个门管区）	F	103平方微米	若单个门管区内有多个胆管，则相加输出
                BigDecimal structureAreaNum1 = contourInsideOrOutside.getStructureAreaNum();

                // 4=E/A
                if (structureAreaNum.compareTo(BigDecimal.ZERO) != 0) {
                    listNum.add(new BigDecimal(count).divide(structureAreaNum, 10, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(10));
                }

                // 5=F/A
                if (structureAreaNum.compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal divide = structureAreaNum1.divide(structureAreaNum, 10, RoundingMode.HALF_UP);
                    lists.add(divide);
                }

            }
        }

        String confidenceInterval = MathUtils.getConfidenceInterval(lists);
        String confidenceInterval1 = MathUtils.getConfidenceInterval(listNum);


        //        产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后三位）	English	计算方式	备注
        //        肝脏面积	1	平方毫米	Liver area	1=H
        //        静脉面积占比	2	%	Vein area%	2=(B+C)/H	运算前注意统一单位
        //        肝细胞核密度	3	个/平方毫米	Nucleus density of hepatocyte	3=D/H(肝细胞核密度 3 = D/H)
        //        胆管密度（单个）	4	个/103平方微米	Density of bile duct (per)	4=E/A	单个为单个门管区
        //        以95%置信区间和均数±标准差呈现
        //        胆管面积占比（单个）	5	%	Bile duct area%
        //        (per)	5=F/A	单个为单个门管区
        //        以95%置信区间和均数±标准差呈现；
        //        运算前统一单位
        //        窦内细胞核密度	6	个/平方毫米	Nucleus density of Sinus cell	6=G/H

        // 算法输出指标 -------------------------------------------------------------

        // 胆管数量（单个门管区）	E	个	单个门管区内胆管数量
        // 胆管面积（单个门管区）	F	103平方微米	若单个门管区内有多个胆管，则相加输出
        Annotation annotationBy = new Annotation();
        annotationBy.setCountName("胆管数量（单个门管区）");
        annotationBy.setCountUnit("个");
        annotationBy.setAreaName("胆管面积（单个门管区）");
        annotationBy.setAreaUnit("10³平方微米");
        commonJsonParser.putAnnotationDynamicData(jsonTask, "112145", "11214A", annotationBy, 1);

        // A
        //  1：面积转10（3）平方微米  2:平方微米 （默认平方毫米）
        Annotation annotationC = new Annotation();
        annotationC.setCountName("门管区面积（单个）");
        annotationC.setCountUnit("10³平方微米");
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "112145", annotationC, 1);
        map.put("门管区面积（单个）", new IndicatorAddIn());
        // B
        map.put("中央静脉面积", new IndicatorAddIn("central veins area", centralVeinsArea.setScale(3, RoundingMode.HALF_UP).toString(), "10³平方微米", CommonConstant.NUMBER_1));
        // C
        map.put("大静脉面积", new IndicatorAddIn("vena cava area", venaCavaArea.setScale(3, RoundingMode.HALF_UP).toString(), "10³平方微米", CommonConstant.NUMBER_1));
        // D
        map.put("肝细胞核数量", new IndicatorAddIn("Nucleus count of hepatocyte", nucleusCount.toString(), "个", CommonConstant.NUMBER_1));
        // E TODO
        // map.put("胆管数量（单个门管区）", new IndicatorAddIn("bile duct count", bileDuctCount.toString(), "个", CommonConstant.NUMBER_1));
        map.put("胆管数量（单个门管区）", new IndicatorAddIn());
        // F TODO
        // map.put("胆管面积（单个门管区）", new IndicatorAddIn("", bileDuctArea.setScale(3, RoundingMode.HALF_UP).toString(), "10³平方微米", CommonConstant.NUMBER_1));
        map.put("胆管面积（单个门管区）", new IndicatorAddIn());
        // G
        map.put("窦内细胞核数量", new IndicatorAddIn("Sinus nucleus count", sinusNnucleusCount.toString(), "个", CommonConstant.NUMBER_1));

        // 产品呈现指标 -------------------------------------------------------------
        // H 肝脏面积	1	平方毫米	Liver area	1=H
        map.put("肝脏面积", new IndicatorAddIn("Liver area", accurateArea, "平方毫米"));


        if (accurateAreaDecimal.compareTo(BigDecimal.ZERO) != 0) {
            // 静脉面积占比	2	%	Vein area%	2=(B+C)/H	运算前注意统一单位
            String accurateAreaDecimalRate = centralVeinsArea.add(venaCavaArea).divide(accurateAreaDecimal, 3, RoundingMode.HALF_UP).setScale(3, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(3).toString();
            map.put("静脉面积占比", new IndicatorAddIn("Vein area%", accurateAreaDecimalRate, "%"));

            // TODO 肝细胞核密度	3	个/平方毫米	Nucleus density of hepatocyte	3=D/H
            String density = new BigDecimal(nucleusCount).divide(accurateAreaDecimal, 3, RoundingMode.HALF_UP).setScale(3, RoundingMode.HALF_UP).toString();
            map.put("肝细胞核密度", new IndicatorAddIn("Nucleus density of hepatocyte", density, "个/平方毫米"));

            // 窦内细胞核密度	6	个/平方毫米	Nucleus density of Sinus cell	6=G/H
            String nucleusDensityOfSinusCellRate = new BigDecimal(sinusNnucleusCount).divide(accurateAreaDecimal, 3, RoundingMode.HALF_UP).setScale(3, RoundingMode.HALF_UP).toString();
            map.put("窦内细胞核密度", new IndicatorAddIn("Nucleus density of Sinus cell", nucleusDensityOfSinusCellRate, "个/平方毫米"));
        } else {
            map.put("静脉面积占比", new IndicatorAddIn("Vein area%", "0.000", "%"));
            map.put("肝细胞核密度", new IndicatorAddIn("Nucleus density of hepatocyte", "0.000", "个/平方毫米"));
            map.put("窦内细胞核密度", new IndicatorAddIn("Nucleus density of Sinus cell", "0.000", "个/平方毫米"));
        }


        // 胆管密度（单个）	4	个/103平方微米	Density of bile duct (per)	4=E/A	单个为单个门管区  以95%置信区间和均数±标准差呈现
        String densityOfBileDuctPerRate = "";// = new BigDecimal(bileDuctCount).divide()
        map.put("胆管密度（单个）", new IndicatorAddIn("Density of bile duct (per)", confidenceInterval, "个/10³平方毫米"));

        // 胆管面积占比（单个）	5	%	Bile duct area%    (per)	5=F/A	单个为单个门管区  以95%置信区间和均数±标准差呈现； 运算前统一单位
        String bileDuctAreaRate = "";
        map.put("胆管面积占比（单个）", new IndicatorAddIn("Bile duct area", confidenceInterval1, "%"));


        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);

        log.info("指标计算结束-大鼠肝脏");
    }
}
