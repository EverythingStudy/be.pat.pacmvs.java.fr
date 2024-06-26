package cn.staitech.fr.service.strategy.json.impl;

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
import java.util.HashMap;
import java.util.Map;

/**
 * @author wanglibei
 * @version V1.0
 * @ClassName: SternumParserStrategyImpl
 * @Description:大鼠胸骨-7I
 * @date 2024年5月13日
 */
@Slf4j
@Component("Sternum")
public class SternumParserStrategyImpl extends AbstractCustomParserStrategy {
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
		log.info("SternumParserStrategyImpl init");
	}


	@Override
	public void alculationIndicators(JsonTask jsonTask) {

		log.info("大鼠胸骨构指标计算开始");

		//骨髓腔:14E00E
		//红系细胞核:14E011
		//粒系细胞:14E01A
		//巨核系细胞:14E022
		//红细胞:14E004
		//脂肪细胞:	14E012
		//骨质:	14E00F
		//组织轮廓:	14E111

		//        骨髓腔面积	A	平方毫米	若输出结果为多个则相加
//		Annotation annotation  = commonJsonParser.getOrganArea(jsonTask, "14E00E");
//		BigDecimal bigDecimalA = annotation.getStructureAreaNum();
//		bigDecimalA = commonJsonParser.getBigDecimalValue(bigDecimalA);
		BigDecimal bigDecimalA = new BigDecimal("0");

		//        红系细胞核数量	B	个	无
		Integer mucosaCountB = commonJsonParser.getOrganAreaCount(jsonTask, "14E011");
		mucosaCountB = commonJsonParser.getIntegerValue(mucosaCountB);
		//        粒系细胞数量	C	个	无
		Integer mucosaCountC = commonJsonParser.getOrganAreaCount(jsonTask, "14E01A");
		mucosaCountC = commonJsonParser.getIntegerValue(mucosaCountC);
		//        巨核系细胞数量	D	个	无
		Integer mucosaCountD = commonJsonParser.getOrganAreaCount(jsonTask, "14E022");
		mucosaCountD = commonJsonParser.getIntegerValue(mucosaCountD);

		//        红细胞面积	E	10³平方微米	若输出结果为多个则相加
		Annotation annotationE  = commonJsonParser.getOrganArea(jsonTask, "14E004");
		BigDecimal bigDecimalE = annotationE.getStructureAreaNum();
		bigDecimalE = commonJsonParser.getBigDecimalValue(bigDecimalE);
		String bigDecimalEStr = areaUtils.convertToSquareMicrometer(bigDecimalE.toString());
		bigDecimalE = new BigDecimal(bigDecimalEStr);
		//        脂肪细胞面积	F	10³平方微米	若输出结果为多个则相加
		Annotation annotationF  = commonJsonParser.getOrganArea(jsonTask, "14E012");
		BigDecimal bigDecimalF_1 = annotationF.getStructureAreaNum();
		bigDecimalF_1 = commonJsonParser.getBigDecimalValue(bigDecimalF_1);

		BigDecimal bigDecimalF = annotationF.getStructureAreaNum();
		bigDecimalF = commonJsonParser.getBigDecimalValue(bigDecimalF);
		String bigDecimalFStr = areaUtils.convertToSquareMicrometer(annotationF.getStructureAreaNum().toString());
		//bigDecimalF = bigDecimalF.multiply(new BigDecimal("0.001"));
		bigDecimalF = new BigDecimal(bigDecimalFStr);
		//        骨质面积	G	10³平方微米	 负样本，辅助得到骨髓腔，若输出结果为多个则相加
		Annotation annotationG  = commonJsonParser.getOrganArea(jsonTask, "14E00F");
		BigDecimal bigDecimalG = annotationG.getStructureAreaNum();
		bigDecimalG = commonJsonParser.getBigDecimalValue(bigDecimalG);
		String bigDecimalGStr = areaUtils.convertToSquareMicrometer(bigDecimalG.toString());
		BigDecimal bigDecimalGM = new BigDecimal(bigDecimalGStr);

		//        组织轮廓面积	H	平方毫米	无
		BigDecimal bigDecimalH = BigDecimal.ZERO;
		String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());
		if(StringUtils.isNotEmpty(slideArea)){
			bigDecimalH = new BigDecimal(slideArea);
		}

		//开始计算
		//		骨髓腔面积	1	平方毫米	Bone marrow area	1=A-G
		BigDecimal BigDecimalA_G = BigDecimal.ZERO;
		if(bigDecimalA.compareTo(BigDecimal.ZERO) != 0 && bigDecimalG.compareTo(BigDecimal.ZERO) != 0){
			BigDecimalA_G = bigDecimalA.subtract(bigDecimalG);
		}
		//		粒红比	2	无	Myelocyte:erythropoiesis ratio	2=C/B
		BigDecimal BigDecimalC_B = BigDecimal.ZERO;
		if(mucosaCountC != 0 && mucosaCountB != 0){
			BigDecimalC_B = commonJsonParser.getProportion(new BigDecimal(mucosaCountC), new BigDecimal(mucosaCountB));
		}
		//		红细胞面积占比	3	%	Erythrocyte area%	3=E/(A-G)
		BigDecimal BigDecimalE_A_G =   BigDecimal.ZERO;
		if(bigDecimalE.compareTo(BigDecimal.ZERO) != 0 && BigDecimalA_G.compareTo(BigDecimal.ZERO) != 0){
			BigDecimalE_A_G = commonJsonParser.getProportion(bigDecimalE, BigDecimalA_G);
		}
		//		脂肪细胞面积占比	4	%	Adipocyte area%	4=F/(A-G)
		BigDecimal bigDecimalF_A_G =  BigDecimal.ZERO;
		if(bigDecimalF_1.compareTo(BigDecimal.ZERO) != 0 && BigDecimalA_G.compareTo(BigDecimal.ZERO) != 0){
			bigDecimalF_A_G = commonJsonParser.getProportion(bigDecimalF_1, BigDecimalA_G);
		}
		//		粒系细胞密度	3	个/平方毫米	Density of myelocyte	5=C/(A-G)
		BigDecimal bigDecimalC_A_G =  BigDecimal.ZERO;
		if(mucosaCountC != 0 && BigDecimalA_G.compareTo(BigDecimal.ZERO) != 0){
			bigDecimalC_A_G = commonJsonParser.getProportion(new BigDecimal(mucosaCountC), BigDecimalA_G);
		}
		//		红系细胞核密度	4	个/平方毫米	Nucleus density of erythropoiesis	6=B/(A-G)
		BigDecimal bigDecimalB_A_G =  BigDecimal.ZERO;
		if(mucosaCountB != 0 && BigDecimalA_G.compareTo(BigDecimal.ZERO) != 0){
			bigDecimalB_A_G = commonJsonParser.getProportion(new BigDecimal(mucosaCountB), BigDecimalA_G);
		}
		//		巨核系细胞密度	5	个/平方毫米	Density of megakaryocyte	7=D/(A-G)
		BigDecimal bigDecimalD_A_G =  BigDecimal.ZERO;
		if(mucosaCountD != 0 && BigDecimalA_G.compareTo(BigDecimal.ZERO) != 0){
			bigDecimalD_A_G = commonJsonParser.getProportion(new BigDecimal(mucosaCountD), BigDecimalA_G);
		}
		//		胸骨面积	8	平方毫米	Sternum area	8=H

		//算法保存
		Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
		Map<String, IndicatorAddIn> indicatorResultsMapSecond = new HashMap<>();
		/*if(bigDecimalA.compareTo(BigDecimal.ZERO) != 0) {
			indicatorResultsMap.put("骨髓腔面积", new IndicatorAddIn("", String.valueOf(bigDecimalA), "平方毫米", "1"));
		}*/
		if(mucosaCountB != 0) {
			indicatorResultsMap.put("红系细胞核数量", new IndicatorAddIn("", String.valueOf(mucosaCountB), "个", "1"));
		}
		if(mucosaCountC != 0) {
			indicatorResultsMap.put("粒系细胞数量", new IndicatorAddIn("", String.valueOf(mucosaCountC), "个", "1"));
		}
		//		if(mucosaCountD != 0) {
		indicatorResultsMap.put("巨核系细胞数量", new IndicatorAddIn("", String.valueOf(mucosaCountD), "个", "1"));
		//		}

		//		if(bigDecimalE.compareTo(BigDecimal.ZERO) != 0) {
		indicatorResultsMap.put("红细胞面积", new IndicatorAddIn("", String.valueOf(bigDecimalE), "10³平方微米", "1"));
		//		}
		//		if(bigDecimalF.compareTo(BigDecimal.ZERO) != 0) {
		indicatorResultsMap.put("脂肪细胞面积", new IndicatorAddIn("", String.valueOf(bigDecimalF), "10³平方微米", "1"));
		//		}
