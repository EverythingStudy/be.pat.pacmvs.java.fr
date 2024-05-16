package cn.staitech.fr.service.strategy.json.impl;

import cn.hutool.core.date.DateUtil;
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
import cn.staitech.fr.utils.AreaUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author wudi
 * @Date 2024/5/16 15:26
 * @desc 食管
 */
@Slf4j
@Component("Esophagus")
public class EsophagusParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
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

    @Autowired
    private AreaUtils areaUtils;
    @Resource
    private CommonJsonParser commonJsonParser;
    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        log.info("EsophagusParserStrategyImpl init");
    }

    @Override
    public String getAlgorithmCode() {
        return "Esophagus";
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("食管结构指标米面积计算开始：");
        //组织轮廓面积
        List<AiForecast> insertEntity = new ArrayList<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        //面积
        AiForecast aiForecast = new AiForecast();
        aiForecast.setQuantitativeIndicators("食管面积");
        aiForecast.setQuantitativeIndicatorsEn("Tissue contour area");
        aiForecast.setUnit("平方毫米");
        aiForecast.setSingleSlideId(jsonTask.getSingleId());
        BigDecimal area = areaUtils.getOrganArea(jsonTask, "10F120");
        aiForecast.setResults(new BigDecimal(singleSlide.getArea()).subtract(area).toString());
        aiForecast.setCreateTime(DateUtil.now());
        insertEntity.add(aiForecast);
        aiForecastService.saveBatch(insertEntity);

    }
}
