package cn.staitech.fr.service.strategy.json.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.domain.AiForecast;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wanglibei
 * @version V1.0
 * @ClassName: SciaticNerveParserStrategyImpl
 * @Description:大鼠坐骨神经
 * @date 2024年5月13日
 */
@Slf4j
@Component("Sciatic_nerve")
public class SciaticNerveParserStrategyImpl extends AbstractCustomParserStrategy {
	@Resource
	private AiForecastService aiForecastService;
	@Resource
	private CommonJsonParser commonJsonParser;

	@PostConstruct
	public void init() {
		setCommonJsonParser(commonJsonParser);
		log.info("SciaticNerveParserStrategyImpl init");
	}


	@Override
	public void alculationIndicators(JsonTask jsonTask) {

		log.info("大鼠坐骨神经构指标计算开始");
		// 查询所有未被删除且登录机构相同的数据
		Map<String, Long> pathologicalMap = commonJsonParser.getPathologicalMap(jsonTask.getOrganizationId());
		//定位表

		//		结构	编码
		//		神经纤维束	1400BB
		//		神经外膜结缔组织	1400BA
		//		算法输出指标	指标代码（仅限本文档）	单位（保留小数点后3位）	备注
		//		神经纤维束面积	A	103平方微米	若多个数据则相加输出
		//		神经外膜结缔组织面积	B	平方毫米	
		//
		//		产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后3位）	English	计算方式	备注
		//		神经纤维束面积	1	103平方微米	Nerve fiber bundles area	1=A	
		//		结缔组织面积	2	平方毫米	Connective tissue area	2=B-A	运算前注意统一单位
		//		即神经外膜结缔组织面积

		List<AiForecast> insertEntity = new ArrayList<>();

		BigDecimal bigDecimalA = new BigDecimal(0);
		BigDecimal bigDecimalAmm = new BigDecimal(0);
		//神经纤维束面积	1	10³平方微米	Nerve fiber bundles area	1=A
		if (ObjectUtil.isNotEmpty(pathologicalMap.get("1400BB"))) {
			bigDecimalA = commonJsonParser.getOrganArea(jsonTask, "1400BB").getStructureAreaNum();
			bigDecimalAmm = commonJsonParser.getOrganAreaMicron(jsonTask, "1400BB");

			AiForecast aiForecast1 = new AiForecast();
			aiForecast1.setQuantitativeIndicators("神经纤维束面积");
			aiForecast1.setQuantitativeIndicatorsEn("Nerve fiber bundles area");
			aiForecast1.setUnit("10³平方微米");
			aiForecast1.setSingleSlideId(jsonTask.getSingleId());
			aiForecast1.setResults(bigDecimalAmm.toString());
			//结构指标类别0：产品呈现指标 1：算法输出指标
			aiForecast1.setStructType("0");
			insertEntity.add(aiForecast1);

			//神经外膜结缔组织面积 B 平方毫米
			BigDecimal bigDecimalB = commonJsonParser.getOrganArea(jsonTask, "1400BA").getStructureAreaNum();
			AiForecast aiForecast2 = new AiForecast();
			aiForecast2.setQuantitativeIndicators("神经外膜结缔组织面积");
			aiForecast2.setUnit("平方毫米");
			aiForecast2.setSingleSlideId(jsonTask.getSingleId());
			aiForecast2.setResults(bigDecimalB.toString());
			//结构指标类别0：产品呈现指标 1：算法输出指标
			aiForecast2.setStructType("1");
			insertEntity.add(aiForecast2);

			//结缔组织面积	2	平方毫米	Connective tissue area	2=B-A
			AiForecast aiForecast3 = new AiForecast();
			aiForecast3.setQuantitativeIndicators("Connective tissue area");
			aiForecast3.setUnit("平方毫米");
			aiForecast3.setSingleSlideId(jsonTask.getSingleId());
			aiForecast3.setResults(bigDecimalB.subtract(bigDecimalA).setScale(3, RoundingMode.HALF_UP).toString());
			//结构指标类别0：产品呈现指标 1：算法输出指标
			aiForecast3.setStructType("0");
			insertEntity.add(aiForecast3);
		}

		aiForecastService.saveBatch(insertEntity);
	}

	@Override
	public String getAlgorithmCode() {
		return "Sciatic_nerve";
	}
}
