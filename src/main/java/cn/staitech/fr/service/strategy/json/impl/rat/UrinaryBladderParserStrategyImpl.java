package cn.staitech.fr.service.strategy.json.impl.rat;

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
public class UrinaryBladderParserStrategyImpl extends AbstractCustomParserStrategy {
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

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("UrinaryBladderParserStrategyImpl start");
        try {
            Map<String, IndicatorAddIn> resultsMap = new HashMap<>();
            log.info("UrinaryBladderParserStrategyImpl start-2");

            // 获取各种指标
            // A 膀胱腔面积
            BigDecimal organAreaA = getOrganArea(jsonTask, "11E034").getStructureAreaNum();
            // B 组织轮廓面积
            String accurateAreaB = areaUtils.getFineContourArea(jsonTask.getSingleId());
            BigDecimal organAreaB = BigDecimal.valueOf(Double.parseDouble(accurateAreaB));
            // C 黏膜上皮面积
            BigDecimal organAreaC = getOrganArea(jsonTask, "11E035").getStructureAreaNum();
            // D 黏膜固有层+黏膜下层面积
            BigDecimal organAreaD = getOrganArea(jsonTask, "11E037").getStructureAreaNum();
            // E 黏膜上皮细胞核数量
            Integer areaCountE = getOrganAreaCount(jsonTask, "11E036");
            // F 血管面积
            BigDecimal organAreaF = getOrganArea(jsonTask, "11E003").getStructureAreaNum();
            // G 血管外红细胞
            BigDecimal organAreaG = getInsideOrOutside(jsonTask, "11E003", "11E004", false).getStructureAreaNum();
            // H 血管内红细胞
            BigDecimal organAreaH = getInsideOrOutside(jsonTask, "11E003", "11E004", true).getStructureAreaNum();

            // 算法输出指标
            resultsMap.put("膀胱腔面积", createIndicator(organAreaA.setScale(3, RoundingMode.UP), SQ_MM, "11E034"));
            //resultsMap.put("组织轮廓面积", createIndicator(organAreaB.setScale(3, RoundingMode.UP), SQ_MM, "11E111"));
            resultsMap.put("黏膜上皮面积", createIndicator(organAreaC.setScale(3, RoundingMode.UP), SQ_MM, "11E035"));
            resultsMap.put("黏膜固有层+黏膜下层面积", createIndicator(organAreaD.setScale(3, RoundingMode.UP), SQ_MM, "11E037"));
            resultsMap.put("黏膜上皮细胞核数量", createIndicator(areaCountE, PIECE, "11E036"));
            resultsMap.put("血管面积", createIndicator(organAreaF.setScale(3, RoundingMode.UP), SQ_MM, "11E003"));
            resultsMap.put("血管外红细胞面积", createIndicator(organAreaG.setScale(3, RoundingMode.UP), SQ_MM, "11E003,11E004"));
            resultsMap.put("血管内红细胞面积", createIndicator(organAreaH.setScale(3, RoundingMode.UP), SQ_MM, "11E003,11E004"));

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
            resultsMap.put("黏膜固有层和黏膜下层面积占比", createNameIndicator("Lamina propria and submucosa area %", laminaPropriaAndSubmucosaArea, PERCENTAGE, "11E037,11E111,11E034"));
            resultsMap.put("黏膜上皮细胞核密度", createNameIndicator("Nucleus density of mucosal epithelial nucleus", nucleusDensityOfMucosalEpithelialNucleus, SQ_MM_PIECE, "11E036,11E035"));
            resultsMap.put("血管面积占比", createNameIndicator("Vessel area %", vesselArea, PERCENTAGE, "11E003,11E111,11E034"));
            resultsMap.put("血管外红细胞面积占比", createNameIndicator("Extravascular erythrocyte area%", extravascularErythrocyteArea, PERCENTAGE, "11E003,11E004,11E111,11E034"));
            resultsMap.put("血管内红细胞面积占比", createNameIndicator("Intravascular erythrocyte area%", intravascularErythrocyteArea, PERCENTAGE, "11E003,11E004,11E111,11E034"));
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
