package cn.staitech.fr.service.strategy.json.impl;

import cn.hutool.core.date.DateUtil;
import cn.staitech.fr.domain.AiForecast;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service("Trachea")
public class TracheaParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    private AiForecastService aiForecastService;

    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private CommonJsonParser commonJsonParser;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        log.info("TracheaParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {

        // 查询精细轮廓面积
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        String accurateArea = singleSlide.getArea();

        // 查询气管腔面积
        BigDecimal organArea = commonJsonParser.getOrganArea(jsonTask, "14D007");

        // 使用组织轮廓面积减去气管腔面积
        BigDecimal areaNum = new BigDecimal(accurateArea).subtract(organArea);

        List<AiForecast> insertEntity = new ArrayList<>();
        AiForecast aiForecast = new AiForecast();
        aiForecast.setQuantitativeIndicators("气管面积");
        aiForecast.setQuantitativeIndicatorsEn("Tracheal area");
        aiForecast.setUnit("平方毫米");
        aiForecast.setResults(String.valueOf(areaNum));
        aiForecast.setSingleSlideId(jsonTask.getSingleId());
        aiForecast.setCreateTime(DateUtil.now());
        insertEntity.add(aiForecast);
        aiForecastService.saveBatch(insertEntity);
    }

    @Override
    public String getAlgorithmCode() {
        return "Trachea";
    }
}

