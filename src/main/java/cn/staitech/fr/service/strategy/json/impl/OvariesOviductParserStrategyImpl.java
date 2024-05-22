package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wanglibei
 * @version V1.0
 * @ClassName: OvariesOviductParserStrategyImpl
 * @Description:大鼠卵巢-7I
 * @date 2024年5月13日
 */
@Slf4j
@Component("OvariesOviduct")
public class OvariesOviductParserStrategyImpl extends AbstractCustomParserStrategy {
	@Resource
	private AnnotationMapper annotationMapper;
	@Resource
	private AiForecastService aiForecastService;
	@Resource
	private CommonJsonParser commonJsonParser;
	@Resource
    private AreaUtils areaUtils;

	@PostConstruct
	public void init() {
		setCommonJsonParser(commonJsonParser);
		log.info("OvariesOviductParserStrategyImpl init");
	}

	@Override
	public void alculationIndicators(JsonTask jsonTask) {
		log.info("大鼠卵巢构指标计算开始");

		// 黄体:1240CA 
		//红细胞:124004 
		//卵泡:1240CB 
		//血管:124003 
		//组织轮廓:124111

		// 黄体数量 A 个
		Integer mucosaCountA = commonJsonParser.getOrganAreaCount(jsonTask, "1240CA");
		// 黄体面积（全片） C 平方毫米
		Annotation annotationC  = commonJsonParser.getOrganArea(jsonTask, "1240CA");
		BigDecimal bigDecimalC = annotationC.getStructureAreaNum();
		// 卵泡数量 D 个
		Integer mucosaCountD = commonJsonParser.getOrganAreaCount(jsonTask, "1240CB");
		// 卵泡面积（全片） F 平方毫米
		Annotation annotationF  = commonJsonParser.getOrganArea(jsonTask, "1240CA");
		BigDecimal bigDecimalF = annotationF.getStructureAreaNum();
		// 血管面积 H 平方微米
//		Annotation annotationH  = commonJsonParser.getOrganArea(jsonTask, "124003");
//		BigDecimal bigDecimalH = annotationH.getStructureAreaNum();
//		bigDecimalH = bigDecimalH.multiply(new BigDecimal("0.001"));
		BigDecimal bigDecimalH  =  commonJsonParser.getOrganAreaMicron(jsonTask, "124003");
		//TODO 血管外红细胞面积 I 平方微米
		BigDecimal bigDecimalI = BigDecimal.ZERO;
		//TODO 血管内红细胞面积 J 平方微米
		BigDecimal bigDecimalJ = BigDecimal.ZERO;
		// 组织轮廓面积 E 平方毫米
		String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());
		
		//算法保存
		Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
		indicatorResultsMap.put("黄体数量", new IndicatorAddIn("Corpus luteum numbers", String.valueOf(mucosaCountA), "个", "0"));
		indicatorResultsMap.put("黄体面积（全片）", new IndicatorAddIn("Corpus luteum area(all)", String.valueOf(bigDecimalC), "平方毫米", "0"));
		indicatorResultsMap.put("卵泡数量", new IndicatorAddIn("Follicle numbers", String.valueOf(mucosaCountD), "个", "0"));
		indicatorResultsMap.put("卵泡面积（全片）", new IndicatorAddIn("Follicle area", String.valueOf(bigDecimalF), "平方毫米", "0"));
		//TODO
//		indicatorResultsMap.put("血管外红细胞面积", new IndicatorAddIn("Extravascular Erythrocyte area", String.valueOf(bigDecimalI), "平方微米", "0"));
//		indicatorResultsMap.put("血管内红细胞面积", new IndicatorAddIn("Intravascular Erythrocyte area", String.valueOf(bigDecimalJ), "平方微米", "0"));
		indicatorResultsMap.put("血管面积", new IndicatorAddIn("Vessel area", String.valueOf(bigDecimalH), "平方微米", "0"));
		indicatorResultsMap.put("组织轮廓面积", new IndicatorAddIn("", slideArea, "平方毫米", "1"));
		
		
		
		//AI指标保存
		
		// 黄体数量 1 个 Corpus luteum numbers 1=A
		// 黄体面积（全片） 2 平方毫米 Corpus luteum area(all) 2=C
		// 卵泡数量 3 个 Follicle numbers 3=D
		// 卵泡面积（全片） 4 平方毫米 Follicle area 4=F
		// 血管面积 3 平方微米 Vessel area 3=H
		// 血管外红细胞面积 4 平方微米 Extravascular Erythrocyte area 4=I
		// 血管内红细胞面积 5 平方微米 Intravascular Erythrocyte area 5=J
		
		//TODO 输卵管算法暂时不支持
		 aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);

	}

	@Override
	public String getAlgorithmCode() {
		return "OvariesOviduct";
	}
}
