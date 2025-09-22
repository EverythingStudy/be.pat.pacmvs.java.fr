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
* @ClassName: SpinalCordParserStrategyImpl
* @Description-d:脊髓
* @author wanglibei
* @date 2025年7月22日
* @version V1.0
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

		//AI指标保存
				/**
			    A	灰质面积（单个）	1390B3
				B	白质面积（单个）	1390B2
				C	中央管面积（单个）	1390B4
				D	室管膜细胞核数量（单个）	1390B4、1390B5
				E	红细胞面积（单个）	139004
				F	组织轮廓面积（单个）	139111
				G	灰质面积（全片）	1390B3   ---
				H	白质面积（全片）	1390B2   ---
				I	中央管面积（全片）	1390B4   ---
				J	室管膜细胞核数量（全片）	1390B4、1390B5  ---
				K	红细胞面积（全片）	139004  ---
				L	组织轮廓面积（全片）	139111  ---
				
				灰质面积占比（单个）	1=A/(A+B)
				白质面积占比（单个）	2=B/(A+B)
				中央管面积占比（单个）	3=C/(A+B)
				室管膜细胞核密度（单个）	4=D/C
				红细胞面积占比（单个）	5=E/(A+B)
				脊髓面积（单个）	6=A+B
				灰质面积占比（全片）	7=G/(G+H)  ---
				白质面积占比（全片）	8=H/(G+H)  ---
				中央管面积占比（全片）	9=I/(G+H)  ---
				室管膜细胞核密度（全片）	10=J/I  ---
				红细胞面积占比（全片）	11=K/(G+H)  ---
				脊髓面积	12=G+H  ---
			 */


		//		灰质面积（全片）	G	平方毫米	
		Annotation annotationG  = commonJsonParser.getOrganArea(jsonTask, "1390B3");
		BigDecimal bigDecimalG = BigDecimal.ZERO;
		if(null != annotationG){
			bigDecimalG = annotationG.getStructureAreaNum();
			bigDecimalG = bigDecimalG.setScale(3, BigDecimal.ROUND_HALF_UP);
		}
		//		白质面积（全片）	H	平方毫米	已扣除灰质
		Annotation annotationH  = commonJsonParser.getOrganArea(jsonTask, "1390B2");
		BigDecimal bigDecimalH = BigDecimal.ZERO;
		if(null != annotationH){
			bigDecimalH = annotationH.getStructureAreaNum();
			bigDecimalH = bigDecimalH.setScale(3, BigDecimal.ROUND_HALF_UP);
		}
		//		中央管面积（全片）	I	10³平方微米	
		BigDecimal bigDecimalI  = commonJsonParser.getOrganAreaMicron(jsonTask, "1390B4");
		
		
		//		中央管面积（全片）	I	平方毫米
//		BigDecimal bigDecimalI = BigDecimal.ZERO;
//		Annotation annotationI  = commonJsonParser.getOrganArea(jsonTask, "1390B4");
//		if(null != annotationI){
//			bigDecimalI = annotationI.getStructureAreaNum();
//			bigDecimalI = bigDecimalI.setScale(3, BigDecimal.ROUND_HALF_UP);
//		}
		
		//		室管膜细胞核数量（全片）	D	个	单个脊髓内数据相加输出
		Integer mucosaCountD = commonJsonParser.getOrganAreaCount(jsonTask, "1390B5");
		//		红细胞面积（全片）	E	平方毫米	单个脊髓内数据相加输出
		Annotation annotationE  = commonJsonParser.getOrganArea(jsonTask, "139004");
		BigDecimal bigDecimalE = BigDecimal.ZERO;
		if(null != annotationE){
			bigDecimalE = annotationE.getStructureAreaNum();
			bigDecimalE = bigDecimalE.setScale(3, BigDecimal.ROUND_HALF_UP);
		}
		
		Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
		
//		indicatorResultsMap.put("灰质面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1,"1390B3"));
//		indicatorResultsMap.put("白质面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1,"1390B2"));
//		indicatorResultsMap.put("中央管面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1,"1390B4"));

		indicatorResultsMap.put("灰质面积（全片 ）", new IndicatorAddIn("", bigDecimalG.toString(), SQ_MM, "1","1390B3"));
		indicatorResultsMap.put("白质面积（全片 ）", new IndicatorAddIn("", bigDecimalH.toString(), SQ_MM, "1","1390B2"));
		indicatorResultsMap.put("中央管面积（全片 ）", new IndicatorAddIn("", bigDecimalI.toString(), SQ_UM_THOUSAND, "1","1390B4"));
		
		
