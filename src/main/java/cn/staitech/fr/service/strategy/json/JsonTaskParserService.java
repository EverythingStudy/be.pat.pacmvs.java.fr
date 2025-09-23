package cn.staitech.fr.service.strategy.json;

import cn.staitech.fr.config.OrganStructureConfig;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.enums.ForecastStatusEnum;
import cn.staitech.fr.enums.JsonTaskStatusEnum;
import cn.staitech.fr.enums.StructureAiStatusEnum;
import cn.staitech.fr.enums.StructureJsonStatusEnum;
import cn.staitech.fr.mapper.AiForecastMapper;
import cn.staitech.fr.mapper.JsonFileMapper;
import cn.staitech.fr.mapper.JsonTaskMapper;
import cn.staitech.fr.mapper.OrganTagMapper;
import cn.staitech.fr.service.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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
    SlideService slideService;
    @Resource
    ParserStrategyFactory parserStrategyFactory;
    @Resource
    private List<CustomParserStrategy> customParserStrategies;

    @Resource
    private SingleSlideService singleSlideService;

    @Resource
    private CommonJsonParser commonJsonParser;

    @Resource
    private AiForecastMapper aiForecastMapper;

    @Resource
    private ContourJsonService contourJsonService;
    @Autowired
    private JsonTaskMapper jsonTaskMapper;
    @Resource
    private OrganTagMapper organTagMapper;

    @Autowired
    private JsonFileMapper jsonFileMapper;
    @Resource
    private OrganStructureConfig organStructureConfig;
    @Autowired
    private ExecutorService executorService;

    /**
     * 创建计算临时表
     * 此方法用于创建一个临时表，用于数据处理或计算目的
     * 它首先检查临时表是否存在，如果不存在则创建一个新的临时表
     *
     * @return 总是返回true，表示执行了创建操作
     */
//    public boolean createCalculateTable() {
//        // 从ThreadLocal中获取序列号，用于标识临时表
//        Long sequenceNumber = ThreadLocalUtils.get(Constants.TEMP_TABLE_KEY);
//        if (sequenceNumber == null) {
//            return false;
//        }
//        // 创建一个Annotation对象，用于存储临时表的相关信息
//        Annotation annotation = new Annotation();
//        annotation.setSequenceNumber(sequenceNumber);
//        // 先确认是否存在这个表了，如果存在就不新建表了
//        Integer existTable = annotationMapper.selectExistTable(annotation);
//        if (existTable != 0) {
//            annotationMapper.dropTable(annotation);
//        }
//        // 1、Sequence
//        // 创建表的序列号，为数据插入提供唯一的标识
//        annotationMapper.createTableSequence(annotation);
//        // 2、建表
//        // 根据Annotation对象中的信息创建临时表
//        annotationMapper.createTable(annotation);
//        return true;
//    }

    /**
     * 删除计算临时表
     * 此方法用于删除之前创建的临时表，以释放资源或清理环境
     * 它检查临时表是否存在，如果存在则删除之
     *
     * @return 总是返回true，表示执行了删除操作
     */
//    public boolean dropCalculateTable() {
//        // 从ThreadLocal中获取序列号，用于标识临时表
//        Long sequenceNumber = ThreadLocalUtils.get(Constants.TEMP_TABLE_KEY);
//        return dropCalculateTable(sequenceNumber);
//    }

