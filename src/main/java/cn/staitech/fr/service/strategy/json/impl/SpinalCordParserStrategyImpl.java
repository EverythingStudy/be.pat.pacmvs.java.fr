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
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.service.strategy.json.CommonParserStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @ClassName: SpinalCordParserStrategyImpl
 * @Description:大鼠脊髓
 * @author wanglibei
 * @date 2024年5月13日
 * @version V1.0
 */
@Slf4j
@Component("Spinal_cord")
public class SpinalCordParserStrategyImpl  extends AbstractCustomParserStrategy {

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
	@Resource
    private CommonParserStrategy commonParserStrategy;
    @Resource
    private CommonJsonParser commonJsonParser;

	@PostConstruct
	public void init() {
		setCommonJsonParser(commonJsonParser);
		log.info("SpinalCordParserStrategyImpl init");
	}


	@Override
	public void alculationIndicators(JsonTask jsonTask) {

		log.info("大鼠脊髓构指标计算开始");
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

		/*结构	编码
		灰质	1390B3
		白质	1390B2
		中央管	1390B4
		室管膜细胞核	1390B5
		红细胞	139004
		算法输出指标	指标代码（仅限本文档）	单位（保留三位小数点）	备注
		灰质面积（单个）	A	平方毫米	
		白质面积（单个）	B	平方毫米	已扣除灰质
		中央管面积（单个）	C	103平方微米	
		室管膜细胞核数量（单个）	D	个	单个脊髓内数据相加输出
		红细胞面积（单个）	E	平方毫米	单个脊髓内数据相加输出
		组织轮廓面积	F	平方毫米	此轮廓包含脑膜

		产品呈现指标	指标代码（仅限本文档）	单位（保留三位小数点）	English	计算方式	备注
		灰质面积占比（单个）	1	%	Gray matter area（per）	1=A/(A+B)	
		白质面积占比（单个）	2	%	White matter area（per） 	2=B/(A+B)	
		中央管面积占比（单个）	3	%	Central canal area（per）	3=C/A

		室管膜细胞核密度（单个）	4	个/103平方微米	Ependyma nucleus%(per)	4=D/C	
		红细胞面积占比（单个）	5	%	Erythrocyte area%（per）	5=E/(A+B)	
		脊髓面积（单个）	6	平方毫米	Spinal cord area（per）	6=A+B*/	

		List<AiForecast> insertEntity = new ArrayList<>();

		//灰质面积（单个）	A	平方毫米
		BigDecimal bigDecimalA = new BigDecimal(0);
		if (ObjectUtil.isNotEmpty(pathologicalMap.get("1390B3"))) {
			Annotation annotation1 = new Annotation();
			annotation1.setSingleSlideId(jsonTask.getSingleId());
			annotation1.setCategoryId(pathologicalMap.get("1390B3"));
			annotation1.setSequenceNumber(sequenceNumber);
			Annotation structureArea = annotationMapper.getStructureArea(annotation1);
			if(StringUtils.isNotEmpty(structureArea.getArea())){
				bigDecimalA = new BigDecimal(structureArea.getArea());
			}
		}
		//白质面积（单个）	B	平方毫米
		BigDecimal bigDecimalB = new BigDecimal(0);
		if (ObjectUtil.isNotEmpty(pathologicalMap.get("1390B2"))) {
			Annotation annotation1 = new Annotation();
			annotation1.setSingleSlideId(jsonTask.getSingleId());
			annotation1.setCategoryId(pathologicalMap.get("1390B2"));
			annotation1.setSequenceNumber(sequenceNumber);
			Annotation structureArea = annotationMapper.getStructureArea(annotation1);
			if(StringUtils.isNotEmpty(structureArea.getArea())){
				bigDecimalB = new BigDecimal(structureArea.getArea());
			}
		}


		//灰质面积占比（单个）	1	%	Gray matter area（per）	1=A/(A+B)
		AiForecast aiForecast1 = new AiForecast();
		aiForecast1.setQuantitativeIndicators("灰质面积占比（单个）");
		aiForecast1.setQuantitativeIndicatorsEn("Gray matter area（per）");
		aiForecast1.setUnit("%");
		aiForecast1.setSingleSlideId(jsonTask.getSingleId());
		// 执行除法操作
		BigDecimal result = bigDecimalA.divide(bigDecimalB.add(bigDecimalB));
		//保留小数点后3位
		result = result.setScale(3, RoundingMode.HALF_UP);
		aiForecast1.setResults(result.toString());
		insertEntity.add(aiForecast1);


		//白质面积占比（单个）	2	%	White matter area（per） 	2=B/(A+B)
		AiForecast aiForecast2 = new AiForecast();
		aiForecast2.setQuantitativeIndicators("白质面积占比（单个）");
		aiForecast2.setQuantitativeIndicatorsEn("White matter area（per）");
		aiForecast2.setUnit("%");
		aiForecast2.setSingleSlideId(jsonTask.getSingleId());
		// 执行除法操作
		BigDecimal result2 = bigDecimalB.divide(bigDecimalB.add(bigDecimalB));
		//保留小数点后3位
		result2 = result2.setScale(3, RoundingMode.HALF_UP);
		aiForecast2.setResults(result2.toString());
		insertEntity.add(aiForecast2);

		//脊髓面积（单个）	6	平方毫米	Spinal cord area（per）	6=A+B
		AiForecast aiForecast3 = new AiForecast();
		aiForecast3.setQuantitativeIndicators("脊髓面积（单个）");
		aiForecast3.setQuantitativeIndicatorsEn("Spinal cord area（per）");
		aiForecast3.setUnit("平方毫米");
		aiForecast3.setSingleSlideId(jsonTask.getSingleId());
		// 执行除法操作
		BigDecimal result3 = bigDecimalB.add(bigDecimalB);
		//保留小数点后3位
		result3 = result3.setScale(3, RoundingMode.HALF_UP);
		aiForecast3.setResults(result3.toString());
		insertEntity.add(aiForecast3);



		aiForecastService.saveBatch(insertEntity);
	}

	@Override
	public String getAlgorithmCode() {
		return "Spinal_cord";
	}
}
