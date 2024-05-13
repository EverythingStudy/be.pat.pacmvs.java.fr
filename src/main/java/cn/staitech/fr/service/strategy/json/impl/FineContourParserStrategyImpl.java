package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.service.strategy.json.ParserStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author: wangfeng
 * @create: 2024-05-10 14:18:48
 * @Description: Fine_contour Json Parser
 */
@Slf4j
@Component("Fine_contour")
public class FineContourParserStrategyImpl implements ParserStrategy {


    @Override
    public void parseJson(JsonTask jsonTask, JsonFile jsonFileS) {
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
    }
}
