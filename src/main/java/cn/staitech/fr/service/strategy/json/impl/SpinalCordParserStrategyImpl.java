package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wanglibei
 * @version V1.0
 * @ClassName: SpinalCordParserStrategyImpl
 * @Description:大鼠脊髓-7I
 * @date 2024年5月13日
 */
@Slf4j
@Component("Spinal_cord")
public class SpinalCordParserStrategyImpl extends AbstractCustomParserStrategy {
	@Resource
	private AnnotationMapper annotationMapper;
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
		log.info("SpinalCordParserStrategyImpl init");
	}


	@Override
	public void alculationIndicators(JsonTask jsonTask) {

		log.info("大鼠脊髓构指标计算开始");

		String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());
		BigDecimal bigDSlideArea = BigDecimal.ZERO;
		if(StringUtils.isNotEmpty(slideArea)){
			bigDSlideArea = new BigDecimal(slideArea);
		}

		/*
		灰质	1390B3
		白质	1390B2
		中央管	1390B4
		室管膜细胞核	1390B5
		红细胞	139004
		 */

		//		灰质面积（单个）	A	平方毫米	
		Annotation annotationA  = commonJsonParser.getOrganArea(jsonTask, "1390B3");
		BigDecimal bigDecimalA = BigDecimal.ZERO;
		if(null != annotationA){
			bigDecimalA = annotationA.getStructureAreaNum();
			bigDecimalA = bigDecimalA.setScale(3, BigDecimal.ROUND_HALF_UP);
		}
		//		白质面积（单个）	B	平方毫米	已扣除灰质
		Annotation annotationB  = commonJsonParser.getOrganArea(jsonTask, "1390B2");
		BigDecimal bigDecimalB = BigDecimal.ZERO;
		if(null != annotationB){
			bigDecimalB = annotationB.getStructureAreaNum();
			bigDecimalB = bigDecimalB.setScale(3, BigDecimal.ROUND_HALF_UP);
		}
		//		中央管面积（单个）	C	10³平方微米	
		BigDecimal bigDecimalC  = commonJsonParser.getOrganAreaMicron(jsonTask, "1390B4");
		//		室管膜细胞核数量（单个）	D	个	单个脊髓内数据相加输出
		Integer mucosaCountD = commonJsonParser.getOrganAreaCount(jsonTask, "1390B5");
		//		红细胞面积（单个）	E	平方毫米	单个脊髓内数据相加输出
		Annotation annotationE  = commonJsonParser.getOrganArea(jsonTask, "139004");
		BigDecimal bigDecimalE = BigDecimal.ZERO;
		if(null != annotationE){
			bigDecimalE = annotationE.getStructureAreaNum();
			bigDecimalE = bigDecimalE.setScale(3, BigDecimal.ROUND_HALF_UP);
		}

		Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();

		indicatorResultsMap.put("灰质面积（全片 ）", new IndicatorAddIn("", bigDecimalA.toString(), "平方毫米", "1"));
		indicatorResultsMap.put("白质面积（全片 ）", new IndicatorAddIn("", bigDecimalB.toString(), "平方毫米", "1"));
		indicatorResultsMap.put("中央管面积（全片 ）", new IndicatorAddIn("", bigDecimalC.toString(), "10³平方微米", "1"));
		indicatorResultsMap.put("室管膜细胞核数量（全片 ）", new IndicatorAddIn("", mucosaCountD.toString(), "个", "1"));
		indicatorResultsMap.put("红细胞面积（全片 ）", new IndicatorAddIn("", bigDecimalE.toString(), "平方毫米", "1"));
		indicatorResultsMap.put("组织轮廓面积", new IndicatorAddIn("",   String.valueOf(bigDSlideArea.setScale(3, RoundingMode.HALF_UP)), "平方毫米", "1"));

		aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
		
		indicatorResultsMap = new HashMap<>();
		BigDecimal BigDecimalA_add_B = BigDecimal.ZERO;
		if(bigDecimalA.compareTo(BigDecimal.ZERO) != 0 && bigDecimalB.compareTo(BigDecimal.ZERO) != 0) {
			BigDecimalA_add_B = bigDecimalA.add(bigDecimalB);
		}

		
		//AI指标保存
		//		灰质面积占比（单个）	1	%	Gray matter area（per）	1=A/(A+B)
		if(bigDecimalA.compareTo(BigDecimal.ZERO) != 0 && BigDecimalA_add_B.compareTo(BigDecimal.ZERO) != 0) {
			BigDecimal mucosaCountA_B = commonJsonParser.getProportion(bigDecimalA, BigDecimalA_add_B);
			indicatorResultsMap.put("灰质面积占比（全片）", new IndicatorAddIn("Gray matter area", String.valueOf(mucosaCountA_B), "%", "0"));
		}
		//		白质面积占比（单个）	2	%	White matter area（per） 	2=B/(A+B)	
		if(bigDecimalB.compareTo(BigDecimal.ZERO) != 0 && BigDecimalA_add_B.compareTo(BigDecimal.ZERO) != 0) {
			BigDecimal bigDecimalB_A_B = commonJsonParser.getProportion(bigDecimalB, BigDecimalA_add_B);
			indicatorResultsMap.put("白质面积占比（全片）", new IndicatorAddIn("White matter area", String.valueOf(bigDecimalB_A_B), "%", "0"));
		}
		//		中央管面积占比（单个）	3	%	Central canal area（per）	3=C/A
		if(bigDecimalC.compareTo(BigDecimal.ZERO) != 0 && bigDecimalA.compareTo(BigDecimal.ZERO) != 0) {
			BigDecimal bigDecimalC_A = commonJsonParser.getProportion(bigDecimalC, bigDecimalA);
			indicatorResultsMap.put("中央管面积占比（全片）", new IndicatorAddIn("Central canal area", String.valueOf(bigDecimalC_A), "%", "0"));
		}
		//		室管膜细胞核密度（单个）	4	个/10³平方微米	Ependyma nucleus%(per)	4=D/C	
		if(mucosaCountD != 0 && bigDecimalC.compareTo(BigDecimal.ZERO) != 0) {
			BigDecimal bigDecimalD_C = commonJsonParser.bigDecimalDivideCheck(new BigDecimal(mucosaCountD), bigDecimalC); 	
			indicatorResultsMap.put("室管膜细胞核密度（全片）", new IndicatorAddIn("Ependyma nucleus%", String.valueOf(bigDecimalD_C), "个/10³平方微米", "0"));
		}
		//		红细胞面积占比（单个）	5	%	Erythrocyte area%（per）	5=E/(A+B)	
		if(bigDecimalE.compareTo(BigDecimal.ZERO) != 0 && BigDecimalA_add_B.compareTo(BigDecimal.ZERO) != 0) {
			BigDecimal bigDecimalE_A_B = commonJsonParser.getProportion(bigDecimalE, BigDecimalA_add_B);
			indicatorResultsMap.put("红细胞面积占比（全片）", new IndicatorAddIn("Erythrocyte area%", String.valueOf(bigDecimalE_A_B), "%", "0"));
		}
		//		脊髓面积（单个）	6	平方毫米	Spinal cord area（per）	6=A+B

		if(BigDecimalA_add_B.compareTo(BigDecimal.ZERO) != 0) {
			indicatorResultsMap.put("脊髓面积（全片）", new IndicatorAddIn("Sternum area", String.valueOf(BigDecimalA_add_B), "平方毫米", "0"));
		}

		aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);

	}

	@Override
	public String getAlgorithmCode() {
		return "Spinal_cord";
	}
}
