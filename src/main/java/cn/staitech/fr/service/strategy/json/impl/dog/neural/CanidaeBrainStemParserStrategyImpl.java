package cn.staitech.fr.service.strategy.json.impl.dog.neural;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.service.strategy.json.ParserStrategy;
import cn.staitech.fr.utils.AreaUtils;
import cn.staitech.fr.utils.DecimalUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 
* @ClassName: CerebellumParserStrategyImpl
* @Description-d:脑干
* @author wanglibei
* @date 2025年7月22日
* @version V1.0
 */
@Slf4j
@Component("Brain_stem_3")
public class CanidaeBrainStemParserStrategyImpl extends AbstractCustomParserStrategy {

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
    private String speciesId = "3";
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
        log.info("指标计算开始-犬脑干");
        Map<String, IndicatorAddIn> map = new LinkedHashMap<>();
        
        /**
        A	血管内红细胞面积	13D003、13D004
		B	血管外红细胞面积	13D003、13D004
		C	颗粒细胞层核浦肯野细胞层面积	13E0A5
		D	组织轮廓面积	17D111

        血管内红细胞面积占比	1=A/D
		血管外红细胞面积占比	2=B/D
		颗粒细胞层和浦肯野细胞层面积占比	3=C/D
		小脑和脑干面积	4=D
         */

        //        血管内红细胞面积	A	平方毫米	数据相加输出
        BigDecimal intravascularErythrocyteArea = commonJsonParser.getInsideOrOutside(jsonTask, speciesId+"3D003", speciesId+"3D004", true).getStructureAreaNum();
        //        血管外红细胞面积	B	平方毫米	数据相加输出
        BigDecimal extravascularErythrocyteArea = commonJsonParser.getInsideOrOutside(jsonTask, speciesId+"3D003", speciesId+"3D004", false).getStructureAreaNum();


        //组织轮廓面积 33D111
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal accurateAreaDecimal = new BigDecimal(singleSlide.getArea());

        // 算法输出指标 -------------------------------------------------------------
        /**
        A	血管内红细胞面积	33D003、33D004
		B	血管外红细胞面积	33D003、33D004
		C	颗粒细胞层核浦肯野细胞层面积	33E0A5
		D	组织轮廓面积	33D111

        血管内红细胞面积占比	1=A/D
		血管外红细胞面积占比	2=B/D
		颗粒细胞层和浦肯野细胞层面积占比	3=C/D
		小脑和脑干面积	4=D
         */
        // 脑干
        // A
        //-- map.put("血管内红细胞面积", new IndicatorAddIn("Intravascular Erythrocyte area%", DecimalUtils.setScale3(intravascularErythrocyteArea), CommonConstant.SQUARE_MILLIMETRE, CommonConstant.NUMBER_1,areaUtils.getStructureIds(speciesId+"3D003",speciesId+"3D004")));
        // B
        //-- map.put("血管外红细胞面积", new IndicatorAddIn("Extravascular Erythrocyte area%", DecimalUtils.setScale3(extravascularErythrocyteArea), CommonConstant.SQUARE_MILLIMETRE, CommonConstant.NUMBER_1,areaUtils.getStructureIds(speciesId+"3D003",speciesId+"3D004")));
        // 小脑
        //-- map.put("分子层红细胞面积", new IndicatorAddIn("erythrocyte area", DecimalUtils.setScale3(molecularLevelerythrocyteArea), CommonConstant.SQUARE_MILLIMETRE, CommonConstant.NUMBER_1));

        // 产品呈现指标 -------------------------------------------------------------

            // 脑干
            // 血管外红细胞面积占比	1	%	Extravascular erythrocyte area%	1=B/D	无
        BigDecimal extravascularErythrocyteAreaRate = commonJsonParser.getProportion(extravascularErythrocyteArea,accurateAreaDecimal);//extravascularErythrocyteArea.divide(accurateAreaDecimal, 7, RoundingMode.HALF_UP);
        map.put("血管外红细胞面积占比", new IndicatorAddIn("Extravascular erythrocyte area%", DecimalUtils.percentScale3(extravascularErythrocyteAreaRate), "%",areaUtils.getStructureIds(speciesId+"3D003",speciesId+"3D004",speciesId+"3D111")));

            // 血管内红细胞面积	2	%	Intravascular Erythrocyte area%	A/D	无
        BigDecimal intravascularErythrocyteAreaRate = commonJsonParser.getProportion(intravascularErythrocyteArea,accurateAreaDecimal);//intravascularErythrocyteArea.divide(accurateAreaDecimal, 7, RoundingMode.HALF_UP);
        map.put("血管内红细胞面积占比", new IndicatorAddIn("Intravascular Erythrocyte area%", DecimalUtils.percentScale3(intravascularErythrocyteAreaRate), "%",areaUtils.getStructureIds(speciesId+"3D003",speciesId+"3D004",speciesId+"3D111")));
            // 分子层红细胞面积占比	2	%	Molecular level erythrocyte area%	2=B/C	无
//            BigDecimal molecularLevelErythrocyteAreaRate = molecularLevelerythrocyteArea.divide(accurateAreaDecimal, 7, RoundingMode.HALF_UP);
//             map.put("分子层红细胞面积占比", new IndicatorAddIn("Molecular level erythrocyte area%", DecimalUtils.percentScale3(molecularLevelErythrocyteAreaRate), "%"));


        // C 小脑与脑干面积	3	平方毫米	Cerebellum and Brainstem area	3=C	此组织面积为小脑＋脑干面积
        map.put("脑干面积", new IndicatorAddIn("Cerebellum and Brainstem area", DecimalUtils.setScale3(accurateAreaDecimal), CommonConstant.SQUARE_MILLIMETRE,speciesId+"3D111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
        log.info("指标计算结束-犬脑干");
    }

    @Override
    public String getAlgorithmCode() {
        return "Brain_stem_3";
    }
}
