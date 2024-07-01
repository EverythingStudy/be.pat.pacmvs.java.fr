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
		indicatorResultsMap.put("组织轮廓面积", new IndicatorAddIn("", slideArea, "平方毫米", "1"));

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



		//		灰质	1390B3
		//		白质	1390B2
		//		中央管	1390B4
		//		室管膜细胞核	1390B5
		//		红细胞	139004

		/*//灰质面积（单个）	A	平方毫米
		Annotation annotation1390B3 = new Annotation();
		annotation1390B3.setAreaName("灰质面积（单个）");
		annotation1390B3.setAreaUnit("平方毫米");
		commonJsonParser.putSingleAnnotationDynamicData(jsonTask,"1390B3",annotation1390B3,3);

		//        白质面积（单个）	B	平方毫米
		Annotation annotation11390B2 = new Annotation();
		annotation11390B2.setAreaName("白质面积（单个）");
		annotation11390B2.setAreaUnit("平方毫米");
		commonJsonParser.putSingleAnnotationDynamicData(jsonTask,"1390B2",annotation11390B2,3);

		//        中央管面积（单个）	C	10³平方微米
		Annotation annotation11390B4 = new Annotation();
		annotation11390B4.setAreaName("中央管面积（单个）");
		annotation11390B4.setAreaUnit("10³平方微米");
		commonJsonParser.putSingleAnnotationDynamicData(jsonTask,"1390B4",annotation11390B4,1);

		//        室管膜细胞核数量（单个）	D	个

		//        红细胞面积（单个）	E	平方毫米
		Annotation annotation1139004 = new Annotation();
		annotation1139004.setAreaName("红细胞面积（单个）");
		annotation1139004.setAreaUnit("平方毫米");
		commonJsonParser.putSingleAnnotationDynamicData(jsonTask,"139004",annotation1139004,3);


		Annotation annotationBy = new Annotation();
		annotationBy.setCountName("室管膜细胞核数量（单个）");
		annotationBy.setCountUnit("个");
		commonJsonParser.putAnnotationDynamicData(jsonTask,"1390B4","1390B5",annotationBy);*/



		//灰质	1390B3
		//白质	1390B2
		//中央管	1390B4
		//室管膜细胞核	1390B5
		//红细胞	139004

		//TODO 使用getStructureContourList查询脊髓列表，循环列表然后使用getInsideOrOutside查询脊髓内的灰质、白质总面积进行计算，然后封装成annotation对象调用putAnnotationDynamicDataBy
		/*Annotation annoQuery = new Annotation();
		annoQuery.setSingleSlideId(jsonTask.getSingleId());
		annoQuery.setContourType(3L);
		List<Annotation> structureContourList = annotationMapper.getSpinalCordAnno(annoQuery);
		if(CollectionUtils.isNotEmpty(structureContourList)){
			for(Annotation anno:structureContourList){
				//灰质 A
				BigDecimal bigDecimal1390B3 = commonJsonParser.getContourInsideOrOutside(jsonTask, anno.getContour(), "1390B3", true).getStructureAreaNum().setScale(3, BigDecimal.ROUND_HALF_UP);
				//白质B
				BigDecimal bigDecimal1390B2 = commonJsonParser.getContourInsideOrOutside(jsonTask, anno.getContour(), "1390B2", true).getStructureAreaNum().setScale(3, BigDecimal.ROUND_HALF_UP);
				//中央管	C
				BigDecimal bigDecimal1390B4 = commonJsonParser.getContourInsideOrOutside(jsonTask, anno.getContour(), "1390B4", true).getStructureAreaNum().setScale(3, BigDecimal.ROUND_HALF_UP);
				String bigDecimalAStr = areaUtils.convertToSquareMicrometer(bigDecimal1390B4.toString());
				//室管膜细胞核数量 D
				Integer int1390B5 = commonJsonParser.getContourInsideOrOutside(jsonTask, anno.getContour(), "1390B5", true).getCount();
				//红细胞	E
				BigDecimal bigDecimal139004 = commonJsonParser.getContourInsideOrOutside(jsonTask, anno.getContour(), "139004", true).getStructureAreaNum().setScale(3, BigDecimal.ROUND_HALF_UP);

				BigDecimal bigDecimalA_B = bigDecimal1390B3.add(bigDecimal1390B2).setScale(3, BigDecimal.ROUND_HALF_UP);
				//灰质   1=A/(A+B)
				Annotation annotationB3 = new Annotation();
				annotationB3.setAnnotationId(anno.getAnnotationId());
				annotationB3.setAreaName("灰质面积占比（单个）");
				annotationB3.setAreaUnit("平方毫米");
				annotationB3.setAreaValue(bigDecimal1390B3.toString());
				commonJsonParser.putAnnotationDynamicDataBy(jsonTask, annotationB3);
				BigDecimal bigDecimalA = new BigDecimal("0.000");
				if(bigDecimal1390B3.compareTo(BigDecimal.ZERO) != 0 && bigDecimalA_B.compareTo(BigDecimal.ZERO) != 0) {
					bigDecimalA = bigDecimal1390B3.divide(bigDecimalA_B, 3, RoundingMode.HALF_UP).setScale(3, BigDecimal.ROUND_HALF_UP);
				}
				//白质   2=B/(A+B)
				Annotation annotationB2 = new Annotation();
				annotationB2.setAnnotationId(anno.getAnnotationId());
				annotationB2.setAreaName("白质面积占比（单个）");
				annotationB2.setAreaUnit("平方毫米");
				annotationB2.setAreaValue(bigDecimal1390B2.toString());
				commonJsonParser.putAnnotationDynamicDataBy(jsonTask, annotationB2);
				BigDecimal bigDecimalB = new BigDecimal("0.000");
				if(bigDecimal1390B2.compareTo(BigDecimal.ZERO) != 0 && bigDecimalA_B.compareTo(BigDecimal.ZERO) != 0) {
					bigDecimalB = bigDecimal1390B2.divide(bigDecimalA_B, 3, RoundingMode.HALF_UP).setScale(3, BigDecimal.ROUND_HALF_UP);
				}
				//中央管	3=C/A
				Annotation annotationB4 = new Annotation();
				annotationB4.setAnnotationId(anno.getAnnotationId());
				annotationB4.setAreaName("中央管面积占比（单个）");
				annotationB4.setAreaUnit("10³平方微米");
				annotationB4.setAreaValue(bigDecimalAStr.toString());
				commonJsonParser.putAnnotationDynamicDataBy(jsonTask, annotationB4);
				BigDecimal bigDecimalC = new BigDecimal("0.000");
				if(bigDecimal1390B4.compareTo(BigDecimal.ZERO) != 0 && bigDecimal1390B3.compareTo(BigDecimal.ZERO) != 0) {
					bigDecimalC = bigDecimal1390B4.divide(bigDecimal1390B3, 3, RoundingMode.HALF_UP).setScale(3, BigDecimal.ROUND_HALF_UP);
				}


				//室管膜细胞核数量  4=D/C
				Annotation annotationB5 = new Annotation();
				annotationB5.setAnnotationId(anno.getAnnotationId());
				annotationB5.setAreaName("室管膜细胞核密度（单个）");
				annotationB5.setAreaUnit("个");
				annotationB5.setAreaValue(int1390B5 == null ?"":int1390B5.toString());
				commonJsonParser.putAnnotationDynamicDataBy(jsonTask, annotationB5);
				BigDecimal bigDecimalD = new BigDecimal("0.000");
				if(null != int1390B5 && int1390B5 >0 &&  StringUtils.isNotEmpty(bigDecimalAStr)) {
					bigDecimalD = new BigDecimal(int1390B5).divide(new BigDecimal(bigDecimalAStr), 3, RoundingMode.HALF_UP).setScale(3, BigDecimal.ROUND_HALF_UP);
				}

				//红细胞	  5=E/(A+B)
				Annotation annotation004 = new Annotation();
				annotation004.setAnnotationId(anno.getAnnotationId());
				annotation004.setAreaName("红细胞面积占比（单个）");
				annotation004.setAreaUnit("平方毫米");
				annotation004.setAreaValue(bigDecimal139004.toString());
				commonJsonParser.putAnnotationDynamicDataBy(jsonTask, annotation004);
				BigDecimal bigDecimalE = new BigDecimal("0.000");
				if(bigDecimal139004.compareTo(BigDecimal.ZERO) != 0 && bigDecimalA_B.compareTo(BigDecimal.ZERO) != 0) {
					bigDecimalE = bigDecimal139004.divide(bigDecimalA_B, 3, RoundingMode.HALF_UP).setScale(3, BigDecimal.ROUND_HALF_UP);
				}


				//脊髓面积（单个） 6=A+B

				Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();

				//TODO AI指标保存
				if(bigDecimalA.compareTo(BigDecimal.ZERO) != 0) {
					indicatorResultsMap.put("灰质面积占比（单个）", new IndicatorAddIn("Gray matter area（per）", String.valueOf(bigDecimalA), "%", "0"));
				}
				if(bigDecimalB.compareTo(BigDecimal.ZERO) != 0) {
					indicatorResultsMap.put("白质面积占比（单个）", new IndicatorAddIn("White matter area（per）", String.valueOf(bigDecimalB), "%", "0"));
				}
				if(bigDecimalC.compareTo(BigDecimal.ZERO) != 0) {
					indicatorResultsMap.put("中央管面积占比（单个）", new IndicatorAddIn("Central canal area（per）", String.valueOf(bigDecimalC), "%", "0"));
				}
				if(bigDecimalD.compareTo(BigDecimal.ZERO) != 0) {
					indicatorResultsMap.put("室管膜细胞核密度（单个）", new IndicatorAddIn("Ependyma nucleus%(per)", String.valueOf(bigDecimalD), "个/10³平方微米", "0"));
				}
				if(bigDecimalE.compareTo(BigDecimal.ZERO) != 0) {
					indicatorResultsMap.put("红细胞面积占比（单个）", new IndicatorAddIn("Erythrocyte area%（per）", String.valueOf(bigDecimalE), "%", "0"));
				}
				if(bigDecimalA_B.compareTo(BigDecimal.ZERO) != 0) {
					indicatorResultsMap.put("脊髓面积（单个）", new IndicatorAddIn("Sternum area", String.valueOf(bigDecimalA_B), "平方毫米", "0"));
				}
				aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);

			}
		}*/





	}

	@Override
	public String getAlgorithmCode() {
		return "Spinal_cord";
	}
}
