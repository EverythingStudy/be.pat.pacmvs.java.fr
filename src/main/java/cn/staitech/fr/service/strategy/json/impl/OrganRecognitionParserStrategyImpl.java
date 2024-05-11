package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.service.strategy.json.ParserStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: wangfeng
 * @create: 2024-05-10 14:18:48
 * @Description: Organ_recognition Json Parser
 */
@Slf4j
@Component("Organ_recognition")
public class OrganRecognitionParserStrategyImpl implements ParserStrategy {
    /**
     * 解析文件路径，并存入MySQL
     *
     * @param jsonTask
     * @return
     */
    @Override
    public List<JsonFile> parseJsonFileList(JsonTask jsonTask) {
        List<JsonFile> list = new ArrayList<>();

        return list;
    }


    @Override
    public void submitTask(JsonTask jsonTask) {

    }

    @Override
    public void parseJson(String filePath) {

    }
}
