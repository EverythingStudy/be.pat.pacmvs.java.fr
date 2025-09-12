package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.service.strategy.json.OutlineCustom;
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
* @ClassName: AortaParserStrategyImpl
* @Description-d:主动脉
* @author wanglibei
* @date 2025年7月21日
* @version V1.0
 */
@Slf4j
@Component("Aorta")
public class AortaParserStrategyImpl extends AbstractCustomParserStrategy implements OutlineCustom {

	@Resource
	private SingleSlideMapper singleSlideMapper;
	@Resource
	private CommonJsonParser commonJsonParser;
	@Resource
	private AreaUtils areaUtils;
	@Resource
	private AiForecastService aiForecastService;
	@Resource
	private CommonJsonCheck commonJsonCheck;
	@PostConstruct
	public void init() {
		setCommonJsonParser(commonJsonParser);
		setCommonJsonCheck(commonJsonCheck);
		log.info("AortaParserStrategyImpl init");
	}


	@Override
	public void alculationIndicators(JsonTask jsonTask) {

		log.info("大鼠主动脉构指标计算开始");

		//空腔	15D113  A     10³平方微米
		//组织轮廓	15D111  D   10³平方微米

		//空腔面积 A 10³平方微米
		//平方毫米
		BigDecimal organAreaA = getOrganArea(jsonTask, "15D113").getStructureAreaNum();
		BigDecimal organPerimeterNumP = getOrganArea(jsonTask, "15D113").getStructurePerimeterNum();

		//平方毫米
		BigDecimal bigDecimalA = BigDecimal.ZERO;
		//平方微米
		BigDecimal bigDecimalA_2 = BigDecimal.ZERO;

		if(null !=organAreaA){
			String bigDecimalAStr = areaUtils.convertToSquareMicrometer(organAreaA.toString());
			//非10³平方微米 ，普通的平方微米
			String bigDecimalASecondStr = areaUtils.convertToMicrometer(organAreaA.toString());
			bigDecimalA =  new BigDecimal(bigDecimalAStr);
			bigDecimalA_2 =  new BigDecimal(bigDecimalASecondStr);
		}


		//空腔周长	B	毫米
		BigDecimal bigDecimalB =  BigDecimal.ZERO;
		if(null != organPerimeterNumP){
			bigDecimalB =  organPerimeterNumP.setScale(3, RoundingMode.HALF_UP);
		}

		BigDecimal bigDecimalC = BigDecimal.ZERO;
		BigDecimal bigDecimalD = BigDecimal.ZERO;
		BigDecimal bigDecimalD_2 = BigDecimal.ZERO;
		//组织轮廓面积 D 10³平方微米
		SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
		if (StringUtils.isNotEmpty(singleSlide.getArea())) {
			String area = areaUtils.convertToSquareMicrometer(singleSlide.getArea());
			//非10³平方微米 ，普通的平方微米
			String area2 = areaUtils.convertToMicrometer(singleSlide.getArea());
			bigDecimalD = new BigDecimal(area);
			bigDecimalD_2 = new BigDecimal(area2);
			bigDecimalC =  new BigDecimal(singleSlide.getPerimeter());
		}

		/**
		A	空腔面积	15D113
		B	空腔周长	15D113
		C	组织轮廓周长	15D111
		D	组织轮廓面积	15D111
	
		主动脉壁面积	1=D-A
		主动脉壁平均厚度	2=2*(D-A)/(B+C)
		 */
		Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
		indicatorResultsMap.put("空腔面积", new IndicatorAddIn("", String.valueOf(bigDecimalA.setScale(3, RoundingMode.HALF_UP)), SQ_UM_THOUSAND, "1","15D113"));

		indicatorResultsMap.put("空腔周长", new IndicatorAddIn("", String.valueOf(bigDecimalB.setScale(3, RoundingMode.HALF_UP)),MM, "1","15D113"));


		indicatorResultsMap.put("组织轮廓周长", new IndicatorAddIn("", String.valueOf(bigDecimalC.setScale(3, RoundingMode.HALF_UP)),MM, "1","15D111"));

		indicatorResultsMap.put("组织轮廓面积", new IndicatorAddIn("", String.valueOf(bigDecimalD.setScale(3, RoundingMode.HALF_UP)), SQ_UM_THOUSAND, "1","15D111"));
		//1=D-A
		if(bigDecimalD.compareTo(BigDecimal.ZERO) != 0 && bigDecimalA.compareTo(BigDecimal.ZERO) != 0){
			indicatorResultsMap.put("主动脉壁面积", new IndicatorAddIn("Aorta wall area", String.valueOf(bigDecimalD.subtract(bigDecimalA)), SQ_UM_THOUSAND, "0",areaUtils.getStructureIds("15D111","15D113")));
		}
		//2=2*（D-A）/(B+C)
		if(bigDecimalD.compareTo(BigDecimal.ZERO) != 0 && bigDecimalA.compareTo(BigDecimal.ZERO) != 0&& bigDecimalB.compareTo(BigDecimal.ZERO) != 0&& bigDecimalC.compareTo(BigDecimal.ZERO) != 0){
			BigDecimal  bigDecimalDA = bigDecimalD_2.subtract(bigDecimalA_2);
			//毫米转微米
			BigDecimal  bigDecimalBC = bigDecimalB.add(bigDecimalC);
			bigDecimalBC= bigDecimalBC.multiply(new BigDecimal("1000"));
			BigDecimal  bigDecimal2 = new BigDecimal(2);
			BigDecimal mal =  bigDecimal2.multiply(commonJsonParser.getProportionMultiply(bigDecimalDA, bigDecimalBC));
			indicatorResultsMap.put("主动脉壁平均厚度", new IndicatorAddIn("Average thickness of aorta wall", String.valueOf(mal.setScale(3, RoundingMode.HALF_UP)),UM, "0",areaUtils.getStructureIds("15D111","15D113","15D113")));
		}
//		aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
	}

	@Override
	public String getAlgorithmCode() {
		return "Aorta";
	}

	@Override
	public void getCustomOutLine(JsonTask jsonTask) {
		Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
		SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
		BigDecimal pituitaryH = new BigDecimal(singleSlide.getArea());
		indicatorResultsMap.put("组织轮廓面积", createNameIndicator("Thyroid gland area", String.valueOf(pituitaryH.setScale(3, RoundingMode.HALF_UP)), SQ_UM_THOUSAND,"15D111"));
		aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
	}
}
