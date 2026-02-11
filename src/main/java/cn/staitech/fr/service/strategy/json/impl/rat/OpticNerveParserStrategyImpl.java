package cn.staitech.fr.service.strategy.json.impl.rat;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 
* @ClassName: D65EyeAndOpticNerveParserStrategyImpl
* @Description-d:眼球(视神经)
* @author wanglibei
* @date 2025年7月21日
* @version V1.0
 */
@Slf4j
@Service("Optic_nerve")
public class OpticNerveParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private CommonJsonCheck commonJsonCheck;
    @Autowired
    private AreaUtils areaUtils;
    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.debug("D65EyeAndOpticNerveParserStrategyImpl init");
    }


    /**
     * 视神经
     * 结构	编码
     * 神经纤维束	13F0BB
     * 神经外膜结缔组织	13F0BA
     * 算法输出指标	指标代码（仅限本文档）	单位（保留小数点后3位）	备注
     * 神经纤维束面积	A	103平方微米	相加输出
     * 神经外膜结缔组织面积	B	平方毫米	无
     * <p>
     * 产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后3位）	English	计算方式	备注
     * 神经纤维束面积	1	平方毫米	Nerve fiber bundles area	1=A	无
     * 神经外膜面积	2	103平方微米	Epineurium area	2=B-A	即神经外膜结缔组织面积
     *
     * @param jsonTask
     */

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
    	/**
    	 * 视神经
	     * 结构	编码
	     * 神经纤维束	13F0BB
	     * 神经外膜结缔组织	13F0BA
	     * 算法输出指标	指标代码（仅限本文档）	单位（保留小数点后3位）	备注
	     * 神经纤维束面积	A	103平方微米	相加输出
	     * 神经外膜结缔组织面积	B	平方毫米	无
	     * <p>
	     * 产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后3位）	English	计算方式	备注
	     * 神经纤维束面积	1	平方毫米	Nerve fiber bundles area	1=A	无
	     * 神经外膜面积	2	103平方微米	Epineurium area	2=B-A	即神经外膜结缔组织面积
    	 */
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        BigDecimal organArea3 = getOrganArea(jsonTask, "13F0BB").getStructureAreaNum();
        BigDecimal organArea4 = getOrganArea(jsonTask, "13F0BA").getStructureAreaNum();
        indicatorResultsMap.put("神经纤维束面积", new IndicatorAddIn("", organArea3.setScale(3, RoundingMode.HALF_UP).toString(), CommonConstant.SQUARE_MICROMETER, CommonConstant.NUMBER_1));
        indicatorResultsMap.put("神经纤维束面积", new IndicatorAddIn("Nerve fiber bundles area", organArea3.setScale(3, RoundingMode.HALF_UP).toString(),SQ_MM, CommonConstant.NUMBER_0));
       // indicatorResultsMap.put("神经外膜结缔组织面积", new IndicatorAddIn("", organArea4.setScale(3, RoundingMode.HALF_UP).toString(),SQ_MM, CommonConstant.NUMBER_1));

//        BigDecimal A = getOrganArea(jsonTask, "13F0BB",new BigDecimal(1000)).getStructureAreaNum();
//        BigDecimal B = getOrganArea(jsonTask, "13F0BA",new BigDecimal(1000)).getStructureAreaNum();
        indicatorResultsMap.put("神经外膜结缔组织面积", new IndicatorAddIn("Connective tissue area", organArea4.subtract(organArea3).toString(), CommonConstant.SQUARE_MILLIMETRE, CommonConstant.NUMBER_0));


        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Optic_nerve";
    }
}
