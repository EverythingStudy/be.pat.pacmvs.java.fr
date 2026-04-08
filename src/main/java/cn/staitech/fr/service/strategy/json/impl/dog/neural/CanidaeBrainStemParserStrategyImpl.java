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
import cn.staitech.fr.utils.AreaUtils;
import cn.staitech.fr.utils.DecimalUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author wanglibei
 * @version V1.0
 * @ClassName: CerebellumParserStrategyImpl
 * @Description-d:脑干
 * @date 2025年7月22日
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
        //        血管外红细胞面积
        BigDecimal bigDecimalA = commonJsonParser.getInsideOrOutside(jsonTask, speciesId + "3D003", speciesId + "3D004", false).getStructureAreaNum();
        //        血管内红细胞面积
        BigDecimal bigDecimalB = commonJsonParser.getInsideOrOutside(jsonTask, speciesId + "3D003", speciesId + "3D004", true).getStructureAreaNum();

        //组织轮廓面积 33D111
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal bigDecimalC = new BigDecimal(singleSlide.getArea());

        // 脑干
        // A
        // B
        map.put("血管外红细胞面积", new IndicatorAddIn("Extravascular Erythrocyte area%", DecimalUtils.setScale3(bigDecimalA.multiply(new BigDecimal("1000"))), MULTIPLIED_SQ_UM_THOUSAND, CommonConstant.NUMBER_1, areaUtils.getStructureIds(speciesId + "3D003", speciesId + "3D004")));
        map.put("血管内红细胞面积", new IndicatorAddIn("Intravascular Erythrocyte area%", DecimalUtils.setScale3(bigDecimalB), CommonConstant.SQUARE_MILLIMETRE, CommonConstant.NUMBER_1, areaUtils.getStructureIds(speciesId + "3D003", speciesId + "3D004")));


        // 血管外红细胞面积占比	1	%	Extravascular erythrocyte area%	1=B/D	无
        BigDecimal ac1 = commonJsonParser.getProportion(bigDecimalA, bigDecimalC);
        map.put("血管外红细胞面积占比", new IndicatorAddIn("Extravascular erythrocyte area%", String.valueOf(ac1), "%", "33D003,33D004,33D111"));

        // 血管内红细胞面积	2	%	Intravascular Erythrocyte area%	A/D	无
        BigDecimal bc2 = commonJsonParser.getProportion(bigDecimalB, bigDecimalC);
        map.put("血管内红细胞面积占比", new IndicatorAddIn("Intravascular Erythrocyte area%", String.valueOf(bc2), "%", "33D003,33D004,33D111"));

        // C 小脑与脑干面积	3	平方毫米	Cerebellum and Brainstem area	3=C	此组织面积为小脑＋脑干面积
        map.put("脑干面积", new IndicatorAddIn("Cerebellum and Brainstem area", DecimalUtils.setScale3(bigDecimalC), CommonConstant.SQUARE_MILLIMETRE, "33D111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
        log.info("指标计算结束-犬脑干");
    }

    @Override
    public String getAlgorithmCode() {
        return "Brain_stem_3";
    }
}
