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
 * 膀胱-UB
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
    	try{
	        Map<String, IndicatorAddIn>  resultsMap = new HashMap<>();
	        log.info("UrinaryBladderParserStrategyImpl start-2");

            // 获取各种指标
            BigDecimal organAreaA = areaUtils.getOrganArea(jsonTask, "11E034");// A 膀胱腔面积
	        String accurateAreaB = areaUtils.getFineContourArea(jsonTask.getSingleId()); // B 组织轮廓面积
            BigDecimal organAreaC = areaUtils.getOrganArea(jsonTask,"11E035");// C 黏膜上皮面积
            BigDecimal organAreaD = areaUtils.getOrganArea(jsonTask,"11E037");// D 黏膜固有层+黏膜下层面积
            Integer areaCountE = areaUtils.getOrganAreaCount(jsonTask, "11E036");// E 黏膜上皮细胞核数量

            // 算法输出指标
            resultsMap.put("膀胱腔面积", createIndicator(organAreaA, SQ_MM));
            resultsMap.put("组织轮廓面积", createIndicator(accurateAreaB, SQ_MM));
            resultsMap.put("黏膜上皮面积", createIndicator(organAreaC, SQ_MM));
            resultsMap.put("黏膜固有层+黏膜下层面积", createIndicator(organAreaD, SQ_MM));
            resultsMap.put("黏膜上皮细胞核数量", createIndicator(areaCountE, PIECE));

            // 计算指标
            String result = getArea(accurateAreaB, organAreaA);

            // 产品呈现指标
            resultsMap.put("膀胱面积", createNameIndicator("Urinary bladder area", result, SQ_MM));
	        aiForecastService.addAiForecast(jsonTask.getSingleId(),  resultsMap);
    	}catch(Exception e){
    		 log.info("UrinaryBladderParserStrategyImpl start-2:{}",e);
    		e.printStackTrace();
    	}
    	log.info("UrinaryBladderParserStrategyImpl end");
    }

    /**
     * 计算指标
     */
    private String getArea(String accurateAreaB, BigDecimal organAreaA) {
        String result = "";
        if(!"0".equals(accurateAreaB)){
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
