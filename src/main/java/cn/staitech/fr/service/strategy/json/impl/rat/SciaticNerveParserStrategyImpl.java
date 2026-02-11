package cn.staitech.fr.service.strategy.json.impl.rat;

import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
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
 * 
* @ClassName: SciaticNerveParserStrategyImpl
* @Description-d:坐骨神经
* @author wanglibei
* @date 2025年7月22日
* @version V1.0
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
		
		/**
		 A	神经纤维束面积	1400BB
		 B	神经外膜结缔组织面积	1400BA
		 
		 神经纤维束面积	1=A
		 神经外膜结缔组织面积	2=B-A
		 */

		Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
		//神经纤维束面积	1	10³平方微米	Nerve fiber bundles area	1=A
		 BigDecimal pituitaryA = getOrganArea(jsonTask, "1400BB").getStructureAreaNum();
		if(null != pituitaryA){
			String accurateArea = areaUtils.convertToSquareMicrometer(pituitaryA.toString());
			//神经纤维束面积	1	10³平方微米	Nerve fiber bundles area	1=A
			indicatorResultsMap.put("神经纤维束面积", new IndicatorAddIn("Nerve fiber bundles area", accurateArea, SQ_UM_THOUSAND, "0","1400BB"));
			indicatorResultsMap.put("神经纤维束面积", new IndicatorAddIn("", String.valueOf(pituitaryA.setScale(3, RoundingMode.HALF_UP)), SQ_UM_THOUSAND, "1","1400BB"));
		}


		//神经外膜结缔组织面积 B 平方毫米
		BigDecimal bigDecimalB = getOrganArea(jsonTask, "1400BA").getStructureAreaNum();
		if(null != bigDecimalB){
			bigDecimalB = bigDecimalB.setScale(3, RoundingMode.HALF_UP);
			bigDecimalB = commonJsonParser.getBigDecimalValue(bigDecimalB);
//			indicatorResultsMap.put("神经外膜结缔组织面积", new IndicatorAddIn("", String.valueOf(bigDecimalB.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "1","1400BA"));
		}
		//神经外膜结缔组织面积	2	平方毫米	Connective tissue area	2=B-A
		if(bigDecimalB.compareTo(BigDecimal.ZERO) != 0 && pituitaryA.compareTo(BigDecimal.ZERO) != 0){
			BigDecimal BigDecimalB_A = bigDecimalB.subtract(pituitaryA);
			indicatorResultsMap.put("神经外膜结缔组织面积", new IndicatorAddIn("Connective tissue area", String.valueOf(BigDecimalB_A.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "0",areaUtils.getStructureIds("1400BB","1400BA")));
		}

		aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);

	}

	@Override
	public String getAlgorithmCode() {
		return "Sciatic_nerve";
	}
}
