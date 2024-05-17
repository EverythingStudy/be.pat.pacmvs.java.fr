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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service("Lung")
public class LungParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    private AiForecastService aiForecastService;

    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private CommonJsonParser commonJsonParser;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        log.info("LungParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {

        // 查询精细轮廓面积
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        String accurateArea = singleSlide.getArea();

        // 肺泡上皮细胞核 e 个	14C006
        Integer count = commonJsonParser.getOrganAreaCount(jsonTask, "14C006");

        Double density = count / Double.parseDouble(accurateArea);

        List<AiForecast> insertEntity = new ArrayList<>();

        AiForecast aiForecast = new AiForecast();
        aiForecast.setSingleSlideId(jsonTask.getSingleId());
        aiForecast.setCreateTime(DateUtil.now());

        aiForecast.setQuantitativeIndicators("肺脏面积");
        aiForecast.setQuantitativeIndicatorsEn("Lung area");
        aiForecast.setUnit("平方毫米");
        aiForecast.setResults(accurateArea);
        insertEntity.add(aiForecast);

        aiForecast.setQuantitativeIndicators("肺泡上皮细胞核密度");
        aiForecast.setQuantitativeIndicatorsEn("Nucleus density of alveolar epithelial cell");
        aiForecast.setUnit("个/平方毫米");
        aiForecast.setResults(String.valueOf(density));
        insertEntity.add(aiForecast);

        aiForecastService.saveBatch(insertEntity);



    }

    @Override
    public String getAlgorithmCode() {
        return "Lung";
    }
}
