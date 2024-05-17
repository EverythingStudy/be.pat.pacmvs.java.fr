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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author wudi
 * @Date 2024/5/16 14:33
 * @desc 十二指肠
 */
@Slf4j
@Component("Duodenum")
public class DuodenumParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        log.info("DuodenumParserStrategyImpl init");
    }

    @Override
    public String getAlgorithmCode() {
        return "Duodenum";
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("十二指肠结构面积计算：");
        //组织轮廓面积
        List<AiForecast> insertEntity = new ArrayList<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        //面积
        AiForecast aiForecast = new AiForecast();
        aiForecast.setQuantitativeIndicators("十二指肠面积");
        aiForecast.setQuantitativeIndicatorsEn("Duodenum area");
        aiForecast.setUnit("平方毫米");
        if(ObjectUtil.isNotEmpty(singleSlide)&& StringUtils.isNotEmpty(singleSlide.getArea())){
            aiForecast.setResults(singleSlide.getArea());
        }
        aiForecast.setSingleSlideId(jsonTask.getSingleId());
        aiForecast.setCreateTime(DateUtil.now());
        insertEntity.add(aiForecast);
        aiForecastService.saveBatch(insertEntity);
    }
}
