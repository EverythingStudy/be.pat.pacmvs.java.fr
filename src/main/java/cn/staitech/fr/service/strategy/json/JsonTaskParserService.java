package cn.staitech.fr.service.strategy.json;

import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.service.JsonFileService;
import cn.staitech.fr.service.JsonTaskService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author: wangfeng
 * @create: 2024-05-10 15:52:39
 * @Description:
 */
@Service
@Slf4j
public class JsonTaskParserService {

    @Resource
    JsonTaskService jsonTaskService;

    @Resource
    JsonFileService jsonFileService;

    @Resource
    ParserStrategyFactory parserStrategyFactory;


    public void input(String input) {
        JSONObject jsonObject = JSON.parseObject(input);

        // 空数据返回
        if (jsonObject == null || !jsonObject.containsKey("algorithmCode")) {
            return;
        }

        String algorithmCode = jsonObject.get("algorithmCode").toString().trim();
        log.info("algorithmCode:{}", algorithmCode);

        // 解析任务元数据,并存入MySQL
        JsonTask jsonTask = parseJasonTask(jsonObject);
        if (jsonTask == null) {
            return;
        }

        log.info("jsonTask:{}", jsonTask);


        // 解析文件路径，并存入MySQL
        List<JsonFile> jsonFileList = parseJsonFileList(jsonTask);
        log.info("jsonFileList:{}", jsonFileList);

        // 获取解析器
        ParserStrategy parser = parserStrategyFactory.getParserStrategy(algorithmCode);

        for (JsonFile jsonFile : jsonFileList) {
            // TODO:异步线程池
            // 调用策略提交任务
            parser.parseJson(jsonTask, jsonFile);
        }
    }

    /**
     * 解析任务元数据,并存入MySQL
     *
     * @param jsonObject
     * @return
     */
    private JsonTask parseJasonTask(JSONObject jsonObject) {
        try {
            JsonTask jsonTask = new JsonTask();
            jsonTask.setAlgorithmCode(jsonObject.get("algorithmCode").toString());

            jsonTask.setImageId(jsonObject.containsKey("imageId") ? Long.parseLong(jsonObject.get("imageId").toString()) : 0L);
            jsonTask.setSlideId(jsonObject.containsKey("slideId") ? Long.parseLong(jsonObject.get("slideId").toString()) : 0L);
            jsonTask.setSingleId(jsonObject.containsKey("singleId") ? Long.parseLong(jsonObject.get("singleId").toString()) : 0L);
            jsonTask.setSpecialId(jsonObject.containsKey("specialId") ? Long.parseLong(jsonObject.get("specialId").toString()) : 0L);
            jsonTask.setOrganizationId(jsonObject.containsKey("organizationId") ? Long.parseLong(jsonObject.get("organizationId").toString()) : 0L);

            jsonTask.setCode(jsonObject.containsKey("code") ? jsonObject.get("code").toString() : "");
            jsonTask.setMsg(jsonObject.containsKey("msg") ? jsonObject.get("msg").toString() : "");
            jsonTask.setData(jsonObject.containsKey("data") ? jsonObject.get("data").toString() : "");

            jsonTask.setCreateTime(new Date());
            jsonTask.setStartTime(new Date());
            jsonTask.setStatus(0);
            // TODO:判重、丢弃任务

            jsonTaskService.save(jsonTask);
            return jsonTask;
        } catch (Exception e) {
            log.info("json-task-parser-service解析任务元数据异常:{}", e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析文件路径，并存入MySQL
     *
     * @param task
     * @return
     */
    private List<JsonFile> parseJsonFileList(JsonTask task) {
        JSONArray jsonArray = new JSONArray(task.getData());

        List<JsonFile> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            org.json.JSONObject jsonObject = jsonArray.getJSONObject(i);
            JsonFile jsonFile = new JsonFile();

            jsonFile.setStructureName(jsonObject.has("structureName") ? jsonObject.getString("structureName") : "");
            jsonFile.setFileUrl(jsonObject.has("fileUrl") ? jsonObject.getString("fileUrl") : "");

            jsonFile.setTaskId(task.getTaskId());
            jsonFile.setStatus(0);

            jsonFile.setCreateTime(new Date());
            jsonFile.setStartTime(new Date());

            list.add(jsonFile);
        }
        jsonFileService.saveBatch(list);
        return list;
    }
}
