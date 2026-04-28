package cn.staitech.fr.service.strategy.json.impl.dog.neural;

import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author wanglibei
 * @version V1.0
 * @ClassName: SciaticNerveParserStrategyImpl
 * @Description-d:坐骨神经
 * @date 2025年7月22日
 */
@Slf4j
@Component("Nerve_sciatic_3")
public class CanidaeSciaticNerveParserStrategyImpl extends AbstractCustomParserStrategy {
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private AreaUtils areaUtils;
    @Resource
    private CommonJsonCheck commonJsonCheck;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("SciaticNerveParserStrategyImpl init");
    }


    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("犬坐骨神经构指标计算开始");
        //		结构	编码
        //		神经纤维束	1400BB
        //		神经外膜结缔组织	1400BA
        //		算法输出指标	指标代码（仅限本文档）	单位（保留小数点后3位）	备注
        //		神经纤维束面积	A	10³平方微米	若多个数据则相加输出
        //		神经外膜结缔组织面积	B	平方毫米
        //
        //		产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后3位）	English	计算方式	备注
        //		神经纤维束面积	1	10³平方微米	Nerve fiber bundles area	1=A
        //		结缔组织面积	2	平方毫米	Connective tissue area	2=B-A	运算前注意统一单位
        //		即神经外膜结缔组织面积

        /**
         A	神经纤维束面积	1400BB
         B	神经外膜结缔组织面积	1400BA

         神经纤维束面积	1=A
         神经外膜结缔组织面积	2=B-A
         */

        Map<String, IndicatorAddIn> indicatorResultsMap = new LinkedHashMap<>();
        // 神经束膜内缘内面积
        BigDecimal bigDecimalA = getOrganArea(jsonTask, "3400BB").getStructureAreaNum();
        // 神经外膜结缔组织面积
        BigDecimal bigDecimalB = getOrganArea(jsonTask, "3400BA").getStructureAreaNum();

        indicatorResultsMap.put("神经外膜结缔组织面积", createIndicator(bigDecimalB, SQ_MM, "3400BA"));
        indicatorResultsMap.put("神经束膜内缘内面积", createNameIndicator("Nerve fiber bundles area", bigDecimalA, SQ_MM, "3400BB"));
        indicatorResultsMap.put("结缔组织面积", createNameIndicator("Connective tissue area", bigDecimalB.subtract(bigDecimalA), SQ_MM, "3400BB,3400BA"));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);

    }

    @Override
    public String getAlgorithmCode() {
        return "Nerve_sciatic_3";
    }
}
