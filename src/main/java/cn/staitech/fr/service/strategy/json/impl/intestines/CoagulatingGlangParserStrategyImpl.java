package cn.staitech.fr.service.strategy.json.impl.intestines;

import cn.hutool.core.util.ObjectUtil;
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
import cn.staitech.fr.utils.AreaUtils;
import cn.staitech.fr.utils.DecimalUtils;
import cn.staitech.fr.utils.MathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
* @ClassName: CoagulatingGlangParserStrategyImpl
* @Description-d:凝固腺
* @author wanglibei
* @date 2025年7月22日
* @version V1.0
 */
@Slf4j
@Component("Coagulating_glang")
public class CoagulatingGlangParserStrategyImpl implements ParserStrategy {

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

    @Override
    public void parseJson(JsonTask jsonTask, JsonFile jsonFileS) {
        commonJsonParser.parseJson(jsonTask, jsonFileS);
    }

    @Override
    public boolean checkJson(JsonTask jsonTask, List<JsonFile> jsonFileList) {
        return commonJsonCheck.checkJson(jsonTask, jsonFileList);
    }

    /**
     * 指标计算
     *
     * @param jsonTask
     */
    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("标计算开始-大鼠凝固腺");
        Map<String, IndicatorAddIn> map = new HashMap<>();

        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        // 组织轮廓面积
        String area = ObjectUtil.isNotEmpty(singleSlide) ? singleSlide.getArea() : "0";
        area = ObjectUtil.isEmpty(area) ? "0" : area;

        // 结构编码 -------------------------------------------------------------
        // 结构	编码
        // 腺上皮	12B074
        // 腺腔	12B0E9
        // 腺上皮细胞核	12B0ED
        // 组织轮廓	12B111

        // 腺上皮面积（全片）
        BigDecimal colonArea = commonJsonParser.getOrganArea(jsonTask, "12B074").getStructureAreaNum();
        // D 腺腔面积（全片）
        BigDecimal areaNum2 = commonJsonParser.getOrganArea(jsonTask, "12B0E9").getStructureAreaNum();
        // 组织轮廓
        BigDecimal tissueArea = new BigDecimal(area);

        // 腺上皮面积占比（单个）	3	%	Acinar epithelial area% (per)	3=A/(A+C) 以95%置信区间和均数±标准差呈现
        // 腺泡上皮细胞核密度（单个）	4	个/平方毫米	Nucleus density of acinar epithelium (per)	4=E/A	以95%置信区间和均数±标准差呈现
        List<Annotation> structureContourList = commonJsonParser.getStructureContourList(jsonTask, "12B074");
        List<BigDecimal> lists = new ArrayList<>();
        List<BigDecimal> listNum = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(structureContourList)) {
            for (Annotation annotation : structureContourList) {
                // A 腺上皮面积（单个）	A	平方毫米	单个腺上皮面积
                BigDecimal structureAreaNum = annotation.getStructureAreaNum();

                // C 腺腔面积（单个）	C	平方毫米	单个腺上皮内所有腺腔面积
                Annotation contourInsideOrOutside = commonJsonParser.getContourInsideOrOutside(jsonTask, annotation.getContour(), "12B0E9", true);
                Annotation contourInsideOrOutside2 = commonJsonParser.getContourInsideOrOutside(jsonTask, annotation.getContour(), "12B0ED", true);

                // 面积 C
                BigDecimal structureAreaNum1 = contourInsideOrOutside.getStructureAreaNum();
                // A+C
                BigDecimal addAC = structureAreaNum.add(structureAreaNum1);
                if (addAC.compareTo(BigDecimal.ZERO) != 0) {
                    // 3=A/(A+C)
                    lists.add(structureAreaNum.divide(addAC, 7, RoundingMode.HALF_UP).multiply(new BigDecimal(100)));
                }

                // E
                Integer count = contourInsideOrOutside2.getCount();
                if (addAC.compareTo(BigDecimal.ZERO) != 0) {
                    // 4=E/A
                    BigDecimal divide = new BigDecimal(count).divide(structureAreaNum, 7, RoundingMode.HALF_UP);
                    listNum.add(divide);
                }
            }
        }

        String confidenceInterval = MathUtils.getConfidenceInterval(lists);
        String confidenceInterval1 = MathUtils.getConfidenceInterval(listNum);

        // 腺上皮面积（单个）A 平方毫米 单个腺上皮面积 1：面积转10（3）平方微米  2:平方微米 3默认平方毫米
        Annotation annotationA = new Annotation();
        annotationA.setAreaName("腺上皮面积（单个）");
        annotationA.setAreaUnit(CommonConstant.SQUARE_MILLIMETRE);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "12B074", annotationA, 3);

        // 腺腔面积（单个）	C	平方毫米	单个腺上皮内所有腺腔面积
        Annotation annotationByC = new Annotation();
        annotationByC.setAreaName("腺腔面积（单个）");
        annotationByC.setAreaUnit(CommonConstant.SQUARE_MILLIMETRE);
        commonJsonParser.putAnnotationDynamicData(jsonTask, "12B074", "12B0E9", annotationByC);

        // 腺上皮细胞核数量（单个）	E	个	单个腺上皮细胞核数量
        Annotation annotationByE = new Annotation();
        annotationByE.setCountName("腺上皮细胞核数量（单个）");
        annotationByE.setCountUnit("个");
        commonJsonParser.putAnnotationDynamicData(jsonTask, "12B074", "12B0ED", annotationByE);

        // 算法输出指标 -------------------------------------------------------------
        /**
        A	腺上皮面积（单个）	12B074
		B	腺上皮面积（全片）	12B074
		C	腺腔面积（单个）	12B074、12B0E9
		D	腺腔面积（全片）	12B074、12B0E9
		E	腺上皮细胞核数量（单个）	12B074、12B0ED
		F	组织轮廓面积	12B111
		
		凝固腺面积	1=F
		腺上皮面积（全片）	2=B
		腺上皮面积占比（单个）	3=A/(A+C)
		腺泡上皮细胞核密度（单个）	4=E/A
		间质和肌层面积占比	5=(F-B-D)/F
         */
        // 腺上皮面积（单个）A 平方毫米 单个腺上皮面积
        map.put("腺上皮面积（单个）", new IndicatorAddIn("12B074"));

        // 腺腔面积（单个）C 平方毫米 单个腺上皮内所有腺腔面积