//    public boolean dropCalculateTable(Long sequenceNumber) {
//        // 从ThreadLocal中获取序列号，用于标识临时表
//        if (sequenceNumber == null) {
//            return false;
//        }
//        // 创建一个Annotation对象，用于存储临时表的相关信息
//        Annotation annotation = new Annotation();
//        annotation.setSequenceNumber(sequenceNumber);
//        // 先确认是否存在这个表了，如果存在就不新建表了
//        Integer existTable = annotationMapper.selectExistTable(annotation);
//        if (existTable != 0) {
//            annotationMapper.dropTable(annotation);
//        }
//        return true;
//    }
    private boolean updateSingleSlideStatus(Long singleSlideId, String forecastStatus) {
        SingleSlide singleSlide = new SingleSlide();
        singleSlide.setSingleId(singleSlideId);
        //0未预测、1预测成功、2预测失败、3预测中
        singleSlide.setForecastStatus(forecastStatus);
        return singleSlideService.updateById(singleSlide);
    }

    public void input(String input) {
        String algorithmCode = "";
        try {
            JSONObject jsonObject = JSON.parseObject(input);
            Long singleSlideId = 0L;
            if (jsonObject != null && jsonObject.containsKey("singleId")) {
                Integer singleId = Integer.valueOf(jsonObject.get("singleId").toString());
                singleSlideId = singleId.longValue();
            }

            if (jsonObject == null || singleSlideId.equals(0)) {
                log.info("singleSlide id:{} jsonTask 任务为空,新任务不再执行; {}", singleSlideId, jsonObject);
                return;
            }

            if (jsonObject != null && jsonObject.containsKey("algorithmCode")) {
                algorithmCode = jsonObject.get("algorithmCode").toString().trim();
            }

            if (StringUtils.isEmpty(algorithmCode)) {
                log.info("singleSlide id:[{}],算法名称标识是空的,JSON校验失败!", singleSlideId);
                updateSingleSlideStatus(singleSlideId, ForecastStatusEnum.FORECAST_FAIL.getCode());
                return;
            }
            JsonTask jsonTask = parseJasonTask(jsonObject);
//            if (jsonTask == null || jsonTask.getCode().equals("500")) {
//                log.info("singleSlide id:[{}],jsonTask :[{}] JSON解析失败!", singleSlideId, jsonTask);
//                updateSingleSlideStatus(singleSlideId, ForecastStatusEnum.FORECAST_FAIL.getCode());
//                //return;
//            }

            JsonTask taskOld = jsonTaskMapper.selectOne(Wrappers.<JsonTask>lambdaQuery().eq(JsonTask::getSingleId, singleSlideId));
            log.info("singleSlide id:{} 查询已存在任务 {}", singleSlideId, taskOld);
            if (taskOld != null && (JsonTaskStatusEnum.PARSE_FAIL.getCode().equals(taskOld.getStatus()) || JsonTaskStatusEnum.PARSE_SUCCESS.getCode().equals(taskOld.getStatus()))) {
                jsonTask.setTaskId(taskOld.getTaskId());
                jsonTaskService.updateById(jsonTask);
                log.info("singleSlide id:{} 更新已执行成功或失败任务 {}", singleSlideId, jsonTask);
            } else if (taskOld != null && (JsonTaskStatusEnum.PARSE_ING.getCode().equals(taskOld.getStatus()) || JsonTaskStatusEnum.NO_PARSE.getCode().equals(taskOld.getStatus()))) {
                log.info("singleSlide id:{} jsonTask 任务执行中,新任务不再执行; {}", singleSlideId, jsonObject);
                //return;
            } else {
                jsonTaskService.save(jsonTask);
                log.info("singleSlide id:[{}] 新增任务 [{}]", singleSlideId, jsonTask);
            }
            //校验脏器下所有结构是否解析完成
            Boolean flag = verifyCategoryStructure(jsonTask);
            if (!flag) {
                //解析脏器结构文件路径，并存入MySQL
                parseSingleJsonFile(jsonTask, jsonObject);
                Boolean flag1 = verifyCategoryStructure(jsonTask);
                if (flag1) {
                    List<JsonFile> fileList = jsonFileMapper.selectList(Wrappers.<JsonFile>lambdaQuery().eq(JsonFile::getTaskId, jsonTask.getTaskId()).isNotNull(JsonFile::getFileUrl));
                    List<JsonFile> list = fileList.stream().filter(e -> e.getAiStatus() == 0).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(list)) {
                        //结构识别全部失败-->以脏器为单位 (指标计算)结构分析失败-->forecastStatus结构化状态：2
                        updateSingleSlideStatus(jsonTask.getSingleId(), ForecastStatusEnum.FORECAST_FAIL.getCode());
                    } else {
                        //验证精细轮廓是否存在
                        SingleSlide singleSlide = singleSlideService.getById(singleSlideId);
                        if (singleSlide != null && !singleSlide.getAiStatusFine().equals(1)) {
                            jsonTask.setStatus(JsonTaskStatusEnum.PARSE_NOT_START.getCode());
                            jsonTaskService.updateById(jsonTask);
                            log.info("singleSlide id:{} 待开始结构化任务 {}", singleSlideId, jsonTask);
                            return;
                        }
                        executorService.execute(() -> {
                            structureFileCalculate(jsonTask, fileList);
                        });
                    }
                }
            } else {
                //解析脏器结构文件路径，并存入MySQL
                parseSingleJsonFile(jsonTask, jsonObject);
            }
        } catch (Exception e) {
            log.error("Unexpected error occurred: [{}]", e);
            throw new JsonTaskParserException(e.getMessage());
        }
    }

    public void structureFileCalculate(JsonTask jsonTask, List<JsonFile> fileList) {
        updateSingleSlideStatus(jsonTask.getSingleId(), ForecastStatusEnum.FORECAST_ING.getCode());
        //进行指标计算
        log.info("jsonTask id:{} singleSlide id:{} checkJson 进入指标开始 startTime:{}", jsonTask.getTaskId(), jsonTask.getSingleId(), new Date());
        long start = System.nanoTime();
        JsonTaskAiHandler(jsonTask, fileList);
        // 计算耗时（秒）
        long costMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        long costSeconds = TimeUnit.MILLISECONDS.toSeconds(costMillis);
        log.info("jsonTask id:{} singleSlide id:{} checkJson 进入指标结束 endTime:{} 耗时:{} 秒", jsonTask.getTaskId(), jsonTask.getSingleId(), new Date(), costSeconds);
        //部分成功-->以脏器为单位 (指标计算)结构分析完成-->forecastStatus结构化状态：1
        updateSingleSlideStatus(jsonTask.getSingleId(), ForecastStatusEnum.FORECAST_SUCCESS.getCode());
    }

    private Boolean verifyCategoryStructure(JsonTask jsonTask) {
        OrganTag category = organTagMapper.selectById(jsonTask.getCategoryId());
        //AI识别每个脏器对应的结构
        //AI识别每个脏器对应的结构JSON文件
        List<JsonFile> fileList = jsonFileMapper.selectList(Wrappers.<JsonFile>lambdaQuery().eq(JsonFile::getTaskId, jsonTask.getTaskId()));
        if (CollectionUtils.isNotEmpty(fileList)) {
            //Set<String> structureIdSet1 = fileList.stream().map(e -> e.getStructureId()).collect(Collectors.toSet());
            List<String> structureIdSet1 = fileList.stream().map(e -> e.getStructureId()).collect(Collectors.toList());
            return isOrganRecognitionComplete(category.getOrganTagCode(), structureIdSet1);
//            if (structureIdSet1.containsAll(structureIdSet)) {
//                return Boolean.TRUE;
//            }
        }
        return Boolean.FALSE;
    }

    /**
     * 判断指定脏器的所有启用结构是否都已完成识别
     *
     * @param organId             脏器ID
     * @param completedStructures 已完成识别的结构列表
     * @return 是否完成所有结构识别
     */
    public boolean isOrganRecognitionComplete(String organId, List<String> completedStructures) {
        List<OrganStructureConfig.OrganStructure> structures = organStructureConfig.getStructures().get(organId);
        if (structures == null || structures.isEmpty()) {
            return false;
        }

        // 获取该脏器所有启用的结构
        List<String> enabledStructures = Arrays.asList(structures.get(0).getStructureId().split(","));
        //Set<String> enabledStructures = structures.stream().filter(OrganStructureConfig.OrganStructure::getEnabled).map(OrganStructureConfig.OrganStructure::getStructureId).collect(Collectors.toSet());

        // 检查所有启用的结构是否都已完成识别
        return completedStructures.containsAll(enabledStructures);
    }

    private void JsonTaskAiHandler(JsonTask jsonTask, List<JsonFile> jsonFileList) {
        // 获取解析器
        ParserStrategy parser = parserStrategyFactory.getParserStrategy(jsonTask.getAlgorithmCode());
        if (parser == null) {
            for (CustomParserStrategy parserStrategy : customParserStrategies) {
                if (parserStrategy.getAlgorithmCode().equals(jsonTask.getAlgorithmCode())) {
                    parser = parserStrategy;
                }
            }
        }

        if (null == parser) {
            updateSingleSlideStatus(jsonTask.getSingleId(), ForecastStatusEnum.FORECAST_FAIL.getCode());
            jsonTask.setStatus(JsonTaskStatusEnum.PARSE_FAIL.getCode());
            jsonTaskService.updateById(jsonTask);
            log.info("AI预测切片id:{},算法名称标识:{},当前标签无法解析", jsonTask.getSingleId(), jsonTask.getAlgorithmCode());
            return;
        }

        //判断json数据目录是否存在
        for (JsonFile jsonFile : jsonFileList) {
            String fileUrl = jsonFile.getFileUrl();
            File file = new File(fileUrl);
            if (!file.exists()) {
                log.info("AI预测切片id:{},算法名称标识:{},目录不存在，结构：{},地址{}", jsonTask.getSingleId(), jsonTask.getAlgorithmCode(), jsonFile.getStructureId(), fileUrl);
            }
        }

        // 修改任务状态
        jsonTask.setStatus(JsonTaskStatusEnum.PARSE_ING.getCode());
        jsonTaskService.updateById(jsonTask);

        try {
            boolean b = parser.checkJson(jsonTask, jsonFileList);
            if (!b) {
                log.info("AI预测切片id:{},算法名称标识:{},JSON校验失败!", jsonTask.getSingleId(), jsonTask.getAlgorithmCode());
                updateSingleSlideStatus(jsonTask.getSingleId(), ForecastStatusEnum.FORECAST_FAIL.getCode());
                jsonTask.setStatus(JsonTaskStatusEnum.PARSE_FAIL.getCode());
                jsonTaskService.updateById(jsonTask);
                return;
            }
            //log.info("jsonTask id:[{}] singleSlide id:[{}] 创建临时计算表", jsonTask.getTaskId(), jsonTask.getSingleId());
//            ThreadLocalUtils.set(Constants.TEMP_TABLE_KEY, jsonTask.getTaskId());
//            if (!createCalculateTable()) {
//                log.warn("jsonTask id:[{}] singleSlide id:[{}] 创建临时表失败", jsonTask.getTaskId(), jsonTask.getSingleId());
//                return;
//            }
            long starts = System.nanoTime();
            for (JsonFile jsonFile : jsonFileList) {
                long start = System.nanoTime();
                log.info("jsonTask id:[{}] singleSlide id:[{}],Json文件解析开始:{} {} {}", jsonTask.getTaskId(), jsonTask.getSingleId(), System.currentTimeMillis(), jsonFile.getFileUrl(), parser.getClass().getName());
                jsonFile.setStartTime(new Date());
                jsonFile.setStatus(StructureJsonStatusEnum.PARSE_ING.getCode());
                jsonFileService.updateById(jsonFile);
                // 解析json文件
                parser.parseJson(jsonTask, jsonFile);
                // 计算耗时（秒）
                long costMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                long costSeconds = TimeUnit.MILLISECONDS.toSeconds(costMillis);
                double fileSize = getFileSizeInMB(jsonFile.getFileUrl());
                log.info("jsonTask id:[{}] singleSlide id:[{}],Json文件解析结束:{} {} {},文件大小为:[{}M],解析存储pg总共耗时[{}]秒", jsonTask.getTaskId(), jsonTask.getSingleId(), System.currentTimeMillis(), jsonFile.getFileUrl(), parser.getClass().getName(), fileSize, costSeconds);
                jsonFile.setStatus(StructureJsonStatusEnum.PARSE_SUCCESS.getCode());
                jsonFile.setEndTime(new Date());
                jsonFileService.updateById(jsonFile);
            }
            long costMilli = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - starts);
            long costSecond = TimeUnit.MILLISECONDS.toSeconds(costMilli);
            log.info("Json文件解析 jsonTask id:{} singleSlide id:{} ,耗时:{}秒", jsonTask.getTaskId(), jsonTask.getSingleId(), costSecond);
            log.info("jsonTask id:[{}] singleSlide id:[{}] 开始删除原有指标。", jsonTask.getTaskId(), jsonTask.getSingleId());
            //删除原有指标
            LambdaQueryWrapper<AiForecast> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiForecast::getSingleSlideId, jsonTask.getSingleId());
            aiForecastMapper.delete(wrapper);
            log.info("jsonTask id:[{}] singleSlide id:[{}] 开始计算指标。", jsonTask.getTaskId(), jsonTask.getSingleId());
            // 指标计算
            long alculationTime = System.nanoTime();
            parser.alculationIndicators(jsonTask);
            long alculationMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - alculationTime);
            long alculationSeconds = TimeUnit.MILLISECONDS.toSeconds(alculationMillis);
            log.info("jsonTask id:[{}] singleSlide id:[{}] 结束计算指标。耗时:{} 秒", jsonTask.getTaskId(), jsonTask.getSingleId(), alculationSeconds);
            long start = System.nanoTime();
            try {
                log.info("jsonTask id:[{}] singleSlide id:[{}] 开始执行新的anno数据存储。startTime:[{}]", jsonTask.getTaskId(), jsonTask.getSingleId(), new Date());
                contourJsonService.aiJson(jsonFileList, jsonTask);
                // 计算耗时（秒）
                long costMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                long costSeconds = TimeUnit.MILLISECONDS.toSeconds(costMillis);
                log.info("jsonTaskAiJson id:[{}] singleSlide id:[{}] 结束执行新的anno数据存储。endTime:[{}], 耗时: {} 秒", jsonTask.getTaskId(), jsonTask.getSingleId(), new Date(), costSeconds);
            } catch (Exception e) {
                long costMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                long costSeconds = TimeUnit.MILLISECONDS.toSeconds(costMillis);
                log.error("jsonTaskAiJson id:[{}] singleSlide id:[{}] 执行anno数据存储失败，耗时: {} 秒", jsonTask.getTaskId(), jsonTask.getSingleId(), costSeconds, e);
            }
            //log.info("jsonTask id:[{}] singleSlide id:[{}] 删除临时计算表", jsonTask.getTaskId(), jsonTask.getSingleId());
