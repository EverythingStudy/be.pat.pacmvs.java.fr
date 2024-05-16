package cn.staitech.fr.service.strategy.json.impl;

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
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.PathologicalIndicatorCategory;
import cn.staitech.fr.domain.SingleSlide;
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
 * @ClassName: PituitaryParserStrategyImpl
 * @Description:大鼠垂体
 * @author wanglibei
 * @date 2024年5月13日
 * @version V1.0
 */
@Slf4j
@Component("Pituitary")
public class PituitaryParserStrategyImpl  extends AbstractCustomParserStrategy {

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
        log.info("PituitaryParserStrategyImpl init");
    }


	@Override
	public void alculationIndicators(JsonTask jsonTask) {

		log.info("大鼠垂体构指标计算开始");
		QueryWrapper<PathologicalIndicatorCategory> qw = new QueryWrapper<>();
		// 查询所有未被删除且登录机构相同的数据
		qw.eq("del_flag", 0).eq("organization_id", jsonTask.getOrganizationId());
		List<PathologicalIndicatorCategory> list = pathologicalIndicatorCategoryMapper.selectList(qw);
		Map<String, Long> pathologicalMap = list.stream().collect(Collectors.toMap(PathologicalIndicatorCategory::getStructureId, PathologicalIndicatorCategory::getCategoryId, (entity1, entity2) -> entity1));

//		神经部:	10607F
//		神经部细胞核（胶质细胞）:	106080
//		中间部:	106081
//		中间部细胞核:（嫌色细胞或嗜碱性细胞）	106082
//		远侧部:	106083
//		远侧部细胞核（嗜酸性细胞、嗜碱性细胞、嫌色细胞）:	106084
//		红细胞:	106004
//		组织轮廓	:106111

		List<AiForecast> insertEntity = new ArrayList<>();

		//垂体面积 8=H

		//胸骨面积 ==>组织轮廓面积
		if (ObjectUtil.isNotEmpty(pathologicalMap.get("106111"))) {
			AiForecast aiForecast2 = new AiForecast();
			aiForecast2.setQuantitativeIndicators("垂体面积");
			aiForecast2.setQuantitativeIndicatorsEn("Pituitary gland area");
			aiForecast2.setUnit("平方毫米");
			SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
			if(StringUtils.isNotEmpty(singleSlide.getArea())){
				aiForecast2.setResults(singleSlide.getArea());
			}
			aiForecast2.setSingleSlideId(jsonTask.getSingleId());
			insertEntity.add(aiForecast2);
		}


		aiForecastService.saveBatch(insertEntity);
	}

	@Override
    public String getAlgorithmCode() {
        return "Pituitary";
    }
}
