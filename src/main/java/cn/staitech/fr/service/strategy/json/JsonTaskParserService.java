package cn.staitech.fr.service.strategy.json;

import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.service.JsonFileService;
import cn.staitech.fr.service.JsonTaskService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: wangfeng
 * @create: 2024-05-10 15:52:39
 * @Description:
 */
@Service
@Slf4j
public class JsonTaskParserService {

    public static final ExecutorService jsonTaskExecutorService = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() - 1,
            Runtime.getRuntime().availableProcessors() * 2,
            // 空闲线程等待工作的超时时间
            10L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(4096),
            new ThreadFactory() {
                public Thread newThread(Runnable r) {
                    return new Thread(r, "json-task-service-thread-" + r.hashCode());
                }
            },
            new ThreadPoolExecutor.DiscardOldestPolicy());

    @Resource
    JsonTaskService jsonTaskService;
    @Resource
    JsonFileService jsonFileService;
    @Resource
    ParserStrategyFactory parserStrategyFactory;
    @Resource
    private List<CustomParserStrategy> customParserStrategies;


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
        int count = jsonFileList.size();
        if (count == 0) {
            return;
        }

        // 获取解析器
        ParserStrategy parser = parserStrategyFactory.getParserStrategy(algorithmCode);
        if (parser == null) {
            for (CustomParserStrategy parserStrategy : customParserStrategies) {
                if (parserStrategy.getAlgorithmCode().equals(algorithmCode)) {
                    parser = parserStrategy;
                }
            }
        }


//        // 线程池 异步  调用策略提交任务
//        for (JsonFile jsonFile : jsonFileList) {
//            ParserStrategy finalParser = parser;
//            finalParser.parseJson(jsonTask, jsonFile);
//        }
//
//        // 指标计算
//        parser.alculationIndicators(jsonTask);
//
//        // 修改任务状态
//        jsonTask.setStatus(1);
//        jsonTaskService.updateById(jsonTask);


        CountDownLatch countDownLatch = new CountDownLatch(count);

        AtomicInteger id = new AtomicInteger();
        // 线程池 异步  调用策略提交任务
        for (JsonFile jsonFile : jsonFileList) {
            try {
                ParserStrategy finalParser = parser;
                jsonTaskExecutorService.submit(() -> {
                            log.info("---> {} {}", id.getAndIncrement(), jsonFile.getFileUrl());
                            finalParser.parseJson(jsonTask, jsonFile);
                        }
                );
            } catch (Exception e) {

            } finally {
                countDownLatch.countDown();
            }
        }

        // 避免主线程无法执行到
        try {
            countDownLatch.await();
            // 指标计算
            parser.alculationIndicators(jsonTask);
            // 修改任务状态
            jsonTask.setStatus(1);
            jsonTaskService.updateById(jsonTask);
        } catch (InterruptedException e) {

        } finally {

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
        JSONArray jsonArray;
        List<JsonFile> list = new ArrayList<>();
        try {
            jsonArray = new JSONArray(task.getData());
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
        } catch (JSONException e) {
            e.printStackTrace();
            log.error("解析文件处理失败:{},taskId是{},singleId是{}", e, task.getTaskId(), task.getSingleId());
        }
        return list;
    }
}
