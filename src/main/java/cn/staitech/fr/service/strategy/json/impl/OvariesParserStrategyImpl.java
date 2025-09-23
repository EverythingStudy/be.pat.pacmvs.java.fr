package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 
* @ClassName: OvariesOviductParserStrategyImpl
* @Description-d:卵巢
* @author wanglibei
* @date 2025年7月22日
* @version V1.0
 */
@Slf4j
@Component("Ovaries")
public class OvariesParserStrategyImpl extends AbstractCustomParserStrategy {
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
		log.info("OvariesParserStrategyImpl init");
	}

	@Override
	public void alculationIndicators(JsonTask jsonTask) {
		log.info("大鼠卵巢构指标计算开始");

		// 黄体:1240CA 
		//红细胞:124004 
		//卵泡:1240CB 
		//血管:124003 
		//组织轮廓:124111


		//AI指标保存

		// 黄体数量 1 个 Corpus luteum numbers 1=A
		// 黄体面积（全片） 2 平方毫米 Corpus luteum area(all) 2=C
		// 卵泡数量 3 个 Follicle numbers 3=D
		// 卵泡面积（全片） 4 平方毫米 Follicle area 4=F
		// 血管面积 3 平方微米 Vessel area 3=H
		// 血管外红细胞面积 4 平方微米 Extravascular Erythrocyte area 4=I
		// 血管内红细胞面积 5 平方微米 Intravascular Erythrocyte area 5=J


		// 黄体数量 A 个
		Integer mucosaCountA = commonJsonParser.getOrganAreaCount(jsonTask, "1240CA");
		mucosaCountA = commonJsonParser.getIntegerValue(mucosaCountA);
		// 黄体面积（全片） C 平方毫米
		BigDecimal bigDecimalC = getOrganArea(jsonTask, "1240CA").getStructureAreaNum();
		bigDecimalC = bigDecimalC.setScale(3, RoundingMode.HALF_UP);
		// 卵泡数量 D 个
		Integer mucosaCountD = commonJsonParser.getOrganAreaCount(jsonTask, "1240CB");
		mucosaCountD = commonJsonParser.getIntegerValue(mucosaCountD);
		// 卵泡面积（全片） F 平方毫米
		BigDecimal bigDecimalF =  getOrganArea(jsonTask, "1240CB").getStructureAreaNum();
		bigDecimalF = bigDecimalF.setScale(3, RoundingMode.HALF_UP);
		// 血管面积 H 平方微米
		BigDecimal bigDecimalH =  getOrganArea(jsonTask, "124003").getStructureAreaNum();
		//平方毫米转平方微米
		if(null != bigDecimalH){
			String bigDecimalASecondStr = areaUtils.convertToMicrometer(bigDecimalH.toString());
			bigDecimalH = new BigDecimal(bigDecimalASecondStr);
		}
		//TODO 血管外红细胞面积 I 平方微米
		BigDecimal bigDecimalI = BigDecimal.ZERO;
		bigDecimalI = commonJsonParser.getInsideOrOutside(jsonTask, "124003", "124004", false).getStructureAreaNum();
		//平方毫米转平方微米
		if(null != bigDecimalI){
			String bigDecimalASecondStr = areaUtils.convertToMicrometer(bigDecimalI.toString());
			bigDecimalI = new BigDecimal(bigDecimalASecondStr);
		}
		//TODO 血管内红细胞面积 J 平方微米
		BigDecimal bigDecimalJ = BigDecimal.ZERO;
		bigDecimalJ = commonJsonParser.getInsideOrOutside(jsonTask, "124003", "124004", true).getStructureAreaNum();
		//平方毫米转平方微米
		if(null != bigDecimalJ){
			String bigDecimalASecondStr = areaUtils.convertToMicrometer(bigDecimalJ.toString());
			bigDecimalJ = new BigDecimal(bigDecimalASecondStr);
		}

		// 组织轮廓面积 E 平方毫米
		String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());
		BigDecimal bigDSlideArea = BigDecimal.ZERO;
		if(StringUtils.isNotEmpty(slideArea)){
			bigDSlideArea = new BigDecimal(slideArea);
		}
		
		/**
			黄体数量	1240CA
			黄体面积（全片）	1240CA
			卵泡数量	1240CB
			卵泡面积（全片）	1240CB
			血管面积	124003
			血管外红细胞面积	124003、124004
			血管内红细胞面积	124003、124004
			
			无
		 */

		//算法保存
		Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
		if(mucosaCountA > 0){
			indicatorResultsMap.put("黄体数量", new IndicatorAddIn("Corpus luteum numbers", String.valueOf(mucosaCountA), PIECE, "0","1240CA"));
		}
		if(bigDecimalC.compareTo(BigDecimal.ZERO) != 0) {
			indicatorResultsMap.put("黄体面积（全片）", new IndicatorAddIn("Corpus luteum area(all)", String.valueOf(bigDecimalC.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "0","1240CA"));
		}
		if(mucosaCountD > 0){
//			indicatorResultsMap.put("卵泡数量", new IndicatorAddIn("Follicle numbers", String.valueOf(mucosaCountD), PIECE, "0","1240CB"));
		}


		if(bigDecimalF.compareTo(BigDecimal.ZERO) != 0) {
//			indicatorResultsMap.put("卵泡面积（全片）", new IndicatorAddIn("Follicle area", String.valueOf(bigDecimalF.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "0","1240CB"));
		}

		if(bigDecimalH.compareTo(BigDecimal.ZERO) != 0) {
//			indicatorResultsMap.put("血管面积", new IndicatorAddIn("Vessel area", String.valueOf(bigDecimalH.setScale(3, RoundingMode.HALF_UP)), SQ_UM, "0","124003"));
		}

		if(bigDecimalI.compareTo(BigDecimal.ZERO) != 0) {
//			indicatorResultsMap.put("血管外红细胞面积", new IndicatorAddIn("Extravascular Erythrocyte area", String.valueOf(bigDecimalI.setScale(3, RoundingMode.HALF_UP)), SQ_UM, "0",areaUtils.getStructureIds("124003","124004")));
		}
		if(bigDecimalJ.compareTo(BigDecimal.ZERO) != 0) {
//			indicatorResultsMap.put("血管内红细胞面积", new IndicatorAddIn("Intravascular Erythrocyte area", String.valueOf(bigDecimalJ.setScale(3, RoundingMode.HALF_UP)), SQ_UM, "0",areaUtils.getStructureIds("124003","124004")));
		}

		if(StringUtils.isNotEmpty(slideArea)) {
//			indicatorResultsMap.put("组织轮廓面积", new IndicatorAddIn("", String.valueOf(bigDSlideArea.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "1","124111"));
		}




		aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);

	}

	@Override
	public String getAlgorithmCode() {
		return "Ovaries";
	}
}
