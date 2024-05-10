package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.service.strategy.json.ParserStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author: wangfeng
 * @create: 2024-05-10 14:18:48
 * @Description: GLAMpoints_pytorch Json Parser
 */
@Slf4j
@Component("GLAMpoints_pytorch")
public class GlamPointsPytorchParserStrategyImpl implements ParserStrategy {

    @Override
    public void submitTask(JsonTask jsonTask) {

    }
}