//		indicatorResultsMap.put("室管膜细胞核数量（全片 ）", new IndicatorAddIn("", mucosaCountD.toString(), PIECE, "1",areaUtils.getStructureIds("1390B4","1390B5")));
//		indicatorResultsMap.put("红细胞面积（全片 ）", new IndicatorAddIn("", bigDecimalE.toString(), SQ_MM, "1","139004"));
//		indicatorResultsMap.put("组织轮廓面积", new IndicatorAddIn("",   String.valueOf(bigDSlideArea.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "1","139111"));

		aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
		
		indicatorResultsMap = new HashMap<>();
		
		BigDecimal bigDecimalG_H = BigDecimal.ZERO;
		bigDecimalG_H = bigDecimalG.add(bigDecimalH);
		
//		BigDecimal BigDecimalA_add_B = BigDecimal.ZERO;
//		if(bigDecimalA.compareTo(BigDecimal.ZERO) != 0 && bigDecimalB.compareTo(BigDecimal.ZERO) != 0) {
//			BigDecimalA_add_B = bigDecimalA.add(bigDecimalB);
//		}
		
		//灰质面积占比（全片）7=G/(G+H)   Gray matter area（all）
		if(bigDecimalG.compareTo(BigDecimal.ZERO) != 0 && bigDecimalG_H.compareTo(BigDecimal.ZERO) != 0) {
          BigDecimal mesenchymeAreaRate = bigDecimalG.divide(bigDecimalG_H, 7, BigDecimal.ROUND_HALF_UP);
			indicatorResultsMap.put("灰质面积占比（全片）", new IndicatorAddIn("Gray matter area（all）", String.valueOf(mesenchymeAreaRate), "%", "0",areaUtils.getStructureIds("1390B3","1390B2")));
		}
		//白质面积占比（全片）White matter area（all）  8=H/(G+H)
		if(bigDecimalH.compareTo(BigDecimal.ZERO) != 0 && bigDecimalG_H.compareTo(BigDecimal.ZERO) != 0) {
			BigDecimal mesenchymeAreaRate = bigDecimalH.divide(bigDecimalG_H, 7, BigDecimal.ROUND_HALF_UP);
			indicatorResultsMap.put("白质面积占比（全片）", new IndicatorAddIn("White matter area（all）", String.valueOf(mesenchymeAreaRate), "%", "0",areaUtils.getStructureIds("1390B3","1390B2")));
		}
		//中央管面积占比（全片）Central canal area（all）9=I/(G+H)
		if(bigDecimalI.compareTo(BigDecimal.ZERO) != 0 && bigDecimalG_H.compareTo(BigDecimal.ZERO) != 0) {
			BigDecimal mesenchymeAreaRate = bigDecimalI.divide(bigDecimalG_H, 7, BigDecimal.ROUND_HALF_UP);
			indicatorResultsMap.put("中央管面积占比（全片）", new IndicatorAddIn("Central canal area（all）", String.valueOf(mesenchymeAreaRate), "%", "0",areaUtils.getStructureIds("1390B3","1390B2","1390B4")));
		}
		
//		//		灰质面积占比（单个）	1	%	Gray matter area（per）	1=A/(A+B)
//		if(bigDecimalA.compareTo(BigDecimal.ZERO) != 0 && BigDecimalA_add_B.compareTo(BigDecimal.ZERO) != 0) {
//			BigDecimal mucosaCountA_B = commonJsonParser.getProportion(bigDecimalA, BigDecimalA_add_B);
//			indicatorResultsMap.put("灰质面积占比（单个）", new IndicatorAddIn("Gray matter area", String.valueOf(mucosaCountA_B), "%", "0",areaUtils.getStructureIds("1390B3","1390B2")));
//		}
//		//		白质面积占比（单个）	2	%	White matter area（per） 	2=B/(A+B)
//		if(bigDecimalB.compareTo(BigDecimal.ZERO) != 0 && BigDecimalA_add_B.compareTo(BigDecimal.ZERO) != 0) {
//			BigDecimal bigDecimalB_A_B = commonJsonParser.getProportion(bigDecimalB, BigDecimalA_add_B);
//			indicatorResultsMap.put("白质面积占比（单个）", new IndicatorAddIn("White matter area", String.valueOf(bigDecimalB_A_B), "%", "0",areaUtils.getStructureIds("1390B3","1390B2")));
//		}
//		//		中央管面积占比（单个）	3	%	Central canal area（per）	3=C/A
//		if(bigDecimalC.compareTo(BigDecimal.ZERO) != 0 && bigDecimalA.compareTo(BigDecimal.ZERO) != 0) {
//			BigDecimal bigDecimalC_A = commonJsonParser.getProportion(bigDecimalC, bigDecimalA);
//			indicatorResultsMap.put("中央管面积占比（单个）", new IndicatorAddIn("Central canal area", String.valueOf(bigDecimalC_A), "%", "0",areaUtils.getStructureIds("1390B4","1390B3")));
//		}
		
		//		灰质面积占比（单个）	1	%	Gray matter area（per）	1=A/(A+B)
