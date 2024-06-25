package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
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
 * @create: 2024-05-21 14:18:48
 * @Description: Json Parser 大鼠脑干（合并）、大鼠小脑(合并）
 */
@Slf4j
@Component("CerebellumBrainStem")
public class CerebellumParserStrategyImpl implements ParserStrategy {

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
        log.info("指标计算开始-大鼠脑干（合并）、大鼠小脑(合并）");
        Map<String, IndicatorAddIn> map = new HashMap<>();

        //        脑干
        //        结构	编码
        //        血管	13D003（无JSON文件）
        //        红细胞	13D004******
        //        组织轮廓	13E111
        //        13D004.json
        //        算法输出指标	指标代码（仅限本文档）	单位（保留小数点后3位）	备注
        //        血管内红细胞面积	A	平方毫米	数据相加输出
        BigDecimal intravascularErythrocyteArea = commonJsonParser.getInsideOrOutside(jsonTask, "13D003", "13D004", true).getStructureAreaNum();
        //        血管外红细胞面积	B	平方毫米	数据相加输出
        BigDecimal extravascularErythrocyteArea = commonJsonParser.getInsideOrOutside(jsonTask, "13D003", "13D004", false).getStructureAreaNum();

        //        组织面积	C	平方毫米	此组织面积为小脑＋脑干面积

        //        产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后3位）	English	计算方式	备注
        //        血管外红细胞面积占比	1	%	Extravascular erythrocyte area%	1=B/C	无
        //        血管内红细胞面积	2	%	Intravascular Erythrocyte area%	2=A/C	无
        //        小脑和脑干面积	3	平方毫米	Cerebellum and Brainstem area	3=C	此组织面积为小脑＋脑干面积

        //        小脑
        //        结构	编码
        //        颗粒细胞层＋浦肯野细胞层	13E0A5******
        //        分子层红细胞	13E0A9（无JSON文件）
        //        组织轮廓	13E111
        //        13E0A5.json

        //        算法输出指标	指标代码（仅限本文档）	单位（保留小数点后3位）	备注
        //        颗粒细胞层＋浦肯野细胞层面积	A	平方毫米	无
        //        分子层红细胞面积	B	平方毫米	无
        //        组织面积	C	平方毫米	此组织面积为小脑＋脑干面积

        //        产品呈现指标	指标代码（仅限本文档）	单位保留小数点后3位）	English	计算方式	备注
        //        颗粒细胞层和浦肯野细胞层面积占比	1	%	Granulocyte and Purkinje cell layer area % 	1=A/C	无
        //        分子层红细胞面积占比	2	%	Molecular level erythrocyte area%	2=B/C	无
        //        小脑和脑干面积	3	平方毫米	Cerebellum and Brainstem area	3=C	此组织面积为小脑＋脑干面积

        // String erythrocyteArea = commonJsonParser.getOrganArea(jsonTask, "13D004").getStructureAreaNum().toString();
        String molecularLevelerythrocyteArea = commonJsonParser.getOrganArea(jsonTask, "13E0A9").getStructureAreaNum().toString();
        String granulocyteAndPurkinjeArea = commonJsonParser.getOrganArea(jsonTask, "13E0A5").getStructureAreaNum().toString();

        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal accurateAreaDecimal = new BigDecimal(singleSlide.getArea());

        // 算法输出指标 -------------------------------------------------------------
        // 脑干
        // A
        map.put("血管内红细胞面积", new IndicatorAddIn("Extravascular erythrocyte area", intravascularErythrocyteArea.toString(), "平方毫米", CommonConstant.NUMBER_1));
        // B
        map.put("血管外红细胞面积", new IndicatorAddIn("Intravascular Erythrocyte area", extravascularErythrocyteArea.toString(), "平方毫米", CommonConstant.NUMBER_1));
        // 小脑
        // A
        map.put("颗粒细胞层＋浦肯野细胞层面积", new IndicatorAddIn("Granulocyte and Purkinje cell layer area", granulocyteAndPurkinjeArea, "平方毫米", CommonConstant.NUMBER_1));
        // B
        map.put("分子层红细胞面积", new IndicatorAddIn("erythrocyte area", molecularLevelerythrocyteArea, "平方毫米", CommonConstant.NUMBER_1));

        // 产品呈现指标 -------------------------------------------------------------

        if (accurateAreaDecimal.compareTo(BigDecimal.ZERO) != 0) {
            // 脑干
            // 血管外红细胞面积占比	1	%	Extravascular erythrocyte area%	1=B/C	无
            String extravascularErythrocyteAreaRate = extravascularErythrocyteArea.divide(accurateAreaDecimal, 3, RoundingMode.HALF_UP).setScale(3, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(3).toString();
            map.put("血管外红细胞面积占比", new IndicatorAddIn("Extravascular erythrocyte area%", extravascularErythrocyteAreaRate, "%"));

            // 血管内红细胞面积	2	%	Intravascular Erythrocyte area%	2=A/C	无
            String intravascularErythrocyteAreaRate = intravascularErythrocyteArea.divide(accurateAreaDecimal, 3, RoundingMode.HALF_UP).setScale(3, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(3).toString();
            map.put("血管内红细胞面积占比", new IndicatorAddIn("Intravascular Erythrocyte area%", intravascularErythrocyteAreaRate, "%"));

            // 小脑
            // 颗粒细胞层和浦肯野细胞层面积占比	1	%	Granulocyte and Purkinje cell layer area % 	1=A/C	无
            String granulocyteAndPurkinjeCellLayerAreaRate = new BigDecimal(granulocyteAndPurkinjeArea).divide(accurateAreaDecimal, 3, RoundingMode.HALF_UP).setScale(3, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(3).toString();
            map.put("颗粒细胞层和浦肯野细胞层面积占比", new IndicatorAddIn("Granulocyte and Purkinje cell layer area %", granulocyteAndPurkinjeCellLayerAreaRate, "%"));

            // 分子层红细胞面积占比	2	%	Molecular level erythrocyte area%	2=B/C	无
            String molecularLevelErythrocyteAreaRate = new BigDecimal(molecularLevelerythrocyteArea).divide(accurateAreaDecimal, 3, RoundingMode.HALF_UP).setScale(3, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(3).toString();
            map.put("", new IndicatorAddIn("Molecular level erythrocyte area%", molecularLevelErythrocyteAreaRate, "%"));
        } else {
            map.put("血管外红细胞面积占比", new IndicatorAddIn("Extravascular erythrocyte area%", "0.000", "%"));
            map.put("血管内红细胞面积占比", new IndicatorAddIn("Intravascular Erythrocyte area%", "0.000", "%"));
            map.put("颗粒细胞层和浦肯野细胞层面积占比", new IndicatorAddIn("Granulocyte and Purkinje cell layer area %", "0.000", "%"));
            map.put("", new IndicatorAddIn("Molecular level erythrocyte area%", "0.000", "%"));
        }

        // C 小脑和脑干面积	3	平方毫米	Cerebellum and Brainstem area	3=C	此组织面积为小脑＋脑干面积
        map.put("小脑和脑干面积", new IndicatorAddIn("Cerebellum and Brainstem area", accurateAreaDecimal.toString(), "平方毫米"));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);

        log.info("指标计算结束-大鼠脑干（合并）、大鼠小脑(合并）");
    }
}