//            dropCalculateTable();
            //transferData(jsonTask.getSpecialId(),ThreadLocalUtils.get(Constants.TEMP_TABLE_KEY));
//            ThreadLocalUtils.delete(Constants.TEMP_TABLE_KEY);
            //log.info("jsonTask id:[{}] singleSlide id:[{}] 处理完成[{}]", jsonTask.getTaskId(), jsonTask.getSingleId(), jsonTask);
            // 修改任务状态
            jsonTask.setStatus(JsonTaskStatusEnum.PARSE_SUCCESS.getCode());
            jsonTask.setEndTime(new Date());
            jsonTaskService.updateById(jsonTask);
            log.info("jsonTask id:[{}] singleSlide id:[{}] 修改状态：[{}]", jsonTask.getTaskId(), jsonTask.getSingleId(), JsonTaskStatusEnum.PARSE_SUCCESS.getCode());
            SingleSlide singleSlide = new SingleSlide();
            singleSlide.setSingleId(jsonTask.getSingleId());
            //0未预测、1预测成功、2预测失败、3预测中
            singleSlide.setForecastStatus(ForecastStatusEnum.FORECAST_SUCCESS.getCode());
            singleSlide.setStructureTime(jsonTask.getStructureTime());
            singleSlideService.updateById(singleSlide);
            log.info("jsonTask id:[{}] singleSlide id:[{}] 修改状态：[{}]", jsonTask.getTaskId(), jsonTask.getSingleId(), ForecastStatusEnum.FORECAST_SUCCESS.getCode());
        } catch (Exception e) {
            updateSingleSlideStatus(jsonTask.getSingleId(), ForecastStatusEnum.FORECAST_FAIL.getCode());
            jsonTask.setStatus(JsonTaskStatusEnum.PARSE_FAIL.getCode());
            jsonTaskService.updateById(jsonTask);
            log.error("jsonTask id:[{}] singleSlide id:[{}] 处理失败:[{}] ,{}", jsonTask.getTaskId(), jsonTask.getSingleId(), jsonTask);
            e.printStackTrace();
        }
    }

    /**
     * 获取指定文件的大小，单位为 MB（保留两位小数）
     *
     * @param filePath 文件路径（JSON 文件或其他）
     * @return 文件大小（以 MB 为单位），如 5.23 MB；若文件不存在或出错，返回 -1
     */
    public static double getFileSizeInMB(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            log.error("文件路径为空");
            return -1;
        }

        Path path = Paths.get(filePath);

        try {
            if (!Files.exists(path)) {
                log.error("文件不存在: " + filePath);
                return -1;
            }

            if (Files.isDirectory(path)) {
                log.error("路径是一个目录，不是文件: " + filePath);
                return -1;
            }

            long sizeInBytes = Files.size(path);
            return Math.round((sizeInBytes / 1024.0 / 1024.0) * 100.0) / 100.0; // 保留两位小数

        } catch (IOException e) {
            log.error("读取文件大小时发生 I/O 错误: " + e.getMessage());
            return -1;
        }
    }

