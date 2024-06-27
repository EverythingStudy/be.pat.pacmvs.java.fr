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
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;


/**
 * 腮腺-PG
 */
@Slf4j
@Service("Parotid_gland")
public class ParotidGlandParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.info("ParotidGlandParserStrategyImpl init");
    }

    /**
     结构	编码
     导管	10906F
     血管	 109003
     腮腺腺泡细胞核 	10906E
     组织轮廓	109111
     算法输出指标	指标代码（仅限本文档）	单位(保留小数点后3位)	备注
     腺泡细胞核数量	A	个	无
     导管面积	B	103平方微米	若多个数据则相加输出
     血管面积	C	103平方微米	若多个数据则相加输出
     组织轮廓	D	平方毫米	无

     产品呈现指标	指标代码（仅限本文档）	单位(保留小数点后3位)	English	计算方式	备注
     腺泡细胞核密度	1	个/平方毫米	Nucleus density of acinar cell	1=A/D
     血管面积占比	2	%	Vessel area%	2=C/D	运算前注意统一单位
     导管面积占比	3	%	Ducts area%	3=B/D	运算前注意统一单位
     腮腺面积	4	平方毫米	 Parotid gland area	4=D
     */

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> resultsMap = new HashMap<>();

        // 获取各种指标
        Integer areaCountA = areaUtils.getOrganAreaCount(jsonTask, "10906E");// A腺泡细胞核数量
        BigDecimal organAreaB = areaUtils.getOrganArea(jsonTask, "10906F");// B导管面积
        BigDecimal organAreaC = areaUtils.getOrganArea(jsonTask, "109003");// C血管面积
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());// D组织轮廓

        // 算法输出指标
        resultsMap.put("腺泡细胞核数量", createIndicator(areaCountA, PIECE));
        resultsMap.put("导管面积", createIndicator(areaUtils.convertToSquareMicrometer(organAreaB.toString()), SQ_UM_THOUSAND));
        resultsMap.put("血管面积", createIndicator(areaUtils.convertToSquareMicrometer(organAreaC.toString()), SQ_UM_THOUSAND));

        // 计算指标
        BigDecimal nucleusResult = getNucleusResult(areaCountA, slideArea);// A/D


        BigDecimal ares = BigDecimal.valueOf(Double.parseDouble(slideArea));
        // 血管面积占比
        BigDecimal vesselArea = commonJsonParser.getProportion(organAreaC, ares);
        // 导管面积占比
        BigDecimal ductsArea = commonJsonParser.getProportion(organAreaB, ares);

        // 产品呈现指标
        resultsMap.put("腺泡细胞核密度", createNameIndicator("Nucleus density of acinar cell", nucleusResult, SQ_MM_PIECE));
        resultsMap.put("血管面积占比", createNameIndicator("Vessel area%", vesselArea, PERCENTAGE));
        resultsMap.put("导管面积占比", createNameIndicator("Ducts area%", ductsArea, PERCENTAGE));
        resultsMap.put("腮腺面积", createNameIndicator("Parotid gland area", slideArea, SQ_MM));


        aiForecastService.addAiForecast(jsonTask.getSingleId(), resultsMap);
    }

    /**
     * 计算指标
     */
    private static BigDecimal getNucleusResult(Integer areaCountA, String slideArea) {
        return (0 == areaCountA) ? BigDecimal.ZERO
                : new BigDecimal(areaCountA).divide(new BigDecimal(slideArea), 3, RoundingMode.HALF_UP);
    }

    @Override
    public String getAlgorithmCode() {
        return "Parotid_gland";
    }
}
