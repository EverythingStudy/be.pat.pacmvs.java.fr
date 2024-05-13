package cn.staitech.fr.service.strategy.json;

import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;

/**
 * @author: wangfeng
 * @create: 2024-05-10 14:09:01
 * @Description: Json Parser Strategy
 */

public interface ParserStrategy {
    
    /**
     * 单个Json文件解析
     *
     * @param jsonTask
     * @param jsonFile
     */
    void parseJson(JsonTask jsonTask, JsonFile jsonFile);

    /**
     * 指标计算
     *
     * @param jsonTask
     */
    void alculationIndicators(JsonTask jsonTask);
}
