package cn.staitech.fr.service.strategy.json.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.domain.AiForecast;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.PathologicalIndicatorCategory;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.SpecialAnnotationRel;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.ImageMapper;
import cn.staitech.fr.mapper.PathologicalIndicatorCategoryMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @ClassName: AortaParserStrategyImpl
 * @Description:大鼠主动脉
 * @author wanglibei
 * @date 2024年5月13日
 * @version V1.0
 */
@Slf4j
@Component("Aorta")
public class AortaParserStrategyImpl  extends AbstractCustomParserStrategy {

	@Resource
	private SpecialAnnotationRelMapper specialAnnotationRelMapper;
	@Resource
	private PathologicalIndicatorCategoryMapper pathologicalIndicatorCategoryMapper;
	@Resource
	private AnnotationMapper annotationMapper;
	@Resource
	private SingleSlideMapper singleSlideMapper;
	@Resource
	private AiForecastService aiForecastService;
	@Resource
	private ImageMapper imageMapper;


	@PostConstruct
	public void init() {
		setAiForecastService(aiForecastService);
		setAnnotationMapper(annotationMapper);
		setPathologicalIndicatorCategoryMapper(pathologicalIndicatorCategoryMapper);
		setSingleSlideMapper(singleSlideMapper);
		setSpecialAnnotationRelMapper(specialAnnotationRelMapper);
		setImageMapper(imageMapper);
		log.info("AortaParserStrategyImpl init");
	}


	@Override
	public void alculationIndicators(JsonTask jsonTask) {

		log.info("大鼠主动脉构指标计算开始");
		QueryWrapper<PathologicalIndicatorCategory> qw = new QueryWrapper<>();
		// 查询所有未被删除且登录机构相同的数据
		qw.eq("del_flag", 0).eq("organization_id", jsonTask.getOrganizationId());
		List<PathologicalIndicatorCategory> list = pathologicalIndicatorCategoryMapper.selectList(qw);
		Map<String, Long> pathologicalMap = list.stream().collect(Collectors.toMap(PathologicalIndicatorCategory::getStructureId, PathologicalIndicatorCategory::getCategoryId, (entity1, entity2) -> entity1));
		//定位表
		QueryWrapper<SpecialAnnotationRel> wrapper = new QueryWrapper<>();
		wrapper.eq("special_id", jsonTask.getSpecialId());
		SpecialAnnotationRel annotationRel = specialAnnotationRelMapper.selectOne(wrapper);
		Long sequenceNumber = annotationRel.getSequenceNumber();

		//空腔	15D113  A     10³平方微米
		//组织轮廓	15D111  D   10³平方微米

		List<AiForecast> insertEntity = new ArrayList<>();

		BigDecimal bigDecimalD = new BigDecimal(0);
		//组织轮廓面积 D 10³平方微米
		if (ObjectUtil.isNotEmpty(pathologicalMap.get("15D111"))) {
			SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
			if(StringUtils.isNotEmpty(singleSlide.getArea())){
				bigDecimalD = new BigDecimal(singleSlide.getArea());
			}
		}



		BigDecimal bigDecimalA = new BigDecimal(0);
		//空腔面积 D 10³平方微米
		if (ObjectUtil.isNotEmpty(pathologicalMap.get("15D113"))) {
			Annotation annotation1 = new Annotation();
			annotation1.setSingleSlideId(jsonTask.getSingleId());
			annotation1.setCategoryId(pathologicalMap.get("15D113"));
			annotation1.setSequenceNumber(sequenceNumber);
			Annotation structureArea = annotationMapper.getStructureArea(annotation1);
			if(StringUtils.isNotEmpty(structureArea.getArea())){
				bigDecimalA = new BigDecimal(structureArea.getArea());
			}
		}

		//主动脉壁面积  1=D-A

		if (null != bigDecimalD) {

			AiForecast aiForecast1 = new AiForecast();
			aiForecast1.setQuantitativeIndicators("主动脉壁面积");
			aiForecast1.setQuantitativeIndicatorsEn("Aorta wall area");
			aiForecast1.setUnit("10³平方微米");
			aiForecast1.setSingleSlideId(jsonTask.getSingleId());
			// 执行减法操作
			BigDecimal result = bigDecimalD.subtract(bigDecimalA);
			//转平方微米
			result = result.multiply(new BigDecimal(1000000));
			//保留小数点后3位
			result = result.setScale(3, RoundingMode.HALF_UP);
			aiForecast1.setResults(result.toString());
			insertEntity.add(aiForecast1);
		}

		aiForecastService.saveBatch(insertEntity);
	}

	@Override
	public String getAlgorithmCode() {
		return "Aorta";
	}
}
