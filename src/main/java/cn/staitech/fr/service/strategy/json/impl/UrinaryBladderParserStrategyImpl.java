package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
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
    
    @PostConstruct
    public void init() {
         setCommonJsonParser(commonJsonParser);
        log.info("UrinaryBladderParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
    	 log.info("UrinaryBladderParserStrategyImpl start");
    	try{
	        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
	        log.info("UrinaryBladderParserStrategyImpl start-2");

            // 获取各种指标
            BigDecimal organAreaA = areaUtils.getOrganArea(jsonTask, "11E034");// A 膀胱腔面积
	        String accurateAreaB = areaUtils.getFineContourArea(jsonTask.getSingleId()); // B 组织轮廓面积
            BigDecimal organAreaC = areaUtils.getOrganArea(jsonTask,"11E035");// C 黏膜上皮面积
            BigDecimal organAreaD = areaUtils.getOrganArea(jsonTask,"11E037");// D 黏膜固有层+黏膜下层面积
            Integer areaCountE = areaUtils.getOrganAreaCount(jsonTask, "11E036");// E 黏膜上皮细胞核数量

            // 算法输出指标
            indicatorResultsMap.put("膀胱腔面积", new IndicatorAddIn("", organAreaA.toString(), "平方毫米", "1"));
            indicatorResultsMap.put("黏膜上皮面积", new IndicatorAddIn("", organAreaC.toString(), "平方毫米", "1"));
            indicatorResultsMap.put("黏膜固有层+黏膜下层面积", new IndicatorAddIn("", organAreaD.toString(), "平方毫米", "1"));
            indicatorResultsMap.put("黏液腺细胞核数量", new IndicatorAddIn("", areaCountE.toString(), "个", "1"));
            /*indicatorResultsMap.put("血管面积", new IndicatorAddIn("", , "平方毫米", "1"));
            indicatorResultsMap.put("血管外红细胞面积", new IndicatorAddIn("", , "平方毫米", "1"));//11E004
            indicatorResultsMap.put("血管内红细胞面积", new IndicatorAddIn("", , "平方毫米", "1"));*/

            // 计算指标
            String result = "";
            if(!"0".equals(accurateAreaB)){
                BigDecimal areaNum = new BigDecimal(accurateAreaB).subtract(organAreaA);
                result = areaNum.setScale(3, RoundingMode.HALF_UP).toString();// B-A
            }

            // 产品呈现指标
	        indicatorResultsMap.put("膀胱面积", new IndicatorAddIn("Urinary bladder area", result, "平方毫米"));
            /*
	        indicatorResultsMap.put("黏膜上皮面积占比", new IndicatorAddIn("Mucosa epithelium area %", "", ""));
	        indicatorResultsMap.put("黏膜固有层和黏膜下层面积占比", new IndicatorAddIn("Lamina propria and submucosa area %", "", ""));
	        indicatorResultsMap.put("黏膜上皮细胞核密度", new IndicatorAddIn("Nucleus density of mucosal epithelial nucleus", "", ""));
	        indicatorResultsMap.put("血管面积占比", new IndicatorAddIn("Vessel area %", "", ""));
	        indicatorResultsMap.put("血管外红细胞面积占比", new IndicatorAddIn("Extravascular erythrocyte area%", "", ""));
	        indicatorResultsMap.put("血管内红细胞面积占比", new IndicatorAddIn("Intravascular erythrocyte area%", "", ""));
	        */

	        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    	}catch(Exception e){
    		 log.info("UrinaryBladderParserStrategyImpl start-2:{}",e);
    		e.printStackTrace();
    	}
    	log.info("UrinaryBladderParserStrategyImpl end");
    }

    @Override
    public String getAlgorithmCode() {
        return "Urinary_bladder";
    }
}
