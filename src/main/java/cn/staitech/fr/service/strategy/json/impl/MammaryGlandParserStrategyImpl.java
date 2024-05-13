package cn.staitech.fr.service.strategy.json.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.domain.AiForecast;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.PathologicalIndicatorCategory;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.SpecialAnnotationRel;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.PathologicalIndicatorCategoryMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.ParserStrategy;
import cn.staitech.fr.vo.geojson.Properties;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author wudi
 * @Date 2024/5/13 10:05
 * @desc 乳腺
 */
@Slf4j
@Component("Skin_mammary")
public class MammaryGlandParserStrategyImpl implements ParserStrategy {

    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private PathologicalIndicatorCategoryMapper pathologicalIndicatorCategoryMapper;
    @Resource
    private AnnotationMapper annotationMapper;
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;

    @Override
    public void parseJson(JsonTask jsonTask, JsonFile jsonFileS) {
        log.info("开始解析乳腺组织");
        String filePath = jsonFileS.getFileUrl();

        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2);
        QueryWrapper<SpecialAnnotationRel> wrapper = new QueryWrapper<>();
        wrapper.eq("special_id", jsonTask.getSpecialId());
        SpecialAnnotationRel annotationRel = specialAnnotationRelMapper.selectOne(wrapper);
        Long sequenceNumber = annotationRel.getSequenceNumber();
        Annotation anno = new Annotation();
        anno.setSequenceNumber(sequenceNumber);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonFactory jsonFactory = objectMapper.getFactory();
        File jsonFile = new File(filePath);
        QueryWrapper<PathologicalIndicatorCategory> qw = new QueryWrapper<>();
        // 查询所有未被删除且登录机构相同的数据
        qw.eq("del_flag", 0).eq("organization_id", jsonTask.getOrganizationId());
        List<PathologicalIndicatorCategory> list = pathologicalIndicatorCategoryMapper.selectList(qw);
        Map<String, Long> pathologicalMap = list.stream().collect(Collectors.toMap(PathologicalIndicatorCategory::getStructureId, PathologicalIndicatorCategory::getCategoryId, (entity1, entity2) -> entity1));

        try (FileInputStream fis = new FileInputStream(jsonFile);
             JsonParser jsonParser = jsonFactory.createParser(fis)) {

            List<JsonNode> elementsList = new ArrayList<>();
            while (!jsonParser.isClosed()) {
                JsonToken token = jsonParser.nextToken();
                if (token == null) {
                    break;
                }
                if (token == JsonToken.START_OBJECT) {
                    processObjectNode(objectMapper, jsonParser, elementsList);
                }
            }
            List<Annotation> arrayList = new ArrayList<>();
            Annotation annotation1 = annotationMapper.collectGeometry(jsonTask.getSingleId());
            elementsList.stream().forEach(element -> {
                Annotation annotation = processJsonElement(element, executorService, pathologicalMap, jsonTask);
                if (!ObjectUtil.isEmpty(annotation)) {
                    annotation1.setContour(annotation.getContour40000());
                    Annotation annotationBy = annotationMapper.intersectsGeometry(annotation1);
                    if (ObjectUtil.equals("t", annotationBy.getIntersectsResults())) {
                        arrayList.add(annotation);
                    }
                }
            });
            anno.setList(arrayList);
        } catch (Exception e) {
            log.error("Unexpected error occurred: " + e.getMessage(), e);
        } finally {
            executorService.shutdown();
        }
        batchProcessAndSave(anno, 1000, Runtime.getRuntime().availableProcessors());
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("乳腺结构指标计算开始");
        QueryWrapper<PathologicalIndicatorCategory> qw = new QueryWrapper<>();
        // 查询所有未被删除且登录机构相同的数据
        qw.eq("del_flag", 0).eq("organization_id", jsonTask.getOrganizationId());
        List<PathologicalIndicatorCategory> list = pathologicalIndicatorCategoryMapper.selectList(qw);
        Map<String, Long> pathologicalMap = list.stream().collect(Collectors.toMap(PathologicalIndicatorCategory::getStructureId, PathologicalIndicatorCategory::getCategoryId, (entity1, entity2) -> entity1));
        //定位表
        QueryWrapper<SpecialAnnotationRel> wrapper = new QueryWrapper<>();
        wrapper.eq("special_id", jsonTask.getSpecialId());
        SpecialAnnotationRel annotationRel = specialAnnotationRelMapper.selectOne(wrapper);
        Long sequenceNumber = annotationRel.getSequenceNumber();

