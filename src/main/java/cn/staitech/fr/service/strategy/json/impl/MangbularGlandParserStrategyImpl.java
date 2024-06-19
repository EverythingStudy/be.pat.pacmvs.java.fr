package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
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
 * 颌下腺-MD
 */
@Slf4j
@Service("Mangbular_gland")
public class MangbularGlandParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.info("MangbularGlandParserStrategyImpl init");
    }

    /**
     结构	编码
     颗粒管（红色）	10B125
     黏液腺细胞核	10B128
     颗粒管细胞核	10B126
     有血管壁的血管	10B003
     红细胞	10B004
     间质	10B027
     小叶间导管	10B115
     组织轮廓	10B111
     算法输出指标	指标代码（仅限本文档）	单位（保留小数点后三位）	备注
     颗粒管（红色）数量	A	个	无
     黏液腺细胞核数量	B	个	无
     颗粒管细胞核数量（单个）	C	个	无
     有血管壁的血管面积	D	平方毫米	数据相加输出
     有血管壁的血管数量	E	个	无
     红细胞面积	F	平方毫米	数据相加输出
     颗粒管（红色）面积（单个）	G	103平方微米	无
     组织轮廓	H	平方毫米	无
     颗粒管（红色）面积（全片）	I	平方毫米	数据相加输出

     产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后三位）	English	计算方式	备注
     颗粒管（红色）密度	1	个/平方毫米	Density of granular convoluted tubules (eosinophilic) 	1=A/H
     黏液腺细胞核密度	2	个/平方毫米	Nucleus density of mucous gland	2=B/H
     颗粒管细胞核密度(单个颗粒管)	3	个/平方毫米	Nucleus density of granular convoluted tubule (per)	3=C/G	以95%置信区间和均数±标准差呈现
     血管面积占比	4	%	Vessel area%	4=D/H	运算前注意统一单位；
     即有血管壁的血管面积
     红细胞面积占比	5	%	Erythrocyte area%	5=F/H	运算前注意统一单位
     颌下腺面积	6	平方毫米	Submadibular gland
     area	6=H
     颗粒管面积占比（全片）	7	%	Granular convoluted tubules area% (all)	7=I/H
     */

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> resultsMap = new HashMap<>();

        // 获取各种指标
        Integer organAreaCountA = areaUtils.getOrganAreaCount(jsonTask, "10B125");// A颗粒管（红色）数量
        Integer organAreaCountB = areaUtils.getOrganAreaCount(jsonTask, "10B128");// B黏液腺细胞核数量
        BigDecimal organAreaD = areaUtils.getOrganArea(jsonTask, "10B003");// D有血管壁的血管面积
        Integer organAreaCountE = areaUtils.getOrganAreaCount(jsonTask, "10B003");// E有血管壁的血管数量
        BigDecimal organAreaF = areaUtils.getOrganArea(jsonTask, "10B004");// F红细胞面积
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId()); // H组织轮廓
        BigDecimal organAreaH = BigDecimal.valueOf(Long.parseLong(slideArea));
        BigDecimal organAreaI = areaUtils.getOrganArea(jsonTask, "10B125");// I颗粒管（红色）面积（全片）
        // todo C颗粒管细胞核数量（单个）10B126

        // 算法输出指标
        resultsMap.put("颗粒管（红色）数量", createIndicator(organAreaCountA, PIECE));
        resultsMap.put("黏液腺细胞核数量", createIndicator(organAreaCountB, PIECE));
        resultsMap.put("有血管壁的血管面积", createIndicator(organAreaD, SQ_MM));
        resultsMap.put("有血管壁的血管数量", createIndicator(organAreaCountE, PIECE));
        resultsMap.put("红细胞面积", createIndicator(organAreaF, SQ_MM));
        resultsMap.put("颗粒管（红色）面积（单个）", createDefaultIndicator());// G颗粒管（红色）面积（单个）
        resultsMap.put("颗粒管（红色）面积（全片）", createIndicator(organAreaI, SQ_MM));// I颗粒管（红色）面积（全片）

        // 计算指标
        BigDecimal densityResult = getDensityResult(organAreaCountA, slideArea);// A/H
        BigDecimal nucleusResult = getDensityResult(organAreaCountB, slideArea);// B/H
        // 颗粒管细胞核密度(单个颗粒管) 置信区---
        // 血管面积占比
        BigDecimal vesselArea = commonJsonParser.getProportion(organAreaD, organAreaH);
        // 红细胞面积占比
        BigDecimal erythrocyteArea = commonJsonParser.getProportion(organAreaF, organAreaH);
        // 颗粒管面积占比（全片）
        BigDecimal granularConvolutedTubulesArea = commonJsonParser.getProportion(organAreaI, organAreaH);


        // 产品呈现指标
        resultsMap.put("颌下腺面积", createNameIndicator("Submadibular gland area", slideArea, SQ_MM));
        resultsMap.put("颗粒管（红色）密度", createNameIndicator("Density of granular convoluted tubules (eosinophilic)", densityResult, SQ_MM_PIECE));
        resultsMap.put("黏液腺细胞核密度", createNameIndicator("Nucleus density of mucous gland", nucleusResult, SQ_MM_PIECE));
//        resultsMap.put("颗粒管细胞核密度(单个颗粒管)", createNameIndicator("Nucleus density of granular convoluted tubule (per)", getDensityResult(organAreaCountC, slideArea), SQ_MM_PIECE));
        resultsMap.put("血管面积占比", createNameIndicator("Vessel area%", vesselArea, PERCENTAGE));
        resultsMap.put("红细胞面积占比", createNameIndicator("Erythrocyte area%", erythrocyteArea, PERCENTAGE));
        resultsMap.put("颗粒管面积占比（全片）", createNameIndicator("Granular convoluted tubules area% (all)", granularConvolutedTubulesArea, PERCENTAGE));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), resultsMap);
    }

    /**
     * 计算指标
     * @return organAreaCount/slideArea结果
     */
    private BigDecimal getDensityResult(Integer organAreaCount, String slideArea) {
        return (0 == organAreaCount) ? BigDecimal.ZERO
                : new BigDecimal(organAreaCount).divide(new BigDecimal(slideArea), 3, RoundingMode.HALF_UP);
    }

    @Override
    public String getAlgorithmCode() {
        return "Mangbular_gland";
    }
}
