package cn.staitech.fr.service.strategy.json.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.domain.AiForecast;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

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
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
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
        BigDecimal bigDecimal = new BigDecimal(0);
        if(ObjectUtil.isNotEmpty(singleSlide)&& StringUtils.isNotEmpty(singleSlide.getArea())){
            bigDecimal = bigDecimal.add(new BigDecimal(singleSlide.getArea()));
        }
        //面积
        AiForecast aiForecast = new AiForecast();
        aiForecast.setQuantitativeIndicators("食管面积");
        aiForecast.setQuantitativeIndicatorsEn("Tissue contour area");
        aiForecast.setUnit("平方毫米");
        aiForecast.setSingleSlideId(jsonTask.getSingleId());
        BigDecimal area = areaUtils.getOrganArea(jsonTask, "10F120");
        aiForecast.setResults(bigDecimal.subtract(area).setScale(3, RoundingMode.HALF_UP).toString());
        aiForecast.setCreateTime(DateUtil.now());
        insertEntity.add(aiForecast);
        aiForecastService.saveBatch(insertEntity);

    }
}
