package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.AiForecast;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.ImageMapper;
import cn.staitech.fr.mapper.PathologicalIndicatorCategoryMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
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
        BigDecimal area = areaUtils.getOrganArea(jsonTask, "10D12E");
        aiForecast.setResults(area.toString());
        insertEntity.add(aiForecast);
        aiForecastService.saveBatch(insertEntity);

    }
}
