package cn.staitech.fr.service.strategy.json;

import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;

import java.util.List;

/**
 * @author: wangfeng
 * @create: 2024-05-10 14:09:01
 * @Description: Json Parser Strategy
 */

public interface ParserStrategy {
    /**
     * 解析文件路径，并存入MySQL
     *
     * @param jsonTask
     * @return
     */
    List<JsonFile> parseJsonFileList(JsonTask jsonTask);

    void submitTask(JsonTask jsonTask);

    void parseJson(String filePath);
}
