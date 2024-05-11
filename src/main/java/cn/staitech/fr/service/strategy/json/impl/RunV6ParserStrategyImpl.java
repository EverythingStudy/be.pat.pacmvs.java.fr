package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.service.strategy.json.ParserStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author: wangfeng
 * @create: 2024-05-10 14:18:48
 * @Description: run_V6 Json Parser
 */
@Slf4j
@Component("run_V6")
public class RunV6ParserStrategyImpl implements ParserStrategy {

    @Override
    public void parseJson(JsonTask jsonTask, JsonFile jsonFile) {

    }
}
