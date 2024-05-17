package cn.staitech.fr.service.strategy.json.impl;

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
 * @Date 2024/5/16 16:02
 * @desc 大鼠舌
 */
@Slf4j
@Component("Tongue")
public class TongueParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Autowired
    private AreaUtils areaUtils;
    @Resource
    private CommonJsonParser commonJsonParser;

    @Resource
    private SingleSlideMapper singleSlideMapper;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        log.info("TongueParserStrategyImpl init");
    }

    @Override
    public String getAlgorithmCode() {
        return "Tongue";
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("大鼠舌结构指标面积计算：");
        List<AiForecast> insertEntity = new ArrayList<>();
        AiForecast aiForecast = new AiForecast();
        aiForecast.setQuantitativeIndicators("舌面积");
        aiForecast.setQuantitativeIndicatorsEn("Tongue area");
        aiForecast.setUnit("平方毫米");
        aiForecast.setSingleSlideId(jsonTask.getSingleId());
        //组织轮廓面积
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        if(ObjectUtil.isNotEmpty(singleSlide)&& StringUtils.isNotEmpty(singleSlide.getArea())){
            aiForecast.setResults(singleSlide.getArea());
        }
        insertEntity.add(aiForecast);
        aiForecastService.saveBatch(insertEntity);

    }
}
