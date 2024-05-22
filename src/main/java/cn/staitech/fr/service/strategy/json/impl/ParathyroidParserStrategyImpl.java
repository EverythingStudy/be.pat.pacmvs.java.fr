package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
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
 * @author wanglibei
 * @version V1.0
 * @ClassName: ParathyroidParserStrategyImpl
 * @Description:大鼠甲状旁腺-7I
 * @date 2024年5月13日
 */
@Slf4j
@Component("Parathyroid")
public class ParathyroidParserStrategyImpl extends AbstractCustomParserStrategy {
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
		log.info("ParathyroidParserStrategyImpl init");
	}


	@Override
	public void alculationIndicators(JsonTask jsonTask) {

		log.info("大鼠甲状旁腺构指标计算开始");
		//主细胞核:108091
		//组织轮廓:（默认都有）	108111

		//		算法输出指标	指标代码（仅限本文档）	单位（保留小数点后三位）	备注
		//		主细胞核数量	A	个	无
		//		组织轮廓面积	B	10³平方微米	若多个数据则相加输出
		//
		//		产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后三位）	English	计算方式	备注
		//		主细胞核密度	1	个/10³平方微米	Nucleus density of chief cell 	1=A/B	
		//		甲状旁腺面积	2	10³平方微米	Parathyroid gland area	2=B	
		String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());
		//主细胞核数量A 个
		Integer mucosaCountA = commonJsonParser.getOrganAreaCount(jsonTask, "108091");
		//组织轮廓面积==>甲状旁腺面积 B 10³平方微米
		BigDecimal areaDecimalB = BigDecimal.ZERO;
		if (StringUtils.isNotEmpty(slideArea)) {
			String area = areaUtils.convertToSquareMicrometer(slideArea);
			areaDecimalB  = new BigDecimal(area);
		}

		

		//主细胞核密度 1=A/B
		BigDecimal bigDecimaE = commonJsonParser.getProportion(new BigDecimal(mucosaCountA), areaDecimalB);
		
		Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
		indicatorResultsMap.put("主细胞核数", new IndicatorAddIn("", String.valueOf(mucosaCountA), "个", "1"));
		indicatorResultsMap.put("甲状旁腺面积", new IndicatorAddIn("Parathyroid gland area", String.valueOf(areaDecimalB), "10³平方微米", "0"));
		indicatorResultsMap.put("主细胞核密度", new IndicatorAddIn("Nucleus density of chief cell", String.valueOf(bigDecimaE), "10³平方微米", "0"));

		aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
	}

	@Override
	public String getAlgorithmCode() {
		return "Parathyroid";
	}
}
