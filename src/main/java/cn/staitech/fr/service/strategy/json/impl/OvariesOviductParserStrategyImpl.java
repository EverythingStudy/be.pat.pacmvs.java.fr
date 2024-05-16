package cn.staitech.fr.service.strategy.json.impl;

import java.math.BigDecimal;
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
 * @ClassName: OvariesOviductParserStrategyImpl
 * @Description:大鼠卵巢
 * @author wanglibei
 * @date 2024年5月13日
 * @version V1.0
 */
@Slf4j
@Component("Ovaries")
public  class OvariesOviductParserStrategyImpl extends AbstractCustomParserStrategy {

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
        log.info("OvariesOviductParserStrategyImpl init");
    }
    
	@Override
	public void alculationIndicators(JsonTask jsonTask) {
		log.info("大鼠卵巢构指标计算开始");
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

		//黄体:1240CA 红细胞:124004 卵泡:1240CB 血管:124003 组织轮廓:124111

		List<AiForecast> insertEntity = new ArrayList<>();
		//黄体数量 1=A
		if (ObjectUtil.isNotEmpty(pathologicalMap.get("1240CA"))) {
			Annotation annotation1 = new Annotation();
			annotation1.setSingleSlideId(jsonTask.getSingleId());
			annotation1.setCategoryId(pathologicalMap.get("1240CA"));
			annotation1.setSequenceNumber(sequenceNumber);
			Integer result = annotationMapper.countDucts(annotation1);
			AiForecast aiForecast = new AiForecast();
			aiForecast.setQuantitativeIndicators("黄体数量");
			aiForecast.setQuantitativeIndicatorsEn("Corpus luteum numbers");
			aiForecast.setUnit("个");
			aiForecast.setResults(result.toString());
			aiForecast.setSingleSlideId(jsonTask.getSingleId());
			insertEntity.add(aiForecast);

			//黄体面积（全片） 2=C
			Annotation structureArea = annotationMapper.getStructureArea(annotation1);
			if (StringUtils.isNotEmpty(structureArea.getArea())) {
				BigDecimal bigDecimal1 = new BigDecimal(structureArea.getArea());
				AiForecast aiForecast1 = new AiForecast();
				aiForecast1.setQuantitativeIndicators("黄体面积（全片）");
				aiForecast1.setQuantitativeIndicatorsEn("Corpus luteum area(all)");
				aiForecast1.setUnit("平方毫米");
				aiForecast1.setSingleSlideId(jsonTask.getSingleId());
				aiForecast1.setResults(bigDecimal1.toString());
				insertEntity.add(aiForecast1);
			}
		}

		//卵泡数量 3=D
		if (ObjectUtil.isNotEmpty(pathologicalMap.get("1240CB"))) {
			Annotation annotation2 = new Annotation();
			annotation2.setSingleSlideId(jsonTask.getSingleId());
			annotation2.setCategoryId(pathologicalMap.get("1240CB"));
			annotation2.setSequenceNumber(sequenceNumber);
			Integer result2 = annotationMapper.countDucts(annotation2);
			AiForecast aiForecast2 = new AiForecast();
			aiForecast2.setQuantitativeIndicators("黄体数量");
			aiForecast2.setQuantitativeIndicatorsEn("Corpus luteum numbers");
			aiForecast2.setUnit("个");
			aiForecast2.setResults(result2.toString());
			aiForecast2.setSingleSlideId(jsonTask.getSingleId());
			insertEntity.add(aiForecast2);
		}

		aiForecastService.saveBatch(insertEntity);

	}

	
	@Override
    public String getAlgorithmCode() {
        return "Ovaries";
    }
}
