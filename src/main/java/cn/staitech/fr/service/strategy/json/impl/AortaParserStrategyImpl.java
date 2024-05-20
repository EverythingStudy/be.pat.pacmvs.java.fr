package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
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
import java.util.HashMap;
import java.util.Map;

/**
 * @author wanglibei
 * @version V1.0
 * @ClassName: AortaParserStrategyImpl
 * @Description:大鼠主动脉-7I
 * @date 2024年5月13日
 */
@Slf4j
@Component("Aorta")
public class AortaParserStrategyImpl extends AbstractCustomParserStrategy {

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
		log.info("AortaParserStrategyImpl init");
	}


	@Override
	public void alculationIndicators(JsonTask jsonTask) {

		log.info("大鼠主动脉构指标计算开始");

		//空腔	15D113  A     10³平方微米
		//组织轮廓	15D111  D   10³平方微米

		
		//空腔面积 A 10³平方微米
		Annotation annotation  = commonJsonParser.getOrganArea(jsonTask, "15D113");
		BigDecimal bigDecimalA = commonJsonParser.getOrganAreaMicron(jsonTask, "15D113");
		bigDecimalA = bigDecimalA.multiply(new BigDecimal("0.001"));


		//空腔周长	B	毫米
		BigDecimal bigDecimalB =  new BigDecimal(annotation.getPerimeter());

		
		BigDecimal bigDecimalC = new BigDecimal(0);
		BigDecimal bigDecimalD = new BigDecimal(0);

		//组织轮廓面积 D 10³平方微米
		SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
		if (StringUtils.isNotEmpty(singleSlide.getArea())) {
			bigDecimalD = new BigDecimal(singleSlide.getArea());
			bigDecimalD = bigDecimalD.multiply(new BigDecimal("0.001"));
			bigDecimalC =  new BigDecimal(singleSlide.getPerimeter());
		}



		Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
		indicatorResultsMap.put("空腔面积", new IndicatorAddIn("", String.valueOf(bigDecimalA), "10³平方微米", "1"));
		indicatorResultsMap.put("空腔周长", new IndicatorAddIn("", String.valueOf(bigDecimalB), "毫米", "1"));
		indicatorResultsMap.put("组织轮廓周长", new IndicatorAddIn("", String.valueOf(bigDecimalC), "毫米", "1"));
		indicatorResultsMap.put("组织轮廓面积", new IndicatorAddIn("", String.valueOf(bigDecimalD), "10³平方微米", "1"));

		//1=D-A
		indicatorResultsMap.put("主动脉壁面积", new IndicatorAddIn("Aorta wall area", String.valueOf(bigDecimalD.subtract(bigDecimalA)), "10³平方微米", "0"));
		//2=2*（D-A）/(B+C)
		BigDecimal  bigDecimalDA = bigDecimalD.subtract(bigDecimalA);
		BigDecimal  bigDecimalBC = bigDecimalB.add(bigDecimalC);
		BigDecimal  bigDecimal2 = new BigDecimal(2);
		BigDecimal mal =  bigDecimal2.multiply(bigDecimalDA.divide(bigDecimalBC)).setScale(2, RoundingMode.HALF_UP); 
		indicatorResultsMap.put("主动脉壁平均厚度", new IndicatorAddIn("Average thickness of aorta wall", String.valueOf(mal), "平方毫米", "0"));


	}

	@Override
	public String getAlgorithmCode() {
		return "Aorta";
	}
}
