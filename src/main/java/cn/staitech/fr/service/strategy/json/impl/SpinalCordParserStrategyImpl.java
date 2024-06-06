package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
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

	@PostConstruct
	public void init() {
		setCommonJsonParser(commonJsonParser);
		log.info("SpinalCordParserStrategyImpl init");
	}


	@Override
	public void alculationIndicators(JsonTask jsonTask) {

		log.info("大鼠脊髓构指标计算开始");

		/*
		灰质	1390B3
		白质	1390B2
		中央管	1390B4
		室管膜细胞核	1390B5
		红细胞	139004
		 */

		//		灰质面积（单个）	A	平方毫米	
		/*Annotation annotationA  = commonJsonParser.getOrganArea(jsonTask, "1390B3");
		BigDecimal bigDecimalA = annotationA.getStructureAreaNum();
		//		白质面积（单个）	B	平方毫米	已扣除灰质
		Annotation annotationB  = commonJsonParser.getOrganArea(jsonTask, "1390B2");
		BigDecimal bigDecimalB = annotationB.getStructureAreaNum();
		//		中央管面积（单个）	C	10³平方微米	
		BigDecimal bigDecimalC  = commonJsonParser.getOrganAreaMicron(jsonTask, "1390B4");
		//		室管膜细胞核数量（单个）	D	个	单个脊髓内数据相加输出
		Integer mucosaCountD = commonJsonParser.getOrganAreaCount(jsonTask, "1390B5");
		//		红细胞面积（单个）	E	平方毫米	单个脊髓内数据相加输出
		Annotation annotationE  = commonJsonParser.getOrganArea(jsonTask, "139004");
		BigDecimal bigDecimalE = annotationE.getStructureAreaNum();
		//		TODO 组织轮廓面积	F	平方毫米	此轮廓包含脑膜
		//		Annotation annotationF  = commonJsonParser.getOrganArea(jsonTask, "139004");
		BigDecimal bigDecimalF = new BigDecimal(0);
		String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());
		bigDecimalF = new BigDecimal(slideArea);
		//		BigDecimal bigDecimalF = annotationE.getStructureAreaNum();



		//		灰质面积占比（单个）	1	%	Gray matter area（per）	1=A/(A+B)
		BigDecimal BigDecimalA_add_B = bigDecimalA.add(bigDecimalB);
		BigDecimal mucosaCountA_B = commonJsonParser.getProportion(bigDecimalA, BigDecimalA_add_B);
		//		白质面积占比（单个）	2	%	White matter area（per） 	2=B/(A+B)	
		BigDecimal bigDecimalB_A_B = commonJsonParser.getProportion(bigDecimalB, BigDecimalA_add_B);
		//		中央管面积占比（单个）	3	%	Central canal area（per）	3=C/A
		BigDecimal bigDecimalC_A = commonJsonParser.getProportion(bigDecimalC, bigDecimalA);

		//		室管膜细胞核密度（单个）	4	个/10³平方微米	Ependyma nucleus%(per)	4=D/C	
		BigDecimal bigDecimalD_C = commonJsonParser.getProportion(new BigDecimal(mucosaCountD), bigDecimalC); 	
		//		红细胞面积占比（单个）	5	%	Erythrocyte area%（per）	5=E/(A+B)	
		BigDecimal bigDecimalE_A_B = commonJsonParser.getProportion(bigDecimalE, BigDecimalA_add_B);
		//		脊髓面积（单个）	6	平方毫米	Spinal cord area（per）	6=A+B
		
		Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
		//算法保存
		indicatorResultsMap.put("灰质面积（单个）", new IndicatorAddIn("", String.valueOf(bigDecimalA), "平方毫米", "1"));
		indicatorResultsMap.put("白质面积（单个）", new IndicatorAddIn("", String.valueOf(bigDecimalB), "平方毫米", "1"));
		indicatorResultsMap.put("中央管面积（单个）", new IndicatorAddIn("", String.valueOf(bigDecimalC), "平方毫米", "1"));
		indicatorResultsMap.put("室管膜细胞核数量（单个）", new IndicatorAddIn("", String.valueOf(mucosaCountD), "个", "1"));
		
		indicatorResultsMap.put("红细胞面积（单个）", new IndicatorAddIn("", String.valueOf(bigDecimalE), "平方毫米", "1"));
		indicatorResultsMap.put("组织轮廓面积", new IndicatorAddIn("", String.valueOf(bigDecimalF), "平方毫米", "1"));

		
		//AI指标保存
		indicatorResultsMap.put("灰质面积占比（单个）", new IndicatorAddIn("Gray matter area（per）", String.valueOf(mucosaCountA_B), "%", "0"));
		indicatorResultsMap.put("白质面积占比（单个）", new IndicatorAddIn("White matter area（per）", String.valueOf(bigDecimalB_A_B), "%", "0"));
		indicatorResultsMap.put("中央管面积占比（单个）", new IndicatorAddIn("Central canal area（per）", String.valueOf(bigDecimalC_A), "%", "0"));
		indicatorResultsMap.put("室管膜细胞核密度（单个）", new IndicatorAddIn("Ependyma nucleus%(per)", String.valueOf(bigDecimalD_C), "个/10³平方微米", "0"));
		indicatorResultsMap.put("红细胞面积占比（单个）", new IndicatorAddIn("Erythrocyte area%（per）", String.valueOf(bigDecimalE_A_B), "%", "0"));
		indicatorResultsMap.put("脊髓面积（单个）", new IndicatorAddIn("Sternum area", String.valueOf(BigDecimalA_add_B), "平方毫米", "0"));*/
		
//        indicatorResultsMap.put("乳腺腺泡/导管面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1));
		String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());
		
		Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
		indicatorResultsMap.put("灰质面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT , CommonConstant.NUMBER_1));
		indicatorResultsMap.put("白质面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT , CommonConstant.NUMBER_1));
		indicatorResultsMap.put("中央管面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT , CommonConstant.NUMBER_1));
//		indicatorResultsMap.put("室管膜细胞核数量（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT , CommonConstant.NUMBER_1));
		indicatorResultsMap.put("红细胞面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT , CommonConstant.NUMBER_1));
		indicatorResultsMap.put("组织轮廓面积", new IndicatorAddIn("", slideArea, "平方毫米", "1"));

		
		//AI指标保存
//		indicatorResultsMap.put("灰质面积占比（单个）", new IndicatorAddIn("Gray matter area（per）", String.valueOf(mucosaCountA_B), "%", "0"));
//		indicatorResultsMap.put("白质面积占比（单个）", new IndicatorAddIn("White matter area（per）", String.valueOf(bigDecimalB_A_B), "%", "0"));
//		indicatorResultsMap.put("中央管面积占比（单个）", new IndicatorAddIn("Central canal area（per）", String.valueOf(bigDecimalC_A), "%", "0"));
//		indicatorResultsMap.put("室管膜细胞核密度（单个）", new IndicatorAddIn("Ependyma nucleus%(per)", String.valueOf(bigDecimalD_C), "个/10³平方微米", "0"));
//		indicatorResultsMap.put("红细胞面积占比（单个）", new IndicatorAddIn("Erythrocyte area%（per）", String.valueOf(bigDecimalE_A_B), "%", "0"));
//		indicatorResultsMap.put("脊髓面积（单个）", new IndicatorAddIn("Sternum area", String.valueOf(BigDecimalA_add_B), "平方毫米", "0"));
		
		
		aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
	}

	@Override
	public String getAlgorithmCode() {
		return "Spinal_cord";
	}
}