//		if(bigDecimalA.compareTo(BigDecimal.ZERO) != 0 && BigDecimalA_add_B.compareTo(BigDecimal.ZERO) != 0) {
//			BigDecimal mucosaCountA_B = commonJsonParser.getProportion(bigDecimalA, BigDecimalA_add_B);
//			indicatorResultsMap.put("灰质面积占比（单个）", new IndicatorAddIn("Gray matter area", String.valueOf(mucosaCountA_B), "%", "0",areaUtils.getStructureIds("1390B3","1390B2")));
//		}
		//		白质面积占比（单个）	2	%	White matter area（per） 	2=B/(A+B)	
//		if(bigDecimalB.compareTo(BigDecimal.ZERO) != 0 && BigDecimalA_add_B.compareTo(BigDecimal.ZERO) != 0) {
//			BigDecimal bigDecimalB_A_B = commonJsonParser.getProportion(bigDecimalB, BigDecimalA_add_B);
//			indicatorResultsMap.put("白质面积占比（单个）", new IndicatorAddIn("White matter area", String.valueOf(bigDecimalB_A_B), "%", "0",areaUtils.getStructureIds("1390B3","1390B2")));
//		}
		//		中央管面积占比（单个）	3	%	Central canal area（per）	3=C/A
//		if(bigDecimalC.compareTo(BigDecimal.ZERO) != 0 && bigDecimalA.compareTo(BigDecimal.ZERO) != 0) {
//			BigDecimal bigDecimalC_A = commonJsonParser.getProportion(bigDecimalC, bigDecimalA);
//			indicatorResultsMap.put("中央管面积占比（单个）", new IndicatorAddIn("Central canal area", String.valueOf(bigDecimalC_A), "%", "0",areaUtils.getStructureIds("1390B4","1390B3")));
//		}
		
		//		室管膜细胞核密度（单个）	4	个/10³平方微米	Ependyma nucleus%(per)	4=D/C	
//		if(mucosaCountD != 0 && bigDecimalC.compareTo(BigDecimal.ZERO) != 0) {
//			BigDecimal bigDecimalD_C = commonJsonParser.bigDecimalDivideCheck(new BigDecimal(mucosaCountD), bigDecimalC); 	
//			indicatorResultsMap.put("室管膜细胞核密度（全片）", new IndicatorAddIn("Ependyma nucleus%", String.valueOf(bigDecimalD_C), SQ_UM_PICE, "0",areaUtils.getStructureIds("1390B4","1390B5")));
//		}
		//		红细胞面积占比（单个）	5	%	Erythrocyte area%（per）	5=E/(A+B)	
//		if(bigDecimalE.compareTo(BigDecimal.ZERO) != 0 && BigDecimalA_add_B.compareTo(BigDecimal.ZERO) != 0) {
//			BigDecimal bigDecimalE_A_B = commonJsonParser.getProportion(bigDecimalE, BigDecimalA_add_B);
//			indicatorResultsMap.put("红细胞面积占比（全片）", new IndicatorAddIn("Erythrocyte area%", String.valueOf(bigDecimalE_A_B), "%", "0",areaUtils.getStructureIds("139004","1390B3","1390B2")));
//		}
		//		脊髓面积（单个）	6	平方毫米	Spinal cord area（per）	6=A+B
//		if(BigDecimalA_add_B.compareTo(BigDecimal.ZERO) != 0) {
//			indicatorResultsMap.put("脊髓面积（全片）", new IndicatorAddIn("Sternum area", String.valueOf(BigDecimalA_add_B), SQ_MM, "0",areaUtils.getStructureIds("1390B3","1390B2")));
//		}
		
		
//		脊髓面积	6	平方毫米	Spinal cord area（all）	12=G+H
		if(bigDecimalG_H.compareTo(BigDecimal.ZERO) != 0) {
			indicatorResultsMap.put("脊髓面积（全片）", new IndicatorAddIn("Spinal cord area（all）", String.valueOf(bigDecimalG_H), SQ_MM, "0",areaUtils.getStructureIds("1390B3","1390B2")));
		}
		

		aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);

	}

	@Override
	public String getAlgorithmCode() {
		return "Spinal_cord";
	}
}
