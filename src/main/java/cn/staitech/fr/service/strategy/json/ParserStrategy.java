package cn.staitech.fr.service.strategy.json;

import cn.staitech.fr.domain.JsonTask;

/**
 * @author: wangfeng
 * @create: 2024-05-10 14:09:01
 * @Description: Json Parser Strategy
 */

public interface ParserStrategy {

    void submitTask(JsonTask jsonTask);
}
