package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.service.strategy.json.ParserStrategy;
import cn.staitech.fr.utils.DecimalUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: wangfeng
 * @create: 2024-05-10 14:18:48
 * @Description: Json Parser 大鼠大脑 Brain BR1_BR2
 */
@Slf4j
@Component("Brain")
public class BrainParserStrategyImpl implements ParserStrategy {
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
        log.info("大鼠大脑 BR1_BR2 指标计算开始……{}", jsonTask);
        Map<String, IndicatorAddIn> map = new HashMap<>();

        // 大脑
        // 结构	编码
        // 脉络丛	13209C(有数据)
        // 血管	132003（暂无数据!!!!!!）
        // 红细胞	132004(有数据)
        // 组织轮廓	132111

        // 算法输出指标	指标代码（仅限本文档）	单位（保留小数点后3位）	备注
        // 脉络丛面积	A	平方毫米	无
        BigDecimal choroidOPlexusAreaAnnotation = commonJsonParser.getOrganArea(jsonTask, "13209C").getStructureAreaNum();
        // 血管外红细胞面积	B	103平方微米	无 (查询血管外红细胞面积)
        // BigDecimal extravascularErythrocyteArea = commonJsonParser.getInsideOrOutside(jsonTask, "132003", "132004", false).getStructureAreaNum();
        // extravascularErythrocyteArea = extravascularErythrocyteArea.multiply(new BigDecimal(0.001));

        // 血管内红细胞面积	C	平方毫米	无 (查询血管内红细胞面积)
        // BigDecimal intravascularErythrocyteArea = commonJsonParser.getInsideOrOutside(jsonTask, "132003", "132004", true).getStructureAreaNum();
        // 大脑面积	D	平方毫米	无

        // 产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后3位）	English	计算方式	备注
        // 脉络丛面积占比	1	%	Choroid Plexus area %	1=A/D	无
        // 血管外红细胞面积占比	2	%	Extravascular Erythrocyte area%	2=B/D	无
        // 血管内红细胞面积占比	3	%	Intravascular Erythrocyte area%	3=C/D	无
        // 大脑面积	4	平方毫米	Brain area	4=D	无

        // D:精细轮廓总面积（大鼠大脑）- 平方毫米
        String accurateArea = singleSlideMapper.selectById(jsonTask.getSingleId()).getArea();
        BigDecimal accurateAreaBigDecimal = new BigDecimal(accurateArea);

        // 算法输出指标 -------------------------------------------------------------
        // A
        map.put("脉络丛面积", new IndicatorAddIn("Choroid Plexus area", DecimalUtils.setScale3(choroidOPlexusAreaAnnotation), "平方毫米", CommonConstant.NUMBER_1));
        // // B
        // map.put("血管外红细胞面积", new IndicatorAddIn("Extravascular Erythrocyte area", extravascularErythrocyteArea.setScale(3, RoundingMode.HALF_UP).toString(), "×10³平方微米", CommonConstant.NUMBER_1));
        // // C
        // map.put("血管内红细胞面积", new IndicatorAddIn("Intravascular Erythrocyte area", intravascularErythrocyteArea.toString(), "平方毫米", CommonConstant.NUMBER_1));

        // 产品呈现指标 -------------------------------------------------------------
        if (accurateAreaBigDecimal.compareTo(BigDecimal.ZERO) != 0) {
            // 脉络丛面积占比	1	%	Choroid Plexus area %	1=A/D	无
            BigDecimal choroidPlexusAreaRate = choroidOPlexusAreaAnnotation.divide(accurateAreaBigDecimal, 7, RoundingMode.HALF_UP);
            map.put("脉络丛面积占比", new IndicatorAddIn("Choroid Plexus area %", DecimalUtils.percentScale3(choroidPlexusAreaRate), "%"));

            // 血管外红细胞面积占比	2	%	Extravascular Erythrocyte area%	2=B/D	无
            // BigDecimal extravascularErythrocyteAreaRate = extravascularErythrocyteArea.divide(accurateAreaBigDecimal, 6, RoundingMode.HALF_UP);
            // map.put("血管外红细胞面积占比", new IndicatorAddIn("Extravascular Erythrocyte area%",DecimalUtils.percentScale3( extravascularErythrocyteAreaRate.), "%"));
            //
            // // 血管内红细胞面积占比	3	%	Intravascular Erythrocyte area%	3=C/D	无
            // BigDecimal intravascularErythrocyteAreaRate = intravascularErythrocyteArea.divide(accurateAreaBigDecimal, 6, RoundingMode.HALF_UP);
            // map.put("血管内红细胞面积占比", new IndicatorAddIn("Intravascular Erythrocyte area%", DecimalUtils.percentScale3(intravascularErythrocyteAreaRate), "%"));
        } else {
            map.put("脉络丛面积占比", new IndicatorAddIn("Choroid Plexus area %", "0.000", "%"));
            // map.put("血管外红细胞面积占比", new IndicatorAddIn("Extravascular Erythrocyte area%", "0.000", "%"));
            // map.put("血管内红细胞面积占比", new IndicatorAddIn("Intravascular Erythrocyte area%", "0.000", "%"));
        }

        // D 大脑面积	4	平方毫米	Brain area	4=D	无
        map.put("大脑面积", new IndicatorAddIn("Brain area", DecimalUtils.setScale3(accurateAreaBigDecimal), "平方毫米"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
    }
}
