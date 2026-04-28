package cn.staitech.fr.service.strategy.json.impl.dog.neural;

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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author wanglibei
 * @version V1.0
 * @ClassName: D65EyeAndOpticNerveParserStrategyImpl
 * @Description-d:眼球(视神经)
 * @date 2025年7月21日
 */
@Slf4j
@Service("Optic_nerve_3")
public class CanidaeOpticNerveParserStrategyImpl extends AbstractCustomParserStrategy {

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
        BigDecimal organAreaA = getOrganArea(jsonTask, "33F0BB").getStructureAreaNum();
        BigDecimal organAreaB = getOrganArea(jsonTask, "33F0BA").getStructureAreaNum();

        indicatorResultsMap.put("神经外膜结缔组织面积", createIndicator(organAreaB, SQ_MM, "33F0BA"));

        indicatorResultsMap.put("神经束膜内缘内面积", createNameIndicator("Nerve fiber bundles area", organAreaA, SQ_MM, "33F0BB"));

//        BigDecimal A = getOrganArea(jsonTask, "13F0BB",new BigDecimal(1000)).getStructureAreaNum();
//        BigDecimal B = getOrganArea(jsonTask, "13F0BA",new BigDecimal(1000)).getStructureAreaNum();
        indicatorResultsMap.put("结缔组织面积", createNameIndicator("Connective tissue area", organAreaB.subtract(organAreaA), CommonConstant.SQUARE_MILLIMETRE, "33F0BB,33F0BA"));


        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Optic_nerve_3";
    }
}