//        map.put("腺腔面积（单个）", new IndicatorAddIn(areaUtils.getStructureIds("12B074","12B0E9")));

        // 腺腔面积（全片）D 平方毫米 若多个数据则相加输出
//        map.put("腺腔面积（全片）", new IndicatorAddIn("Gland cavity area (all)", DecimalUtils.setScale3(areaNum2), CommonConstant.SQUARE_MILLIMETRE, CommonConstant.NUMBER_1,areaUtils.getStructureIds("12B074","12B0E9")));

        // 腺上皮细胞核数量（单个）E 个 单个腺上皮细胞核数量
//        map.put("腺上皮细胞核数量（单个）", new IndicatorAddIn(areaUtils.getStructureIds("12B074","12B0ED")));

        // 产品呈现指标 -------------------------------------------------------------
        // F 组织轮廓的面积 凝固腺面积	1	平方毫米	Coagulating gland area	1=F
        map.put("凝固腺面积", new IndicatorAddIn("Coagulating gland area", DecimalUtils.setScale3(tissueArea), CommonConstant.SQUARE_MILLIMETRE, CommonConstant.NUMBER_0,"12B111"));

        //        腺上皮面积（全片）	2	平方毫米	Acinar epithelial area (all)	2=B
        // 腺上皮面积（全片）B 平方毫米 若多个数据则相加输出
        map.put("腺上皮面积（全片）", new IndicatorAddIn("Acinar epithelial area (all)", DecimalUtils.setScale3(colonArea), CommonConstant.SQUARE_MILLIMETRE, CommonConstant.NUMBER_0,"12B074"));
        map.put("腺上皮面积（全片）", new IndicatorAddIn(DecimalUtils.setScale3(colonArea), CommonConstant.NUMBER_1,"12B074"));

        // 腺上皮面积占比（单个）	3	%	Acinar epithelial area% (per)	3=A/(A+C) 以95%置信区间和均数±标准差呈现
        map.put("腺上皮面积占比（单个）", new IndicatorAddIn("Acinar epithelial area% (per)", confidenceInterval, "%",areaUtils.getStructureIds("12B074","12B0E9")));

        // 腺泡上皮细胞核密度（单个）	4	个/平方毫米	Nucleus density of acinar epithelium (per)	4=E/A 以95%置信区间和均数±标准差呈现
//        map.put("腺泡上皮细胞核密度（单个）", new IndicatorAddIn("Nucleus density of acinar epithelium (per)", confidenceInterval1, CommonConstant.SQ_MM_PIECE_EN,areaUtils.getStructureIds("12B074","12B0ED")));

        if (tissueArea.compareTo(BigDecimal.ZERO) != 0) {
            // 间质和肌层面积占比	5	%	Mesenchyme and muscular area%	5=(F-B-D)/F
//            BigDecimal mesenchymeAndMuscularAreaRate = tissueArea.subtract(colonArea).subtract(areaNum2).divide(tissueArea, 7, RoundingMode.HALF_UP);
//            map.put("间质和肌层面积占比", new IndicatorAddIn("Mesenchyme and muscular area%", DecimalUtils.percentScale3(mesenchymeAndMuscularAreaRate), "%",areaUtils.getStructureIds("12B111","12B074","12B0E9")));
        } else {
//            map.put("间质和肌层面积占比", new IndicatorAddIn("Mesenchyme and muscular area%", "0.000", "%",areaUtils.getStructureIds("12B111","12B074","12B0E9")));
        }

        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
        log.info("标计算结束-大鼠凝固腺");
    }
}
