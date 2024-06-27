package cn.staitech.fr.service.strategy.json.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import org.springframework.stereotype.Component;

import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wanglibei
 * @version V1.0
 * @ClassName: PituitaryParserStrategyImpl
 * @Description:大鼠垂体-7I
 * @date 2024年5月13日
 */
@Slf4j
@Component("Pituitary")
public class PituitaryParserStrategyImpl extends AbstractCustomParserStrategy {
	@Resource
	private AreaUtils areaUtils;
	@Resource
	private AiForecastService aiForecastService;
	@Resource
	private CommonJsonParser commonJsonParser;
	@Resource
	private CommonJsonCheck commonJsonCheck;
	@PostConstruct
	public void init() {
		setCommonJsonParser(commonJsonParser);
		setCommonJsonCheck(commonJsonCheck);
		log.info("PituitaryParserStrategyImpl init");
	}

	@Override
	public void alculationIndicators(JsonTask jsonTask) {

		log.info("大鼠垂体构指标计算开始");

		// 神经部: 10607F
		// 神经部细胞核（胶质细胞）: 106080
		// 中间部: 106081
		// 中间部细胞核:（嫌色细胞或嗜碱性细胞） 106082
		// 远侧部: 106083
		// 远侧部细胞核（嗜酸性细胞、嗜碱性细胞、嫌色细胞）: 106084
		// 红细胞: 106004
		// 组织轮廓 :106111

		// 神经部面积 A 平方毫米 若多个数据则相加输出
		//BigDecimal pituitaryA = commonJsonParser.getOrganArea(jsonTask, "10607F").getStructureAreaNum();
		BigDecimal pituitaryA = getOrganArea(jsonTask, "10607F").getStructureAreaNum();
		pituitaryA =  pituitaryA.setScale(3, RoundingMode.HALF_UP);
		pituitaryA = commonJsonParser.getBigDecimalValue(pituitaryA);
		// 中间部面积 B 平方毫米 若多个数据则相加输出
//		BigDecimal pituitaryB = commonJsonParser.getOrganArea(jsonTask, "106081").getStructureAreaNum();
		BigDecimal pituitaryB = getOrganArea(jsonTask, "106081").getStructureAreaNum();
		pituitaryB =  pituitaryB.setScale(3, RoundingMode.HALF_UP);
		pituitaryB = commonJsonParser.getBigDecimalValue(pituitaryB);
		// 远侧部面积 C 平方毫米 若多个数据则相加输出
//		BigDecimal pituitaryC = commonJsonParser.getOrganArea(jsonTask, "106083").getStructureAreaNum();
		BigDecimal pituitaryC = getOrganArea(jsonTask, "106083").getStructureAreaNum();
		pituitaryC =  pituitaryC.setScale(3, RoundingMode.HALF_UP);
		pituitaryC = commonJsonParser.getBigDecimalValue(pituitaryC);
		// 红细胞面积 D 平方毫米 数据相加输出
//		BigDecimal pituitaryD = commonJsonParser.getOrganArea(jsonTask, "106004").getStructureAreaNum();
		BigDecimal pituitaryD = getOrganArea(jsonTask, "106004").getStructureAreaNum();
		pituitaryD =  pituitaryD.setScale(3, RoundingMode.HALF_UP);
		pituitaryD = commonJsonParser.getBigDecimalValue(pituitaryD);
		// 胸骨面积 ==>组织轮廓面积H
		//		BigDecimal pituitaryH = commonJsonParser.getOrganArea(jsonTask, "106111").getStructureAreaNum();
		String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());
		BigDecimal pituitaryH = BigDecimal.ZERO;
		pituitaryH = new BigDecimal(slideArea);
		// 神经部细胞核数量 E 个 无
		Integer mucosaCountE = commonJsonParser.getOrganAreaCount(jsonTask, "106080");
		// 中间部细胞核数量 F 个 无
		Integer mucosaCountF = commonJsonParser.getOrganAreaCount(jsonTask, "106082");
		// 远侧部细胞核数量 G 个 无
		Integer mucosaCountG = commonJsonParser.getOrganAreaCount(jsonTask, "106084");

		Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();

		//		if(pituitaryA.compareTo(BigDecimal.ZERO) != 0) {
		indicatorResultsMap.put("神经部面积", new IndicatorAddIn("", String.valueOf(pituitaryA), "平方毫米", "1"));
		//		}
		//		if(pituitaryB.compareTo(BigDecimal.ZERO) != 0) {
		indicatorResultsMap.put("中间部面积", new IndicatorAddIn("", String.valueOf(pituitaryB), "平方毫米", "1"));
		//		}
		//		if(pituitaryC.compareTo(BigDecimal.ZERO) != 0) {
		indicatorResultsMap.put("远侧部面积", new IndicatorAddIn("", String.valueOf(pituitaryC), "平方毫米", "1"));
		//		}
		//		if(pituitaryD.compareTo(BigDecimal.ZERO) != 0) {
		indicatorResultsMap.put("红细胞面积", new IndicatorAddIn("", String.valueOf(pituitaryD), "平方毫米", "1"));
		//		}
		//		if(pituitaryH.compareTo(BigDecimal.ZERO) != 0) {
		//			indicatorResultsMap.put("组织轮廓面积", new IndicatorAddIn("", String.valueOf(pituitaryH), "平方毫米", "1"));
		indicatorResultsMap.put("垂体面积", new IndicatorAddIn("Pituitary gland area", String.valueOf(pituitaryH), "平方毫米", "0"));
		//		}

