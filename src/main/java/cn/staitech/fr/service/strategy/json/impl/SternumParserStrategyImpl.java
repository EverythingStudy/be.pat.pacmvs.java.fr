package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
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
import java.math.RoundingMode;
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
	private SingleSlideMapper singleSlideMapper;
	@Resource
	private AiForecastService aiForecastService;
	@Resource
	private CommonJsonParser commonJsonParser;
	@Resource
    private AreaUtils areaUtils;
	
	@PostConstruct
	public void init() {
		setCommonJsonParser(commonJsonParser);
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
		Annotation annotation  = commonJsonParser.getOrganArea(jsonTask, "14E00E");
		BigDecimal bigDecimalA = annotation.getStructureAreaNum();

		//        红系细胞核数量	B	个	无
		Integer mucosaCountB = commonJsonParser.getOrganAreaCount(jsonTask, "14E011");
		//        粒系细胞数量	C	个	无
		Integer mucosaCountC = commonJsonParser.getOrganAreaCount(jsonTask, "14E01A");
		//        巨核系细胞数量	D	个	无
		Integer mucosaCountD = commonJsonParser.getOrganAreaCount(jsonTask, "14E022");

		//        红细胞面积	E	103平方微米	若输出结果为多个则相加
		Annotation annotationE  = commonJsonParser.getOrganArea(jsonTask, "14E004");
		BigDecimal bigDecimalE = annotationE.getStructureAreaNum();
		bigDecimalE = bigDecimalE.multiply(new BigDecimal("0.001"));
		//        脂肪细胞面积	F	103平方微米	若输出结果为多个则相加
		Annotation annotationF  = commonJsonParser.getOrganArea(jsonTask, "14E012");
		BigDecimal bigDecimalF_1 = annotationF.getStructureAreaNum();
		BigDecimal bigDecimalF = annotationF.getStructureAreaNum();
		bigDecimalF = bigDecimalF.multiply(new BigDecimal("0.001"));
		//        骨质面积	G	103平方微米	 负样本，辅助得到骨髓腔，若输出结果为多个则相加
		Annotation annotationG  = commonJsonParser.getOrganArea(jsonTask, "14E00F");
		BigDecimal bigDecimalG = annotationG.getStructureAreaNum();
		BigDecimal bigDecimalGM = bigDecimalG.multiply(new BigDecimal("0.001"));

		//        组织轮廓面积	H	平方毫米	无
//		Annotation annotationH  = commonJsonParser.getOrganArea(jsonTask, "14E111");
//		BigDecimal bigDecimalH = annotationH.getStructureAreaNum();
		BigDecimal bigDecimalH = new BigDecimal(0);
		String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());
		bigDecimalH = new BigDecimal(slideArea);

		//开始计算
		//		骨髓腔面积	1	平方毫米	Bone marrow area	1=A-G
		BigDecimal BigDecimalA_G = bigDecimalA.subtract(bigDecimalG);
		//		粒红比	2	无	Myelocyte:erythropoiesis ratio	2=C/B
		BigDecimal BigDecimalC_B = new BigDecimal(mucosaCountC).divide(new BigDecimal(mucosaCountB)).setScale(3,RoundingMode.HALF_UP);
		//		红细胞面积占比	3	%	Erythrocyte area%	3=E/(A-G)
		BigDecimal bigDecimalA_G = bigDecimalA.subtract(bigDecimalG);
		BigDecimal BigDecimalE_A_G = bigDecimalE.divide(bigDecimalA_G).setScale(3,RoundingMode.HALF_UP);
		//		脂肪细胞面积占比	4	%	Adipocyte area%	4=F/(A-G)
		BigDecimal bigDecimalF_A_G = bigDecimalF_1.divide(bigDecimalA_G).setScale(3,RoundingMode.HALF_UP);
		//		粒系细胞密度	3	个/平方毫米	Density of myelocyte	5=C/(A-G)
		BigDecimal bigDecimalC_A_G = new BigDecimal(mucosaCountC).divide(bigDecimalA_G).setScale(3,RoundingMode.HALF_UP);
		//		红系细胞核密度	4	个/平方毫米	Nucleus density of erythropoiesis	6=B/(A-G)
		BigDecimal bigDecimalB_A_G = new BigDecimal(mucosaCountB).divide(bigDecimalA_G).setScale(3,RoundingMode.HALF_UP);
		//		巨核系细胞密度	5	个/平方毫米	Density of megakaryocyte	7=D/(A-G)
		BigDecimal bigDecimalD_A_G = new BigDecimal(mucosaCountD).divide(bigDecimalA_G).setScale(3,RoundingMode.HALF_UP);
		//		胸骨面积	8	平方毫米	Sternum area	8=H

		//算法保存
		Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
		indicatorResultsMap.put("骨髓腔面积", new IndicatorAddIn("", String.valueOf(bigDecimalA), "平方毫米", "1"));
		indicatorResultsMap.put("红系细胞核数量", new IndicatorAddIn("", String.valueOf(mucosaCountB), "个", "1"));
		indicatorResultsMap.put("粒系细胞数量", new IndicatorAddIn("", String.valueOf(mucosaCountC), "个", "1"));
		indicatorResultsMap.put("巨核系细胞数量", new IndicatorAddIn("", String.valueOf(mucosaCountD), "个", "1"));

		indicatorResultsMap.put("红细胞面积", new IndicatorAddIn("", String.valueOf(bigDecimalE), "10³平方微米", "1"));
		indicatorResultsMap.put("脂肪细胞面积", new IndicatorAddIn("", String.valueOf(bigDecimalF), "10³平方微米", "1"));
		indicatorResultsMap.put("骨质面积", new IndicatorAddIn("", String.valueOf(bigDecimalGM), "10³平方微米", "1"));
		//indicatorResultsMap.put("组织轮廓面积", new IndicatorAddIn("", String.valueOf(bigDecimalH), "平方毫米", "1"));

		//AI指标保存
		indicatorResultsMap.put("骨髓腔面积", new IndicatorAddIn("Bone marrow area", String.valueOf(BigDecimalA_G), "平方毫米", "0"));
		indicatorResultsMap.put("粒红比", new IndicatorAddIn("Myelocyte:erythropoiesis ratio", String.valueOf(BigDecimalC_B), "无", "0"));
		indicatorResultsMap.put("红细胞面积占比", new IndicatorAddIn("Erythrocyte area%", String.valueOf(BigDecimalE_A_G), "%", "0"));
		indicatorResultsMap.put("脂肪细胞面积占比", new IndicatorAddIn("", String.valueOf(bigDecimalF_A_G), "%", "0"));

		indicatorResultsMap.put("粒系细胞密度", new IndicatorAddIn("Density of myelocyte", String.valueOf(bigDecimalC_A_G), "个/平方毫米", "0"));
		indicatorResultsMap.put("红系细胞核密度", new IndicatorAddIn("Nucleus density of erythropoiesis", String.valueOf(bigDecimalB_A_G), "个/平方毫米", "0"));
		indicatorResultsMap.put("巨核系细胞密度", new IndicatorAddIn("Density of megakaryocyte", String.valueOf(bigDecimalD_A_G), "个/平方毫米", "0"));
		indicatorResultsMap.put("胸骨面积", new IndicatorAddIn("Sternum area", String.valueOf(bigDecimalH), "平方毫米", "0"));
		
		aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
	}

	@Override
	public String getAlgorithmCode() {
		return "Sternum";
	}
}
