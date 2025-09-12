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
 * 大鼠-膀胱-UB
 */
@Slf4j
@Service("Urinary_bladder")
public class UrinaryBladderParserStrategyImpl extends AbstractCustomParserStrategy  {
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
        log.info("UrinaryBladderParserStrategyImpl init");
    }


    /**
     * 结构	编码
     * 膀胱腔	11E034
     * 黏膜上皮	11E035
     * 黏膜固有层+黏膜下层	11E037
     * 黏膜上皮细胞核	11E036
     * 膀胱血管	11E003
     * 红细胞	00E004
     * 组织轮廓	11E111
     * 算法输出指标	指标代码（仅限本文档）	单位（保留小数点后三位）	备注
     * 膀胱腔面积	A	平方毫米
     * 组织轮廓面积	B	平方毫米
     * 黏膜上皮面积	C	平方毫米
     * 黏膜固有层+黏膜下层面积	D	平方毫米
     * 黏膜上皮细胞核数量	E	个
     * 血管面积	F	平方毫米	数据相加输出
     * 血管外红细胞面积	G	平方毫米	数据相加输出
     * 血管内红细胞面积	H	平方毫米	数据相加输出
     * <p>
     * 产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后三位）	English	计算方式	备注
     * 黏膜上皮面积占比	1	%	Mucosa epithelium area %	1=C/(B-A）
     * 黏膜固有层和黏膜下层面积占比	2	%	Lamina propria and submucosa area %	2=D/(B-A)
     * 黏膜上皮细胞核密度	3	个/平方毫米	Nucleus density of mucosal epithelial nucleus	3=E/C
     * 血管面积占比	4	%	Vessel area %	4=F/(B-A)
     * 血管外红细胞面积占比	5	%	Extravascular erythrocyte area%	5=G/(B-A)
     * 血管内红细胞面积占比	6	%	Intravascular erythrocyte area%	6=H/(B-A)
     * 膀胱面积	7	平方毫米	Urinary bladder area	7=B-A
     */
    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("UrinaryBladderParserStrategyImpl start");
        try {
            Map<String, IndicatorAddIn> resultsMap = new HashMap<>();
            log.info("UrinaryBladderParserStrategyImpl start-2");

            // 获取各种指标
            // A 膀胱腔面积
            BigDecimal organAreaA = getOrganArea(jsonTask, "11E034").getStructureAreaNum();
            String accurateAreaB = areaUtils.getFineContourArea(jsonTask.getSingleId()); // B 组织轮廓面积
            BigDecimal organAreaB = BigDecimal.valueOf(Double.parseDouble(accurateAreaB));
            // C 黏膜上皮面积
            BigDecimal organAreaC = getOrganArea(jsonTask, "11E035").getStructureAreaNum();
            // D 黏膜固有层+黏膜下层面积
            BigDecimal organAreaD = getOrganArea(jsonTask, "11E037").getStructureAreaNum();
            // E 黏膜上皮细胞核数量
            Integer areaCountE = getOrganAreaCount(jsonTask, "11E036");
            BigDecimal organAreaF = getOrganArea(jsonTask, "11E003").getStructureAreaNum();
            // 血管外红细胞
            BigDecimal organAreaG = getInsideOrOutside(jsonTask, "11E003", "00E004", false).getStructureAreaNum();
            // 血管内红细胞
            BigDecimal organAreaH = getInsideOrOutside(jsonTask, "11E003", "00E004", true).getStructureAreaNum();

            // 算法输出指标
            resultsMap.put("膀胱腔面积", createIndicator(organAreaA.setScale(3, RoundingMode.UP), SQ_MM, "11E034"));
            //resultsMap.put("组织轮廓面积", createIndicator(organAreaB.setScale(3, RoundingMode.UP), SQ_MM, "11E111"));
            resultsMap.put("黏膜上皮面积", createIndicator(organAreaC.setScale(3, RoundingMode.UP), SQ_MM, "11E035"));
//            resultsMap.put("黏膜固有层+黏膜下层面积", createIndicator(organAreaD.setScale(3, RoundingMode.UP), SQ_MM, "11E037"));
//            resultsMap.put("黏膜上皮细胞核数量", createIndicator(areaCountE, PIECE, "11E036"));
//            resultsMap.put("血管面积", createIndicator(organAreaF.setScale(3, RoundingMode.UP), SQ_MM, "11E003"));
//            resultsMap.put("血管外红细胞面积", createIndicator(organAreaG.setScale(3, RoundingMode.UP), SQ_MM, "11E003,00E004"));
//            resultsMap.put("血管内红细胞面积", createIndicator(organAreaH.setScale(3, RoundingMode.UP), SQ_MM, "11E003,00E004"));

            // 计算指标

            //黏膜上皮面积占比
            BigDecimal mucosaEpitheliumArea = getProportion(organAreaC, organAreaB.subtract(organAreaA));
            // 黏膜固有层和黏膜下层面积占比
            BigDecimal laminaPropriaAndSubmucosaArea = getProportion(organAreaD, organAreaB.subtract(organAreaA));
            // 黏膜上皮细胞核密度
            BigDecimal nucleusDensityOfMucosalEpithelialNucleus = commonJsonParser.bigDecimalDivideCheck(BigDecimal.valueOf(areaCountE), organAreaC);
            // 血管面积占比
            BigDecimal vesselArea = getProportion(organAreaF, organAreaB.subtract(organAreaA));
            // 血管外红细胞面积占比
            BigDecimal extravascularErythrocyteArea = getProportion(organAreaG, organAreaB.subtract(organAreaA));
            // 血管内红细胞面积占比
            BigDecimal intravascularErythrocyteArea = getProportion(organAreaH, organAreaB.subtract(organAreaA));
            String result = getArea(accurateAreaB, organAreaA);

            // 产品呈现指标
            resultsMap.put("黏膜上皮面积占比", createNameIndicator("Mucosa epithelium area %", mucosaEpitheliumArea, PERCENTAGE, "11E035,11E111,11E034"));
            //resultsMap.put("黏膜固有层和黏膜下层面积占比", createNameIndicator("Lamina propria and submucosa area %", laminaPropriaAndSubmucosaArea, PERCENTAGE, "11E037,11E111,11E034"));
            //resultsMap.put("黏膜上皮细胞核密度", createNameIndicator("Nucleus density of mucosal epithelial nucleus", nucleusDensityOfMucosalEpithelialNucleus, SQ_MM_PIECE, "11E036,11E035"));
            //resultsMap.put("血管面积占比", createNameIndicator("Vessel area %", vesselArea, PERCENTAGE, "11E003,11E111,11E034"));
            //resultsMap.put("血管外红细胞面积占比", createNameIndicator("Extravascular erythrocyte area%", extravascularErythrocyteArea, PERCENTAGE, "11E003,00E004,11E111,11E034"));
            // resultsMap.put("血管内红细胞面积占比", createNameIndicator("Intravascular erythrocyte area%", intravascularErythrocyteArea, PERCENTAGE, "11E003,00E004,11E111,11E034"));
            resultsMap.put("膀胱面积", createNameIndicator("Urinary bladder area", result, SQ_MM, "11E111,11E034"));
            aiForecastService.addAiForecast(jsonTask.getSingleId(), resultsMap);
        } catch (Exception e) {
            log.info("UrinaryBladderParserStrategyImpl start-2:{}", e);
            e.printStackTrace();
        }
        log.info("UrinaryBladderParserStrategyImpl end");
    }

    /**
     * 计算指标
     */
    private String getArea(String accurateAreaB, BigDecimal organAreaA) {
        String result = "0";
        if (!"0".equals(accurateAreaB)) {
            BigDecimal areaNum = new BigDecimal(accurateAreaB).subtract(organAreaA);
            result = areaNum.setScale(3, RoundingMode.HALF_UP).toString();// B-A
        }
        return result;
    }

    @Override
    public String getAlgorithmCode() {
        return "Urinary_bladder";
    }
}
