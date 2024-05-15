package cn.staitech.fr.service.strategy.json.impl;

import cn.hutool.core.date.DateUtil;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author mugw
 * @version 1.0
 * @description
 * @date 2024/5/13 10:06:53
 */
@Slf4j
@Service("Thymus")
public class ThymusParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.info("ThymusParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
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

        Annotation annotation = new Annotation();
        annotation.setSingleSlideId(jsonTask.getSingleId());
        annotation.setCategoryId(pathologicalMap.get("14403D"));
        annotation.setSequenceNumber(sequenceNumber);
        Annotation structureArea = annotationMapper.getStructureArea(annotation);
        BigDecimal bigDecimalB = new BigDecimal(0);
        //查询切片缩放
        String resolution = singleSlideMapper.getImageId(jsonTask.getSlideId());
        if (StringUtils.isEmpty(resolution)){
            resolution = "0.263";
        }
        if (StringUtils.isNotEmpty(resolution) && StringUtils.isNotEmpty(structureArea.getArea())) {
            BigDecimal bigDecimal = new BigDecimal(resolution);
            BigDecimal bigDecimal1 = new BigDecimal(structureArea.getArea());
            bigDecimalB = bigDecimal1.multiply(bigDecimal).multiply(bigDecimal).multiply(new BigDecimal(0.000001).setScale(3));
        }
        AiForecast aiForecast = new AiForecast();
        aiForecast.setQuantitativeIndicators("胸腺面积");
        aiForecast.setQuantitativeIndicatorsEn("Thymus area (all)");
        aiForecast.setUnit("平方毫米");
        aiForecast.setSingleSlideId(jsonTask.getSingleId());
        aiForecast.setCreateTime(DateUtil.now());
        aiForecast.setResults(bigDecimalB.toString());
        aiForecastService.save(aiForecast);
    }

    @Override
    public String getAlgorithmCode() {
        return "Thymus";
    }
}