		//		if(mucosaCountE > 0){
		indicatorResultsMap.put("神经部细胞核数量", new IndicatorAddIn("", String.valueOf(mucosaCountE), "个", "1"));
		//		}
		//		if(mucosaCountF > 0){
		indicatorResultsMap.put("中间部细胞核数量", new IndicatorAddIn("", String.valueOf(mucosaCountF), "个", "1"));
		//		}
		//		if(mucosaCountG > 0){
		indicatorResultsMap.put("远侧部细胞核数量", new IndicatorAddIn("", String.valueOf(mucosaCountG), "个", "1"));
		//		}

		//		神经部面积占比	1	%	Pars nervosa area%	1=A/H
		if(pituitaryA.compareTo(BigDecimal.ZERO) != 0 && pituitaryH.compareTo(BigDecimal.ZERO) != 0){
			BigDecimal pituitaryA_H = commonJsonParser.getProportion(pituitaryA, pituitaryH);
			indicatorResultsMap.put("神经部面积占比", new IndicatorAddIn("Pars nervosa area%", String.valueOf(pituitaryA_H), "%", "0"));
		}
		//				中间部面积占比	2	%	Pars intermedia area%	2=B/H
		if(pituitaryB.compareTo(BigDecimal.ZERO) != 0 && pituitaryH.compareTo(BigDecimal.ZERO) != 0){
			BigDecimal pituitaryB_H =  commonJsonParser.getProportion(pituitaryB, pituitaryH);
			indicatorResultsMap.put("中间部面积占比", new IndicatorAddIn("Pars intermedia area%", String.valueOf(pituitaryB_H), "%", "0"));
		}
		//				远侧部面积占比	3	%	Pars distalis area%	3=C/H
		if(pituitaryC.compareTo(BigDecimal.ZERO) != 0 && pituitaryH.compareTo(BigDecimal.ZERO) != 0){
			BigDecimal pituitaryC_H = commonJsonParser.getProportion(pituitaryC, pituitaryH);
			indicatorResultsMap.put("远侧部面积占比", new IndicatorAddIn("Pars distalis area%", String.valueOf(pituitaryC_H), "%", "0"));
		}
		//		
		//				红细胞面积占比	4	%	Erythrocyte area%	4=D/H
		if(pituitaryD.compareTo(BigDecimal.ZERO) != 0 && pituitaryH.compareTo(BigDecimal.ZERO) != 0){
			BigDecimal pituitaryD_H = commonJsonParser.getProportion(pituitaryD, pituitaryH); 
			indicatorResultsMap.put("红细胞面积占比", new IndicatorAddIn("Erythrocyte area%", String.valueOf(pituitaryD_H), "%", "0"));
		}
		//				神经部细胞核密度	5	个/平方毫米	Nucleus density of pars nervosa	5=E/A
		if(pituitaryA.compareTo(BigDecimal.ZERO) != 0 && mucosaCountE != 0){
			BigDecimal pituitaryE_A = commonJsonParser.getProportionMultiply(new BigDecimal(mucosaCountE), pituitaryA); 
			indicatorResultsMap.put("神经部细胞核密度", new IndicatorAddIn("Erythrocyte area pars nervosa", String.valueOf(pituitaryE_A), "个/平方毫米", "0"));
		}
		//		
		//				中间部细胞核密度	6	个/平方毫米	Nucleus density of pars intermedia	6=F/B
		if(mucosaCountF != 0 && pituitaryB.compareTo(BigDecimal.ZERO) != 0){
			BigDecimal pituitaryF_B = commonJsonParser.getProportionMultiply(new BigDecimal(mucosaCountF), pituitaryB); 
			indicatorResultsMap.put("中间部细胞核密度", new IndicatorAddIn("Nucleus density of pars intermedi", String.valueOf(pituitaryF_B), "个/平方毫米", "0"));
		}
		//				远侧部细胞核密度	7	个/平方毫米	Nucleus density of 7=G/C
		if(mucosaCountG != 0 && pituitaryC.compareTo(BigDecimal.ZERO) != 0){
			BigDecimal pituitaryG_C = commonJsonParser.getProportionMultiply(new BigDecimal(mucosaCountG), pituitaryC); 
			indicatorResultsMap.put("远侧部细胞核密度", new IndicatorAddIn("Nucleus density of pars distalis", String.valueOf(pituitaryG_C), "个/平方毫米", "0"));
		}



		aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
	}

	@Override
	public String getAlgorithmCode() {
		return "Pituitary";
	}
}
