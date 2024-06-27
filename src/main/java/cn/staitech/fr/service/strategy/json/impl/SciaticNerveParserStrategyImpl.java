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
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wanglibei
 * @version V1.0
 * @ClassName: SciaticNerveParserStrategyImpl
 * @Description:大鼠坐骨神经-7I
 * @date 2024年5月13日
 */
@Slf4j
@Component("Sciatic_nerve")
public class SciaticNerveParserStrategyImpl extends AbstractCustomParserStrategy {
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
		log.info("SciaticNerveParserStrategyImpl init");
	}


	@Override
	public void alculationIndicators(JsonTask jsonTask) {
		log.info("大鼠坐骨神经构指标计算开始");
		//		结构	编码
		//		神经纤维束	1400BB
		//		神经外膜结缔组织	1400BA
		//		算法输出指标	指标代码（仅限本文档）	单位（保留小数点后3位）	备注
		//		神经纤维束面积	A	10³平方微米	若多个数据则相加输出
		//		神经外膜结缔组织面积	B	平方毫米	
		//
		//		产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后3位）	English	计算方式	备注
		//		神经纤维束面积	1	10³平方微米	Nerve fiber bundles area	1=A	
		//		结缔组织面积	2	平方毫米	Connective tissue area	2=B-A	运算前注意统一单位
		//		即神经外膜结缔组织面积
		
		Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();

		//神经纤维束面积	1	10³平方微米	Nerve fiber bundles area	1=A
//		BigDecimal pituitaryA = commonJsonParser.getOrganArea(jsonTask, "1400BB").getStructureAreaNum();
		 BigDecimal pituitaryA = getOrganArea(jsonTask, "1400BB").getStructureAreaNum();
		if(null != pituitaryA){
//			pituitaryA = pituitaryA.setScale(3, RoundingMode.HALF_UP);
			pituitaryA = commonJsonParser.getBigDecimalValue(pituitaryA);
			String accurateArea = areaUtils.convertToSquareMicrometer(pituitaryA.toString());
			//神经纤维束面积	1	10³平方微米	Nerve fiber bundles area	1=A
//			indicatorResultsMap.put("神经纤维束面积", new IndicatorAddIn("", accurateArea, "10³平方微米", "1"));
			indicatorResultsMap.put("神经纤维束面积", new IndicatorAddIn("Nerve fiber bundles area", accurateArea, "10³平方微米", "0"));
		}


		//神经外膜结缔组织面积 B 平方毫米
//		BigDecimal bigDecimalB = commonJsonParser.getOrganArea(jsonTask, "1400BA").getStructureAreaNum();
		BigDecimal bigDecimalB = getOrganArea(jsonTask, "1400BA").getStructureAreaNum();
		if(null != bigDecimalB){
			bigDecimalB = bigDecimalB.setScale(3, RoundingMode.HALF_UP);
			bigDecimalB = commonJsonParser.getBigDecimalValue(bigDecimalB);
			indicatorResultsMap.put("神经外膜结缔组织面积", new IndicatorAddIn("", String.valueOf(bigDecimalB), "平方毫米", "1"));
			
		}
		//结缔组织面积	2	平方毫米	Connective tissue area	2=B-A
		if(bigDecimalB.compareTo(BigDecimal.ZERO) != 0 && pituitaryA.compareTo(BigDecimal.ZERO) != 0){
			BigDecimal BigDecimalB_A = bigDecimalB.subtract(pituitaryA);
			indicatorResultsMap.put("结缔组织面积", new IndicatorAddIn("Connective tissue area", String.valueOf(BigDecimalB_A), "平方毫米", "0"));
		}

		aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);

	}

	@Override
	public String getAlgorithmCode() {
		return "Sciatic_nerve";
	}
}
