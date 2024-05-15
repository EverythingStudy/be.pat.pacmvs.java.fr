package cn.staitech.fr.service.strategy.json.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.ParserStrategy;
import cn.staitech.fr.vo.geojson.Indicator;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: wangfeng
 * @create: 2024-05-10 14:18:48
 * @Description: Harderian_gland Json Parser 哈氏腺
 */
@Slf4j
@Component("Harderian_gland")
public class HarderianGlandParserStrategyImpl implements ParserStrategy {

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
    @Resource
    private ImageMapper imageMapper;

    private static Annotation handleSingleJsonElement(JsonNode element, Map<String, Long> pathologicalMap, JsonTask jsonTask, String resolutionX) {
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
            Map<String, Indicator> dataIndicators = properties.getData_indicators();
            if (!CollectionUtil.isEmpty(dataIndicators)) {
                dataIndicators.forEach((k, v) -> {
                    if (StringUtils.isNotEmpty(v.getUnit())) {
                        double value = v.getValue();
                        BigDecimal bigDecimal = new BigDecimal(resolutionX);
                        BigDecimal bigDecimal1 = new BigDecimal(value);
                        annotation.setArea(bigDecimal1.multiply(bigDecimal).multiply(bigDecimal).setScale(3, RoundingMode.HALF_UP) + "");
                    }
                });
            }

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


    @Override
    public void parseJson(JsonTask jsonTask, JsonFile jsonFileS) {

//        long startTime = System.currentTimeMillis();
        String filePath = jsonFileS.getFileUrl();
        log.info("hashixian:{}", filePath);

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

            Image image = imageMapper.selectById(jsonTask.getImageId());
            String resolutionX = image.getResolutionX();
            if (StringUtils.isEmpty(resolutionX)) {
                resolutionX = "0.262";
            }
            String finalResolutionX = resolutionX;
            List<Annotation> processedAnnotations;
            processedAnnotations = elementsList.parallelStream()
                    .map(element -> {
                        Annotation annotation = handleSingleJsonElement(element, pathologicalMap, jsonTask, finalResolutionX);
                        if (!ObjectUtil.isEmpty(annotation)) {
                            return annotation;
                        }
                        return null;
                    }).filter(Objects::nonNull).collect(Collectors.toList());

            anno.setList(processedAnnotations);
            log.info("hashixiandaxiao:{}", processedAnnotations.size());
            Annotation annotation = new Annotation();
            annotation.setSequenceNumber(sequenceNumber);
            annotation.setSingleSlideId(jsonTask.getSingleId());
            annotationMapper.deleteAiAnnotation(annotation);
            batchProcessAndSave(anno, 1000);
            annotation.setContour("1");
            annotationMapper.deleteAiAnnotation(annotation);
            log.info("hashixianchenggong.....");
//            long endTime = System.currentTimeMillis();
//            long executionTime = endTime - startTime; // 执行时间，单位毫秒
//            System.out.println("执行时间毫秒："+executionTime);
        } catch (Exception e) {
            log.error("Unexpected error occurred: " + e.getMessage(), e);
        }

    }


    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        /*indicatorResultsMap.put("腺泡面积占比（全片）", new IndicatorAddIn("Duct area%", "", ""));
        indicatorResultsMap.put("腺泡细胞核密度(单个)", new IndicatorAddIn("Nucleus density of acinus", "", ""));
        indicatorResultsMap.put("色素面积占比", new IndicatorAddIn("Epithelial apex cytoplasm area%", "", ""));
        indicatorResultsMap.put("腺泡细胞核密度（全片）", new IndicatorAddIn("Mesenchyme area%", "", ""));*/
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        indicatorResultsMap.put("哈氏腺面积", new IndicatorAddIn("Acinus area%", singleSlide.getArea(), "平方毫米"));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    public void batchProcessAndSave(Annotation annotation, int batchSize) {

        List<Annotation> annotations = annotation.getList();
        if (CollectionUtil.isEmpty(annotations)) {
            return;
        }
        int listSize = annotations.size();

        // 分批处理
        for (int i = 0; i < listSize; i += batchSize) {
            int endIndex = Math.min(i + batchSize, listSize);
            List<Annotation> batch = annotations.subList(i, endIndex);
            Annotation annotation1 = new Annotation();
            annotation1.setSequenceNumber(annotation.getSequenceNumber());
            annotation1.setList(batch);
            try {
                annotationMapper.batchSave(annotation1);
            } catch (Exception e) {
                // 处理异常，例如记录日志
                log.error("Error occurred while processing batch: " + e.getMessage(), e);
            }
        }
    }

}
