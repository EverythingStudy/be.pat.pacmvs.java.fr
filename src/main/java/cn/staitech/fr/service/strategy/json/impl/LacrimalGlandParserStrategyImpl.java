package cn.staitech.fr.service.strategy.json.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
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
import cn.staitech.fr.utils.MathUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wanglibei
 * @version V1.0
 * @ClassName: LacrimalGlandParserStrategyImpl
 * @Description-d:泪腺
 * @date 2025年7月21日
 */
@Slf4j
@Component("Lacrimal_gland")
public class LacrimalGlandParserStrategyImpl extends AbstractCustomParserStrategy {
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

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("指标计算开始-泪腺");
        Map<String, IndicatorAddIn> map = new HashMap<>();

        // A 导管面积		mm2
        BigDecimal ductArea = commonJsonParser.getOrganArea(jsonTask, "16906F").getStructureAreaNum();
        // B 腺泡细胞核数量 个	无
        Integer nucleusCount = commonJsonParser.getOrganAreaCount(jsonTask, "16906E");
        // D 上皮顶部胞质面积	mm2
        BigDecimal epithelialApexCytoplasmArea = commonJsonParser.getOrganArea(jsonTask, "16906A").getStructureAreaNum();
        // E 间质面积	 mm2
        BigDecimal mesenchymeArea = commonJsonParser.getOrganArea(jsonTask, "169027").getStructureAreaNum();
        // F 组织轮廓面积	mm2
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal singleSlideBigDecimal = new BigDecimal(singleSlide.getArea());


        // A 导管面积 mm2
        map.put("导管面积", new IndicatorAddIn("导管面积", DecimalUtils.setScale3(ductArea), CommonConstant.SQUARE_MILLIMETRE, CommonConstant.NUMBER_1, "16906F"));
        // B 腺泡细胞核数量 个
        map.put("腺泡细胞核数量", new IndicatorAddIn("腺泡细胞核数量", nucleusCount.toString(), "个", CommonConstant.NUMBER_1, "16906E"));

        // C  腺泡细胞核面积（单个） μm2
        Annotation annotationC = new Annotation();
        annotationC.setAreaName("腺泡细胞核面积（单个）");
        annotationC.setAreaUnit(CommonConstant.SQUARE_MIC);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "16906E", annotationC, 2);
        // D 上皮顶部胞质面积
        map.put("上皮顶部胞质面积", new IndicatorAddIn("上皮顶部胞质面积", DecimalUtils.setScale3(epithelialApexCytoplasmArea), CommonConstant.SQUARE_MILLIMETRE, CommonConstant.NUMBER_1, "16906A"));
        // E 间质面积
        map.put("间质面积", new IndicatorAddIn("间质面积", DecimalUtils.setScale3(mesenchymeArea), CommonConstant.SQUARE_MILLIMETRE, CommonConstant.NUMBER_1, "169027"));

        // 产品呈现指标 -------------------------------------------------------------

        //6 腺泡细胞核面积（单个） μm2
        List<Annotation> skinStructureContourList = commonJsonParser.getStructureContourList(jsonTask, "16906E");
        List<BigDecimal> list = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(skinStructureContourList)) {
            for (Annotation annotation : skinStructureContourList) {
                // 默认平方毫米 - 转 平方微米
                BigDecimal areaNum = new BigDecimal(areaUtils.convertToMicrometer(annotation.getStructureAreaNum().toString()));
                list.add(areaNum);
            }
        }
        String confidence = MathUtils.getConfidenceInterval(list);

        if (singleSlideBigDecimal.compareTo(BigDecimal.ZERO) != 0) {
            // 1 导管面积占比	% Duct area%	1=A/F
            map.put("导管面积占比", new IndicatorAddIn("Duct area%", getProportion(ductArea, singleSlideBigDecimal).toString(), CommonConstant.PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("16906F", "169111")));
            //3 上皮顶部胞质占比 % Epithelial apex cytoplasm area % 3 = D / F
            map.put("上皮顶部胞质占比", new IndicatorAddIn("Epithelial apex cytoplasm area %", getProportion(epithelialApexCytoplasmArea, singleSlideBigDecimal).toString(), CommonConstant.PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("16906A", "169111")));
            //4 间质面积占比 %  Mesenchyme area %  4=E/F
            map.put("间质面积占比", new IndicatorAddIn("Mesenchyme area %", getProportion(mesenchymeArea, singleSlideBigDecimal).toString(), CommonConstant.PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("169027", "169111")));
            //5 腺泡面积占比 %   Acinus area% 5=(F-E)/F
            map.put("腺泡面积占比", new IndicatorAddIn("Acinus area %", getProportion(singleSlideBigDecimal.subtract(mesenchymeArea), singleSlideBigDecimal).toString(), CommonConstant.PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("169027", "169111")));
        } else {
            map.put("导管面积占比", new IndicatorAddIn("Duct area%", "0.000", CommonConstant.PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("16906F", "169111")));
            map.put("上皮顶部胞质占比", new IndicatorAddIn("Epithelial apex cytoplasm area %", "0.000", CommonConstant.PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("16906A", "169111")));
            map.put("间质占比", new IndicatorAddIn("Mesenchyme area %", "0.000", CommonConstant.PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("169027", "169111")));
            map.put("腺泡占比", new IndicatorAddIn("Acinus area %", "0.000", CommonConstant.PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("169027", "169111")));
        }
        // 2 腺泡细胞核密度 个/mm2 Nucleus density of acinus 2 = B / (F - E)
        if (singleSlideBigDecimal.subtract(mesenchymeArea).compareTo(BigDecimal.ZERO) != 0) {
            map.put("腺泡细胞核密度", new IndicatorAddIn("Nucleus density of acinus", bigDecimalDivideCheck(new BigDecimal(nucleusCount), singleSlideBigDecimal.subtract(mesenchymeArea)).toString(), CommonConstant.SQ_MM_PIECE_EN, CommonConstant.NUMBER_0, areaUtils.getStructureIds("16906E", "169027", "169111")));
        } else {
            map.put("腺泡细胞核密度", new IndicatorAddIn("Nucleus density of acinus", "0.000", CommonConstant.SQ_MM_PIECE_EN, CommonConstant.NUMBER_0, areaUtils.getStructureIds("16906E", "169027", "169111")));
        }

        //6 腺泡细胞核面积（单个）μm2  Acinar nucleus area (per) 6=C 以95 % 置信区间和均数±标准差呈现
        map.put("腺泡细胞核面积（单个）", new IndicatorAddIn("Acinar nucleus area (per) ", confidence, CommonConstant.SQUARE_MIC, CommonConstant.NUMBER_0, "16906E"));

        //7 泪腺面积 mm2 Lacrimal gland area 7=F
        map.put("泪腺面积", new IndicatorAddIn("Lacrimal gland area", DecimalUtils.setScale3(singleSlideBigDecimal), CommonConstant.SQUARE_MILLIMETRE, CommonConstant.NUMBER_0, "169111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
        log.info("指标计算结束-泪腺");
    }

    @Override
    public String getAlgorithmCode() {
        return "Lacrimal_gland";
    }
}