//    private void JsonTaskHandler(JsonTask jsonTask, List<JsonFile> jsonFileList) {
//        // 获取解析器
//        ParserStrategy parser = parserStrategyFactory.getParserStrategy(jsonTask.getAlgorithmCode());
//        if (parser == null) {
//            for (CustomParserStrategy parserStrategy : customParserStrategies) {
//                if (parserStrategy.getAlgorithmCode().equals(jsonTask.getAlgorithmCode())) {
//                    parser = parserStrategy;
//                }
//            }
//        }
//
//        if (null == parser) {
//            updateSingleSlideStatus(jsonTask.getSingleId(), "2");
//            jsonTask.setStatus(JsonTaskStatusEnum.PARSE_FAIL.getCode());
//            jsonTaskService.updateById(jsonTask);
//            log.info("AI预测切片id:{},算法名称标识:{},当前标签无法解析", jsonTask.getSingleId(), jsonTask.getAlgorithmCode());
//            return;
//        }
//
//        //判断json数据目录是否存在
//        for (JsonFile jsonFile : jsonFileList) {
//            String fileUrl = jsonFile.getFileUrl();
//            File file = new File(fileUrl);
//            if (!file.exists()) {
//                log.info("AI预测切片id:{},算法名称标识:{},目录不存在，地址{}", jsonTask.getSingleId(), jsonTask.getAlgorithmCode(), fileUrl);
//                updateSingleSlideStatus(jsonTask.getSingleId(), "2");
//                jsonTask.setStatus(JsonTaskStatusEnum.PARSE_FAIL.getCode());
//                jsonTaskService.updateById(jsonTask);
//                return;
//            }
//        }
//
//        // 修改任务状态
//        jsonTask.setStatus(JsonTaskStatusEnum.PARSE_ING.getCode());
//        jsonTaskService.updateById(jsonTask);
//
//        try {
//            log.info("jsonTask id:[{}] singleSlide id:[{}] checkJson 开始", jsonTask.getTaskId(), jsonTask.getSingleId());
//            boolean b = parser.checkJson(jsonTask, jsonFileList);
//            log.info("jsonTask id:[{}] singleSlide id:[{}] checkJson 结束", jsonTask.getTaskId(), jsonTask.getSingleId());
//            if (!b) {
//                log.info("AI预测切片id:{},算法名称标识:{},JSON校验失败!", jsonTask.getSingleId(), jsonTask.getAlgorithmCode());
//                updateSingleSlideStatus(jsonTask.getSingleId(), "2");
//                jsonTask.setStatus(3);
//                jsonTaskService.updateById(jsonTask);
//                return;
//            }
//            log.info("jsonTask id:[{}] singleSlide id:[{}] 创建临时计算表", jsonTask.getTaskId(), jsonTask.getSingleId());
//            ThreadLocalUtils.set(Constants.TEMP_TABLE_KEY, jsonTask.getTaskId());
//            if (!createCalculateTable()) {
//                log.warn("jsonTask id:[{}] singleSlide id:[{}] 创建临时表失败", jsonTask.getTaskId(), jsonTask.getSingleId());
//                return;
//            }
//            log.info("jsonTask id:[{}] singleSlide id:[{}] 开始解析json。", jsonTask.getTaskId(), jsonTask.getSingleId());
//            for (JsonFile jsonFile : jsonFileList) {
//                log.info("jsonTask id:[{}] singleSlide id:[{}],Json文件解析开始:{} {} {}", jsonTask.getTaskId(), jsonTask.getSingleId(), System.currentTimeMillis(), jsonFile.getFileUrl(), parser.getClass().getName());
//                jsonFile.setStartTime(new Date());
//                jsonFileService.updateById(jsonFile);
//                // 解析json文件
//                parser.parseJson(jsonTask, jsonFile);
//                log.info("jsonTask id:[{}] singleSlide id:[{}],Json文件解析结束:{} {} {} {}", jsonTask.getTaskId(), jsonTask.getSingleId(), System.currentTimeMillis(), jsonFile.getFileUrl(), parser.getClass().getName());
//                jsonFile.setEndTime(new Date());
//                jsonFileService.updateById(jsonFile);
//            }
//            log.info("jsonTask id:[{}] singleSlide id:[{}] 开始删除原有指标。", jsonTask.getTaskId(), jsonTask.getSingleId());
//            //删除原有指标
//            LambdaQueryWrapper<AiForecast> wrapper = new LambdaQueryWrapper<>();
//            wrapper.eq(AiForecast::getSingleSlideId, jsonTask.getSingleId());
//            aiForecastService.remove(wrapper);
//            log.info("jsonTask id:[{}] singleSlide id:[{}] 开始计算指标。", jsonTask.getTaskId(), jsonTask.getSingleId());
//            // 指标计算
//            parser.alculationIndicators(jsonTask);
//
//            try {
//                log.info("jsonTask id:[{}] singleSlide id:[{}] 开始执行新的anno数据存储。", jsonTask.getTaskId(), jsonTask.getSingleId());
//                contourJsonService.aiJson(jsonFileList, jsonTask);
//            } catch (Exception e) {
//                log.error("jsonTask id:[{}] singleSlide id:[{}] 执行anno数据存储失败。", jsonTask.getTaskId(), jsonTask.getSingleId());
//            }
//            log.info("jsonTask id:[{}] singleSlide id:[{}] 删除临时计算表", jsonTask.getTaskId(), jsonTask.getSingleId());
//            dropCalculateTable();
//            //transferData(jsonTask.getSpecialId(),ThreadLocalUtils.get(Constants.TEMP_TABLE_KEY));
//            ThreadLocalUtils.delete(Constants.TEMP_TABLE_KEY);
//            log.info("jsonTask id:[{}] singleSlide id:[{}] 处理完成[{}]", jsonTask.getTaskId(), jsonTask.getSingleId(), jsonTask);
//            // 修改任务状态
//            jsonTask.setStatus(JsonTaskStatusEnum.PARSE_SUCCESS.getCode());
//            jsonTask.setEndTime(new Date());
//            jsonTaskService.updateById(jsonTask);
//            log.info("jsonTask id:[{}] singleSlide id:[{}] 修改状态：[{}]", jsonTask.getTaskId(), jsonTask.getSingleId(), 2);
//            SingleSlide singleSlide = new SingleSlide();
//            singleSlide.setSingleId(jsonTask.getSingleId());
//            //0未预测、1预测成功、2预测失败、3预测中
//            singleSlide.setForecastStatus("1");
//            singleSlide.setStructureTime(jsonTask.getStructureTime());
//            singleSlideService.updateById(singleSlide);
//            log.info("jsonTask id:[{}] singleSlide id:[{}] 修改状态：[{}]", jsonTask.getTaskId(), jsonTask.getSingleId(), 1);
//        } catch (Exception e) {
//            updateSingleSlideStatus(jsonTask.getSingleId(), "2");
//            jsonTask.setStatus(JsonTaskStatusEnum.PARSE_FAIL.getCode());
//            jsonTaskService.updateById(jsonTask);
//            log.error("jsonTask id:[{}] singleSlide id:[{}] 处理失败:[{}]", jsonTask.getTaskId(), jsonTask.getSingleId(), jsonTask);
//            if (log.isDebugEnabled()) {
//                e.printStackTrace();
//            }
//        }
//    }

    private Annotation getAnnotation(JsonTask jsonTask) {
        Long sequenceNumber = commonJsonParser.getSequenceNumber(jsonTask.getSpecialId());

        Annotation annotation = new Annotation();
        annotation.setSequenceNumber(sequenceNumber);
        annotation.setSingleSlideId(jsonTask.getSingleId());
        annotation.setInsideOrOutside(true);
        return annotation;
    }

    /**
     * 解析任务元数据,并存入MySQL
     *
     * @param jsonObject
     * @return
     */
    private JsonTask parseJasonTask(JSONObject jsonObject) {
        try {
            //查询task是否存在
            JsonTask jsonTask = jsonTaskMapper.selectOne(new LambdaQueryWrapper<>(JsonTask.class).eq(JsonTask::getSingleId, jsonObject.containsKey("singleId") ? Long.parseLong(jsonObject.get("singleId").toString()) : 0L));
            if (!Objects.isNull(jsonTask)) {
                return jsonTask;
            }
            jsonTask = new JsonTask();
            jsonTask.setAlgorithmCode(jsonObject.get("algorithmCode").toString());
            Long slideId = jsonObject.containsKey("slideId") ? Long.parseLong(jsonObject.get("slideId").toString()) : 0L;
            jsonTask.setSlideId(slideId);
            jsonTask.setImageId(jsonObject.containsKey("imageId") ? Long.parseLong(jsonObject.get("imageId").toString()) : 0L);
            jsonTask.setSingleId(jsonObject.containsKey("singleId") ? Long.parseLong(jsonObject.get("singleId").toString()) : 0L);
            jsonTask.setOrganizationId(jsonObject.containsKey("organizationId") ? Long.parseLong(jsonObject.get("organizationId").toString()) : 0L);
            //算法结构化时间处理
            Long structureTime = 0L;
            if (jsonObject.containsKey("elapsed_time")) {
                Object eTimeObj = jsonObject.get("elapsed_time");
                if (null != eTimeObj) {
                    String eTimeStr = (String) eTimeObj;
                    if (StringUtils.isNotEmpty(eTimeStr)) {
                        structureTime = (long) (Double.parseDouble(eTimeStr));
                    }
                }
            }
            jsonTask.setStructureTime(structureTime);
            //jsonTask.setCode(jsonObject.containsKey("code") ? jsonObject.get("code").toString() : "");
            //jsonTask.setMsg(jsonObject.containsKey("msg") ? jsonObject.get("msg").toString() : "");
            //jsonTask.setData(jsonObject.containsKey("data") ? jsonObject.get("data").toString() : "");
            jsonTask.setCreateTime(new Date());
            jsonTask.setStartTime(new Date());
            jsonTask.setStatus(JsonTaskStatusEnum.NO_PARSE.getCode());

            Slide slide = slideService.getById(slideId);
            jsonTask.setSpecialId(slide.getProjectId());
            SingleSlide singleSlide = singleSlideService.getById(jsonTask.getSingleId());
            jsonTask.setCategoryId(singleSlide.getCategoryId());
            return jsonTask;
        } catch (Exception e) {
            log.error("jsonTask:[{}],json-task-parser-service解析任务元数据异常:[{}]", jsonObject, e.getMessage());
            if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
            return null;
        }
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
                if (jsonObject.has("fileUrl")) {
                    String fileUrl = jsonObject.getString("fileUrl");
                    if (fileUrl.toLowerCase().endsWith(".json")) {
                        jsonFile.setFileUrl(fileUrl);
                    } else {
                        continue;
                    }
                }

                jsonFile.setTaskId(task.getTaskId());
                jsonFile.setStatus(0);

                jsonFile.setCreateTime(new Date());
                jsonFile.setStartTime(new Date());

                list.add(jsonFile);
            }
            jsonFileService.saveBatch(list);
            log.info("jsonTask id:[{}] singleSlide id:[{}] json list:[{}]", task.getTaskId(), task.getSingleId(), list);

        } catch (JSONException e) {
            log.error("jsonTask id:[{}] singleSlide id:[{}] 解析文件处理失败:[{}]", task.getTaskId(), task.getSingleId(), e.getMessage());
            if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
        return list;
    }

    private List<JsonFile> parseSingleJsonFile(JsonTask task, JSONObject jsonObject) {
        List<JsonFile> jsonFileList = jsonFileService.list(new LambdaQueryWrapper<>(JsonFile.class).eq(JsonFile::getTaskId, task.getTaskId()).eq(JsonFile::getStructureName, jsonObject.getString("structureCode")));
        if (CollectionUtils.isNotEmpty(jsonFileList)) {
            return jsonFileList;
        }
        List<JsonFile> list = new ArrayList<>();
        try {
            JsonFile jsonFile = new JsonFile();
            jsonFile.setStructureName(jsonObject.containsKey("structureCode") ? jsonObject.getString("structureCode") : "");
            jsonFile.setStructureId(jsonObject.containsKey("structureCode") ? jsonObject.getString("structureCode") : "");
            if ("500".equals(jsonObject.getString("code"))) {
                jsonFile.setAiStatus(StructureAiStatusEnum.FAIL.getCode());
            } else {
                jsonFile.setAiStatus(StructureAiStatusEnum.SUCCESS.getCode());
                if (jsonObject.containsKey("file_url")) {
                    String fileUrl = jsonObject.getString("file_url");
                    if (fileUrl.toLowerCase().endsWith(".json")) {
                        jsonFile.setFileUrl(fileUrl);
                    }
                }
            }
            jsonFile.setTaskId(task.getTaskId());
            jsonFile.setStatus(StructureJsonStatusEnum.NO_PARSE.getCode());
            jsonFile.setCreateTime(new Date());
            jsonFile.setStartTime(new Date());
            list.add(jsonFile);
            jsonFileService.saveBatch(list);
            log.info("jsonTask id:[{}] singleSlide id:[{}] json list:[{}]", task.getTaskId(), task.getSingleId(), list);

        } catch (JSONException e) {
            log.error("jsonTask id:[{}] singleSlide id:[{}] 解析文件处理失败:[{}]", task.getTaskId(), task.getSingleId(), e.getMessage());
            if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
        return list;
    }

}