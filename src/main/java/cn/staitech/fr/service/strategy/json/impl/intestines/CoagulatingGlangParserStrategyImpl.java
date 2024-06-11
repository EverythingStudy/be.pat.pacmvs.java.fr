package cn.staitech.fr.service.strategy.json.impl.intestines;

import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.service.strategy.json.ParserStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * @author:
 * @create: 2024-05-10 14:18:48
 * @Description: Coagulating_glang Json Parser 大鼠凝固腺
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


    @Override
    public void parseJson(JsonTask jsonTask, JsonFile jsonFileS) {
        commonJsonParser.parseJson(jsonTask, jsonFileS);
    }

    /**
     * 结构指标计算
     * 结构	编码
     * 腺上皮	12B074
     * 腺腔	12B0E9
     * 腺上皮细胞核	12B0ED
     * 组织轮廓	12B111
     * 算法输出指标	指标代码（仅限本文档）	单位（保留小数点后3位）	备注
     * 腺上皮面积（单个）	A	平方毫米	单个腺上皮面积
     * 腺上皮面积（全片）	B	平方毫米	若多个数据则相加输出
     * 腺腔面积（单个）	C	平方毫米	单个腺上皮内所有腺腔面积
     * 腺腔面积（全片）	D	平方毫米	若多个数据则相加输出
     * 腺上皮细胞核数量（单个）	E	个	单个腺上皮细胞核数量
     * 组织轮廓面积	F	平方毫米
     * <p>
     * 产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后3位）	English	计算方式	备注
     * 凝固腺面积	1	平方毫米	Coagulating gland area	1=F
     * 腺上皮面积（全片）	2	平方毫米	Acinar epithelial area (all)	2=B
     * 腺上皮面积占比（单个）	3	%	Acinar epithelial area% (per)	3=A/(A+C)	以95%置信区间和均数±标准差呈现
     * 腺泡上皮细胞核密度（单个）	4	个/平方毫米	Nucleus density of acinar epithelium (per)	4=E/A	以95%置信区间和均数±标准差呈现
     * 间质和肌层面积占比	5	%	Mesenchyme and muscular area%	5=(F-B-D)/F
     *
     * @param jsonTask
     */
    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("大鼠凝固腺结构指标计算开始");
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        // 组织轮廓面积
        String area = ObjectUtil.isNotEmpty(singleSlide) ? singleSlide.getArea() : "0";
        area = ObjectUtil.isEmpty(area) ? "0" : area;
        Map<String, IndicatorAddIn> resultMap = new HashMap<>();
        // 腺上皮面积（全片）
        BigDecimal colonArea = commonJsonParser.getOrganArea(jsonTask, "12B074").getStructureAreaNum();
        // 腺腔面积（单个）
//        BigDecimal areaNum = commonJsonParser.getOrganArea(jsonTask, "12B0E9").getStructureAreaNum();
        // 腺上皮细胞核数量（单个）
//        Integer areaCount = commonJsonParser.getOrganAreaCount(jsonTask, "12B0ED");
        // 腺腔面积（全片）
        BigDecimal areaNum2 = commonJsonParser.getInsideOrOutside(jsonTask, "12B074", "12B0E9", true).getStructureAreaNum();
        // 组织轮廓
        BigDecimal areaNum4 = new BigDecimal(area);
        // 腺上皮细胞核数量（单个）
//        resultMap.put("腺上皮细胞核数量（单个）", new IndicatorAddIn("Acinar epithelial cell number (individual)", areaCount.toString(), "个", CommonConstant.NUMBER_1));
        // 腺腔面积（全片）
        resultMap.put("腺腔面积（全片）", new IndicatorAddIn("Gland cavity area (all)", areaNum2.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        // 腺腔面积（单个）
//        resultMap.put("腺腔面积（单个）", new IndicatorAddIn("Gland cavity area (individual)", areaNum.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        resultMap.put("腺腔面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1));
        // 腺上皮面积（全片）
        resultMap.put("腺上皮面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1));

        resultMap.put("腺上皮面积（全片）", new IndicatorAddIn("Acinar epithelial area (all)", colonArea.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_0));
        // 组织轮廓的面积
        resultMap.put("凝固腺面积", new IndicatorAddIn("Coagulating gland area", areaNum4.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_0));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), resultMap);
        log.info("大鼠凝固腺结构指标计算结束");
    }
}
