package cn.staitech.fr.service.strategy.json.impl.digestive;

import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @Author wudi
 * @Date 2024/5/16 15:19
 * @desc 楚雨xun
 */
@Slf4j
@Component("Stomach")
public class StomachParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private CommonJsonParser commonJsonParser;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        log.info("StomachParserStrategyImpl init");
    }

    @Override
    public String getAlgorithmCode() {
        return "Stomach";
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("大鼠胃结构指标计算：");
    }
}
