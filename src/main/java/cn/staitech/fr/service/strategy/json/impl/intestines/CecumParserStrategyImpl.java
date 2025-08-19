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
import java.util.List;
import java.util.Map;

/**
 * 
* @ClassName: CecumParserStrategyImpl
* @Description-d:盲肠
* @author wanglibei
* @date 2025年7月21日
* @version V1.0
 */
@Slf4j
@Component("Cecum")
public class CecumParserStrategyImpl implements ParserStrategy {
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
        log.info("指标计算开始-大鼠盲肠");
        Map<String, IndicatorAddIn> map = new HashMap<>();

        // 肠腔	114156
        // 黏膜层	114008
        // 黏膜下层 	114009
        // 肌层	11400C
        // 淋巴小结	114049
        // 组织轮廓	114111

        // 查询所有未被删除且登录机构相同的数据
        //组织轮廓
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        String area = ObjectUtil.isNotEmpty(singleSlide) ? singleSlide.getArea() : "0";
        area = ObjectUtil.isEmpty(area) ? "0" : area;

        // 肠腔面积
        BigDecimal colonArea = commonJsonParser.getOrganArea(jsonTask, "114156").getStructureAreaNum();
        // 黏膜层面积
        BigDecimal areaNumB = commonJsonParser.getOrganArea(jsonTask, "114008").getStructureAreaNum();
        // 黏膜下层面积
        BigDecimal areaNumC = commonJsonParser.getOrganArea(jsonTask, "114009").getStructureAreaNum();
        // 肌层面积
        BigDecimal areaNumD = commonJsonParser.getOrganArea(jsonTask, "11400C").getStructureAreaNum();
        // 淋巴小结面积
        BigDecimal areaNumE = commonJsonParser.getOrganArea(jsonTask, "114049").getStructureAreaNum();
        // 组织轮廓
        BigDecimal tissueArea = new BigDecimal(area);
        // 盲肠面积
        BigDecimal subtractFA = new BigDecimal(0);
        if (tissueArea.compareTo(BigDecimal.ZERO) != 0) {
            subtractFA = tissueArea.subtract(colonArea).setScale(7, RoundingMode.HALF_UP);
        }
        
        /**
        A	肠腔面积	114156
		B	黏膜层面积	114008
		C	黏膜下层面积	114009
		D	肌层面积	11400C
		E	淋巴小结面积	114049
		F	组织轮廓面积	114111
		
		黏膜层面积占比	1=B/(F-A)
		黏膜下层面积占比	2=C/(F-A)
		肌层面积占比	3=D/(F-A)
		淋巴小结面积占比	4=E/(F-A)
		盲肠面积	5=F-A
         */
        // 算法输出指标 -------------------------------------------------------------
        // 肠腔面积	A	平方毫米
        map.put("肠腔面积", new IndicatorAddIn("Intestinal cavity area", DecimalUtils.setScale3(colonArea), CommonConstant.SQUARE_MILLIMETRE, CommonConstant.NUMBER_1, "114156"));

        // 黏膜层面积	B	平方毫米	以C型或双层环状输出
        map.put("黏膜层面积", new IndicatorAddIn("Mucosal layer area", DecimalUtils.setScale3(areaNumB), CommonConstant.SQUARE_MILLIMETRE, CommonConstant.NUMBER_1, "114008"));

        // 黏膜下层面积	C	平方毫米	以C型或双层环状输出
        map.put("黏膜下层面积", new IndicatorAddIn("Submucosal area", DecimalUtils.setScale3(areaNumC), CommonConstant.SQUARE_MILLIMETRE, CommonConstant.NUMBER_1, "114009"));

        // 肌层面积	D	平方毫米	以C型或双层环状输出
        map.put("肌层面积", new IndicatorAddIn("Muscle layer area", DecimalUtils.setScale3(areaNumD), CommonConstant.SQUARE_MILLIMETRE, CommonConstant.NUMBER_1, "11400C"));

        // 淋巴小结面积	E	平方毫米	若多个数据则相加输出
        map.put("淋巴小结面积", new IndicatorAddIn("Lymph nodule area", DecimalUtils.setScale3(areaNumE), CommonConstant.SQUARE_MILLIMETRE, CommonConstant.NUMBER_1, "114049"));

        // 组织轮廓面积	F	平方毫米	无
        map.put("组织轮廓面积", new IndicatorAddIn("Tissue area", DecimalUtils.setScale3(tissueArea), CommonConstant.SQUARE_MILLIMETRE, CommonConstant.NUMBER_1, "114111"));

        // 产品呈现指标 -------------------------------------------------------------
        if (subtractFA.compareTo(BigDecimal.ZERO) != 0) {
            // 黏膜层面积占比	1	%	Mucosal area%	1=B/（F-A）
            BigDecimal mucosalAreaRate = areaNumB.divide(subtractFA, 7, RoundingMode.HALF_UP);
            map.put("黏膜层面积占比", new IndicatorAddIn("Mucosal area%", DecimalUtils.percentScale3(mucosalAreaRate), "%",areaUtils.getStructureIds("114008", "114111", "114156")));

            // 黏膜下层面积占比	2	%	Submucosal area%	2=C/（F-A）
            BigDecimal submucosalAreaRate = areaNumC.divide(subtractFA, 7, RoundingMode.HALF_UP);
            map.put("黏膜下层面积占比", new IndicatorAddIn("Submucosal area%", DecimalUtils.percentScale3(submucosalAreaRate), "%",areaUtils.getStructureIds("114009", "114111", "114156")));

            // 肌层面积占比	3	%	Muscular area%	3=D/（F-A）
            BigDecimal muscularAreaRate = areaNumD.divide(subtractFA, 7, RoundingMode.HALF_UP);
            map.put("肌层面积占比", new IndicatorAddIn("Muscular area%", DecimalUtils.percentScale3(muscularAreaRate), "%",areaUtils.getStructureIds("11400C", "114111", "114156")));

            // 淋巴小结面积占比	4	%	Lymphatic nodule area%	4=E/（F-A）
            BigDecimal lymphaticNoduleAreaRate = areaNumE.divide(subtractFA, 7, RoundingMode.HALF_UP);
            map.put("淋巴小结面积占比", new IndicatorAddIn("Lymphatic nodule area%", DecimalUtils.percentScale3(lymphaticNoduleAreaRate), "%",areaUtils.getStructureIds("114049", "114111", "114156")));
        } else {
            map.put("黏膜层面积占比", new IndicatorAddIn("Mucosal area%", "0.000", "%",areaUtils.getStructureIds("114008", "114111", "114156")));
            map.put("黏膜下层面积占比", new IndicatorAddIn("Submucosal area%", "0.000", "%",areaUtils.getStructureIds("114009", "114111", "114156")));
            map.put("肌层面积占比", new IndicatorAddIn("Muscular area%", "0.000", "%",areaUtils.getStructureIds("11400C", "114111", "114156")));
            map.put("淋巴小结面积占比", new IndicatorAddIn("Lymphatic nodule area%", "0.000", "%",areaUtils.getStructureIds("11400C", "114111", "114156")));
        }
        // 盲肠面积	5	平方毫米	Cecum area	5=F-A
        map.put("盲肠面积", new IndicatorAddIn("Cecum area", DecimalUtils.setScale3(subtractFA), CommonConstant.SQUARE_MILLIMETRE, CommonConstant.NUMBER_0,areaUtils.getStructureIds("114111", "114156")));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
        log.info("指标计算结束-大鼠盲肠");
    }
}