//				if(bigDecimalGM.compareTo(BigDecimal.ZERO) != 0) {
				indicatorResultsMap.put("骨质面积", new IndicatorAddIn("", String.valueOf(bigDecimalGM), "10³平方微米", "1"));
		//		}

		//AI指标保存
		if(BigDecimalA_G.compareTo(BigDecimal.ZERO) != 0) {
			indicatorResultsMap.put("骨髓腔面积", new IndicatorAddIn("Bone marrow area", String.valueOf(BigDecimalA_G), "平方毫米", "0"));
		}
		if(BigDecimalC_B.compareTo(BigDecimal.ZERO) != 0) {
			indicatorResultsMap.put("粒红比", new IndicatorAddIn("Myelocyte:erythropoiesis ratio", String.valueOf(BigDecimalC_B), "无", "0"));
		}
		if(BigDecimalE_A_G.compareTo(BigDecimal.ZERO) != 0) {
			indicatorResultsMap.put("红细胞面积占比", new IndicatorAddIn("Erythrocyte area%", String.valueOf(BigDecimalE_A_G), "%", "0"));
		}
		if(bigDecimalF_A_G.compareTo(BigDecimal.ZERO) != 0) {
			indicatorResultsMap.put("脂肪细胞面积占比", new IndicatorAddIn("", String.valueOf(bigDecimalF_A_G), "%", "0"));
		}
		if(bigDecimalC_A_G.compareTo(BigDecimal.ZERO) != 0) {
			indicatorResultsMap.put("粒系细胞密度", new IndicatorAddIn("Density of myelocyte", String.valueOf(bigDecimalC_A_G), "个/平方毫米", "0"));
		}
		if(bigDecimalB_A_G.compareTo(BigDecimal.ZERO) != 0) {
			indicatorResultsMap.put("红系细胞核密度", new IndicatorAddIn("Nucleus density of erythropoiesis", String.valueOf(bigDecimalB_A_G), "个/平方毫米", "0"));
		}
		if(bigDecimalD_A_G.compareTo(BigDecimal.ZERO) != 0) {
			indicatorResultsMap.put("巨核系细胞密度", new IndicatorAddIn("Density of megakaryocyte", String.valueOf(bigDecimalD_A_G), "个/平方毫米", "0"));
		}
		if(bigDecimalH.compareTo(BigDecimal.ZERO) != 0) {
			indicatorResultsMap.put("胸骨面积", new IndicatorAddIn("Sternum area", String.valueOf(bigDecimalH), "平方毫米", "0"));
		}

		aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);

		Map<String, IndicatorAddIn> indicatorRMap = new HashMap<>();
		indicatorRMap.put("骨髓腔面积", new IndicatorAddIn("", String.valueOf(bigDecimalA), "平方毫米", "1"));
		aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorRMap);
	}

	@Override
	public String getAlgorithmCode() {
		return "Sternum";
	}
}
