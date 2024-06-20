package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


/**
 *肠系膜淋巴结-MN
 */
@Slf4j
@Service("Mesenteric_lymph_node")
public class MesentericLymphNodeParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.info("MesentericLymphNodeParserStrategyImpl init");
    }

    /**
     结构	编码
     淋巴滤泡	146050
     生发中心	146051
     髓质	14603E
     组织轮廓	146111
     算法输出指标	指标代码（仅限本文档）	单位（保留3位小数）	备注
     生发中心数量	A	个
     生发中心面积（全片）	B	平方毫米	数据相加输出
     髓质面积	C	平方毫米
     组织轮廓面积	D	平方毫米

     产品呈现指标	指标代码（仅限本文档）	单位（保留3位小数）	English	计算方式	备注
     生发中心数量	1	个	 Number of germinal center	1=A
     生发中心占比	2	%	Germinal center area%	2=B/D
     髓质占比	3	%	Medulla area%	3=C/D
     皮质和副皮质占比	4	%	Cortex and paracortex area%	4=（D-C）/D
     淋巴结面积	5	平方毫米	Lymph node area	5=D
     */
    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> resultsMap = new HashMap<>();

        // 获取各种指标
        Integer areaCountA = areaUtils.getOrganAreaCount(jsonTask, "146051");// A生发中心数量
        BigDecimal organAreaB = areaUtils.getOrganArea(jsonTask, "146051");// B生发中心面积（全片）
        BigDecimal organAreaC = areaUtils.getOrganArea(jsonTask, "14603E");// C髓质面积
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());// D组织轮廓
        BigDecimal organAreaD = BigDecimal.valueOf(Long.parseLong(slideArea));
        // 生发中心占比
        BigDecimal germinalCenterArea = commonJsonParser.getProportion(organAreaB, organAreaD);
        // 髓质占比
        BigDecimal medullaArea = commonJsonParser.getProportion(organAreaC, organAreaD);
        // 皮质和副皮质占比
        BigDecimal cortexAndParacortexArea = commonJsonParser.getProportionMultiply(organAreaD.subtract(organAreaC), organAreaD);

        // 算法输出指标
        resultsMap.put("生发中心面积（全片）", createIndicator(organAreaB, SQ_MM));
        resultsMap.put("髓质面积", createIndicator(organAreaC, SQ_MM));

        // 产品呈现指标
        resultsMap.put("生发中心数量", createNameIndicator("Number of germinal center", areaCountA, PIECE));
        resultsMap.put("生发中心占比", createNameIndicator("Germinal center area%", germinalCenterArea, PERCENTAGE));
        resultsMap.put("髓质占比", createNameIndicator("Medulla area%", medullaArea, PERCENTAGE));
        resultsMap.put("皮质和副皮质占比", createNameIndicator("Cortex and paracortex area%", cortexAndParacortexArea, PERCENTAGE));
        resultsMap.put("淋巴结面积", createNameIndicator("Submadibular gland area", slideArea, SQ_MM));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), resultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Mesenteric_lymph_node";
    }
}
