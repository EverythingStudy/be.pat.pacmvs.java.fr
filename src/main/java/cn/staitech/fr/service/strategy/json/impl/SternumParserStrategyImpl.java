package cn.staitech.fr.service.strategy.json.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.domain.AiForecast;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author wanglibei
 * @version V1.0
 * @ClassName: SternumParserStrategyImpl
 * @Description:大鼠胸骨
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

	@PostConstruct
	public void init() {
		setCommonJsonParser(commonJsonParser);
		log.info("SternumParserStrategyImpl init");
	}


	@Override
	public void alculationIndicators(JsonTask jsonTask) {

		log.info("大鼠胸骨构指标计算开始");
		// 查询所有未被删除且登录机构相同的数据
		Map<String, Long> pathologicalMap = commonJsonParser.getPathologicalMap(jsonTask.getOrganizationId());
		//定位表
		Long sequenceNumber = commonJsonParser.getSequenceNumber(jsonTask.getSpecialId());

		//骨髓腔:14E00E
		//红系细胞核:14E011
		//粒系细胞:14E01A
		//巨核系细胞:14E022
		//红细胞:14E004
		//脂肪细胞:	14E012
		//骨质:	14E00F
		//组织轮廓:	14E111

		List<AiForecast> insertEntity = new ArrayList<>();

		//        骨髓腔面积	A	平方毫米	若输出结果为多个则相加
		//        红系细胞核数量	B	个	无
		//        粒系细胞数量	C	个	无
		//        巨核系细胞数量	D	个	无
		//        红细胞面积	E	103平方微米	若输出结果为多个则相加
		//        脂肪细胞面积	F	103平方微米	若输出结果为多个则相加
		//        骨质面积	G	103平方微米	 负样本，辅助得到骨髓腔，若输出结果为多个则相加
		//        组织轮廓面积	H	平方毫米	无

		//		骨髓腔面积	A
		if (ObjectUtil.isNotEmpty(pathologicalMap.get("14E00E"))) {
			Annotation annotation2 = new Annotation();
			annotation2.setSingleSlideId(jsonTask.getSingleId());
			annotation2.setCategoryId(pathologicalMap.get("14E00E"));
			annotation2.setSequenceNumber(sequenceNumber);
			Integer result2 = annotationMapper.countDucts(annotation2);
		}


		Integer resultC = 0;
		//粒系细胞数量C 个
		if (ObjectUtil.isNotEmpty(pathologicalMap.get("14E01A"))) {
			Annotation annotation1 = new Annotation();
			annotation1.setSingleSlideId(jsonTask.getSingleId());
			annotation1.setCategoryId(pathologicalMap.get("14E01A"));
			annotation1.setSequenceNumber(sequenceNumber);
			resultC = annotationMapper.countDucts(annotation1);
		}
		
		Integer resultB = 0;
		//红系细胞核数量B 个
		if (ObjectUtil.isNotEmpty(pathologicalMap.get("14E011"))) {
			Annotation annotation2 = new Annotation();
			annotation2.setSingleSlideId(jsonTask.getSingleId());
			annotation2.setCategoryId(pathologicalMap.get("14E011"));
			annotation2.setSequenceNumber(sequenceNumber);
			resultB = annotationMapper.countDucts(annotation2);
		}





		//粒红比  2=C/B

		AiForecast aiForecast1 = new AiForecast();
		aiForecast1.setQuantitativeIndicators("粒红比");
		aiForecast1.setQuantitativeIndicatorsEn("Myelocyte:erythropoiesis ratio");
		aiForecast1.setUnit("无");
		aiForecast1.setSingleSlideId(jsonTask.getSingleId());
		//保留小数点后3位
		BigDecimal bigDecimalC = new BigDecimal(resultC);
		BigDecimal bigDecimalB = new BigDecimal(resultB);
		BigDecimal bigDecimal3 = bigDecimalC.divide(bigDecimalB, 3, RoundingMode.HALF_UP);
		aiForecast1.setResults(bigDecimal3.toString());
		insertEntity.add(aiForecast1);

		//胸骨面积 ==>组织轮廓面积
		AiForecast aiForecast2 = new AiForecast();
		aiForecast2.setQuantitativeIndicators("胸骨面积");
		aiForecast2.setQuantitativeIndicatorsEn("Sternum area");
		aiForecast2.setUnit("平方毫米");
		SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
		if (StringUtils.isNotEmpty(singleSlide.getArea())) {
			aiForecast2.setResults(singleSlide.getArea());
		}
		aiForecast2.setSingleSlideId(jsonTask.getSingleId());
		insertEntity.add(aiForecast2);


		aiForecastService.saveBatch(insertEntity);
	}

	@Override
	public String getAlgorithmCode() {
		return "Sternum";
	}
}
