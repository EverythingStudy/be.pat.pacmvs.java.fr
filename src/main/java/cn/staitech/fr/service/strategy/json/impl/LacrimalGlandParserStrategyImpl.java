package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.service.strategy.json.ParserStrategy;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: wangfeng
 * @create: 2024-05-10 14:18:48
 * @Description: Lacrimal_gland Json Parser
 */
@Slf4j
@Component("Lacrimal_gland")
public class LacrimalGlandParserStrategyImpl implements ParserStrategy {

    private static void handleSingleJsonElement(JsonNode element) {
        if (element.isObject()) {
            // 逐个处理JSON对象的字段
            element.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode valueNode = entry.getValue();
                // 根据需要处理每个字段
                log.info("Key: {}, Value: {}", key, valueNode);
            });
        } else {
            log.error("Expected an object, but got a non-object node: " + element);
        }
    }

    /**
     * 解析文件路径，并存入MySQL
     *
     * @param jsonTask
     * @return
     */
    @Override
    public List<JsonFile> parseJsonFileList(JsonTask jsonTask) {
        JSONObject jsonObject = JSON.parseObject(jsonTask.getData());
        for (String key : jsonObject.keySet()) {

        }

        List<JsonFile> list = new ArrayList<>();

        return list;
    }

    @Override
    public void submitTask(JsonTask jsonTask) {

    }

    @Override
    public void parseJson(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonFactory jsonFactory = objectMapper.getFactory();
        File jsonFile = new File(filePath);
        try (FileInputStream fis = new FileInputStream(jsonFile)) {
            JsonParser jsonParser = jsonFactory.createParser(fis);
            while (!jsonParser.isClosed()) {
                if (jsonParser.nextToken() != null) {
                    JsonNode jsonNode = jsonParser.readValueAsTree();
                    // 处理每个JsonNode
                    if (jsonNode.has("features")) {
                        JsonNode featuresNode = jsonNode.get("features");
                        // 进一步处理 featuresNode
                        if (featuresNode.isArray()) {
                            // 遍历JsonNode数组
                            for (JsonNode jsonElement : featuresNode) {
                                handleSingleJsonElement(jsonElement);
                            }
                        } else {
                            log.warn("Merged JSON data is not an array of objects as expected.");
                        }

                    } else {
                        log.info("'features' field not found in JSON data.");
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error occurred while reading JSON file: " + e.getMessage(), e);
        }
    }
}