        //乳腺腺泡和导管数量
        Annotation annotation1 = new Annotation();
        annotation1.setSingleSlideId(jsonTask.getSingleId());
        annotation1.setCategoryId(pathologicalMap.get("12306C"));
        annotation1.setSequenceNumber(sequenceNumber);
        Integer result = annotationMapper.countDucts(annotation1);
        List<AiForecast> insertEntity = new ArrayList<>();
        AiForecast aiForecast = new AiForecast();
        aiForecast.setQuantitativeIndicators("乳腺腺泡和导管数量");
        aiForecast.setQuantitativeIndicatorsEn("Number of acinus and ducts");
        aiForecast.setUnit("个");
        aiForecast.setResults(result.toString());
        aiForecast.setSingleSlideId(jsonTask.getSingleId());
        insertEntity.add(aiForecast);
        //乳腺面积=H-A-B
        //H
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());

        //查询切片缩放
        String resolution = singleSlideMapper.getImageId(jsonTask.getSlideId());

        //计算A面积
        BigDecimal bigDecimalA = new BigDecimal(0);
        if (ObjectUtil.isNotEmpty(pathologicalMap.get("123005"))) {
            Annotation annotation = new Annotation();
            annotation.setSingleSlideId(jsonTask.getSingleId());
            annotation.setCategoryId(pathologicalMap.get("123005"));
            annotation.setSequenceNumber(sequenceNumber);
            Annotation structureArea = annotationMapper.getStructureArea(annotation);
            if (StringUtils.isNotEmpty(resolution) && StringUtils.isNotEmpty(structureArea.getArea())) {
                BigDecimal bigDecimal = new BigDecimal(resolution);
                BigDecimal bigDecimal1 = new BigDecimal(structureArea.getArea());
                bigDecimalA = bigDecimal1.multiply(bigDecimal).multiply(bigDecimal).multiply(new BigDecimal(0.000001));
            }
        }
        Annotation annotation = new Annotation();
        annotation.setSingleSlideId(jsonTask.getSingleId());
        annotation.setCategoryId(pathologicalMap.get("1230C3"));
        annotation.setSequenceNumber(sequenceNumber);
        Annotation structureArea = annotationMapper.getStructureArea(annotation);
        BigDecimal bigDecimalB = new BigDecimal(0);
        if (StringUtils.isNotEmpty(resolution) && StringUtils.isNotEmpty(structureArea.getArea())) {
            BigDecimal bigDecimal = new BigDecimal(resolution);
            BigDecimal bigDecimal1 = new BigDecimal(structureArea.getArea());
            bigDecimalB = bigDecimal1.multiply(bigDecimal).multiply(bigDecimal).multiply(new BigDecimal(0.000001));
        }
        AiForecast aiForecast1 = new AiForecast();
        aiForecast1.setQuantitativeIndicators("腺上皮面积（全片）");
        aiForecast1.setQuantitativeIndicatorsEn("Acinar epithelial area (all)");
        aiForecast1.setUnit("平方毫米");
        aiForecast1.setSingleSlideId(jsonTask.getSingleId());
        if (StringUtils.isNotEmpty(singleSlide.getArea())) {
            BigDecimal bigDecimal = new BigDecimal(singleSlide.getArea()).subtract(bigDecimalA).subtract(bigDecimalB).setScale(3, RoundingMode.HALF_UP);
            aiForecast1.setResults(bigDecimal.toString());
        }
        insertEntity.add(aiForecast1);

        aiForecastService.saveBatch(insertEntity);
    }

    private static void processObjectNode(ObjectMapper objectMapper, JsonParser jsonParser, List<JsonNode> elementsList) throws IOException {
        ObjectNode objectNode = objectMapper.readTree(jsonParser);
        if (objectNode.has("features")) {
            ArrayNode featuresNode = (ArrayNode) objectNode.get("features");
            if (featuresNode.isArray()) {
                Iterator<JsonNode> elementsIterator = featuresNode.elements();
                while (elementsIterator.hasNext()) {
                    elementsList.add(elementsIterator.next());
                }
            } else {
                log.error("Merged JSON data is not an array of objects as expected at current JSON object.");
            }
        } else {
            log.info("'features' field not found in the current JSON object.");
        }
    }

    private static Annotation processJsonElement(JsonNode element, ExecutorService executorService, Map<String, Long> pathologicalMap, JsonTask jsonTask) {

        try {
            Future<Annotation> future = executorService.submit(() -> MammaryGlandParserStrategyImpl.handleSingleJsonElement(element, pathologicalMap, jsonTask));
            Annotation annotation = future.get();
            future.get(30, TimeUnit.SECONDS);  // 设定超时时间以避免无限等待
            return annotation;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void batchProcessAndSave(Annotation annotation, int batchSize, int threadCount) {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Annotation> annotations = annotation.getList();
        int listSize = annotations.size();

        // 分批处理
        for (int i = 0; i < listSize; i += batchSize) {
            int endIndex = Math.min(i + batchSize, listSize);
            List<Annotation> batch = annotations.subList(i, endIndex);
            // 提交任务到线程池
            executor.submit(() -> {
                Annotation annotation1 = new Annotation();
                annotation1.setSequenceNumber(annotation.getSequenceNumber());
                annotation1.setList(batch);
                try {
                    annotationMapper.batchSave(annotation1);
                } catch (Exception e) {
                    // 处理异常，例如记录日志
                    log.error("Error occurred while processing batch: " + e.getMessage(), e);
                }
            });
        }

        // 等待所有任务完成
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            // 处理中断异常
            Thread.currentThread().interrupt();
        }
    }

    private static Annotation handleSingleJsonElement(JsonNode element, Map<String, Long> pathologicalMap, JsonTask jsonTask) {
        if (element.isObject()) {
            JsonNode node = element.get("id");
            // node 转换成String
            String annotationId = node.asText();
            if (StringUtils.isEmpty(annotationId)) {
                log.info("annotationId解析失败");
                return null;
            }
            JsonNode node1 = element.get("properties");
            // 将node1转换成Properties实体类
            ObjectMapper mapper = new ObjectMapper();
            Properties properties = null;
            try {
                properties = mapper.treeToValue(node1, Properties.class);
            } catch (JsonProcessingException e) {
                log.error("Unexpected error occurred: " + e.getMessage(), e);
            }
            if (null == properties) {
                log.info("properties解析失败");
                return null;
            }
            JsonNode geometry = element.get("geometry");
            // geometry转换成JSONObject
            JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(geometry));
            if (null == jsonObject) {
                log.info("geometry解析失败");
                return null;
            }
            JsonNode geometry10000 = element.get("geometry10000");
            // geometry转换成JSONObject
            JSONObject jsonObject10000 = JSONObject.parseObject(JSONObject.toJSONString(geometry10000));
            if (null == jsonObject10000) {
                log.info("geometry10000解析失败");
                return null;
            }
            JsonNode geometry2500 = element.get("geometry2500");
            // geometry转换成JSONObject
            JSONObject jsonObject2500 = JSONObject.parseObject(JSONObject.toJSONString(geometry2500));
            if (null == jsonObject2500) {
                log.info("geometry2500解析失败");
                return null;
            }
            JsonNode geometry625 = element.get("geometry625");
            // geometry转换成JSONObject
            JSONObject jsonObject625 = JSONObject.parseObject(JSONObject.toJSONString(geometry625));
            if (null == jsonObject625) {
                log.info("geometry625解析失败");
                return null;
            }
            String labelCode = properties.getLabel_code();
            if (StringUtils.isEmpty(labelCode)) {
                log.info("labelCode为空");
                return null;
            }
            String annotationType = properties.getAnnotation_type();
            if (StringUtils.isEmpty(annotationType)) {
                log.info("annotationType为空");
                return null;
            }
            Annotation annotation = new Annotation();
            // 查询标签信息
            annotation.setArea(properties.getArea());
            annotation.setPerimeter(properties.getPerimeter());
            annotation.setCreateBy(0L);
            annotation.setCreateTime(String.valueOf(new Date()));
            annotation.setProjectId(0L);

            if (null != geometry) {
                annotation.setContour40000(geometry.toString());
            }
            if (null != geometry10000) {
                annotation.setContour10000(geometry10000.toString());
            }
            if (null != geometry2500) {
                annotation.setContour2500(geometry2500.toString());
            }
            if (null != geometry625) {
                annotation.setContour625(geometry625.toString());
            }
            annotation.setId(annotationId);
            // 拿到categoryId
            Long categoryId = pathologicalMap.get(labelCode);
            if (null == categoryId) {
                log.info("categoryId解析失败");
                return null;
            }
            annotation.setSlideId(jsonTask.getSlideId());
            annotation.setSingleSlideId(jsonTask.getSingleId());
            annotation.setCategoryId(categoryId);
            annotation.setAnnotationType(annotationType.toUpperCase());
            return annotation;
        } else {
            log.error("Expected an object, but got a non-object node: " + element);
            return null;
        }
    }
}
