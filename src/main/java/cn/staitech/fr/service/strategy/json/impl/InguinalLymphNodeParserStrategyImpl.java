package cn.staitech.fr.service.strategy.json.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.ImageMapper;
import cn.staitech.fr.mapper.PathologicalIndicatorCategoryMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.AnnotationService;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
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
 * @Description: Json Parser 腹股沟淋巴结 Inguinal lymph node
 */
@Slf4j
@Component("Inguinal_lymph_node")
public class InguinalLymphNodeParserStrategyImpl implements ParserStrategy {
    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private PathologicalIndicatorCategoryMapper pathologicalIndicatorCategoryMapper;
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private ImageMapper imageMapper;
    @Resource
    private AnnotationService annotationService;
    @Resource
    private CommonJsonParser commonJsonParser;

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

    private List<JsonNode> processObjectNode(ObjectMapper objectMapper, JsonParser jsonParser) {
        try {
            List<JsonNode> elementsList = new ArrayList<>();
            ObjectNode objectNode = objectMapper.readTree(jsonParser);
            if (objectNode.has("features")) {
                ArrayNode featuresNode = (ArrayNode) objectNode.get("features");
                if (featuresNode.isArray()) {
                    Iterator<JsonNode> elementsIterator = featuresNode.elements();
                    while (elementsIterator.hasNext()) {
                        elementsList.add(elementsIterator.next());
                    }
                    return elementsList;
                } else {
                    log.error("Merged JSON data is not an array of objects as expected at current JSON object.");
                }
            } else {
                log.info("'features' field not found in the current JSON object.");
            }
            return null;
        } catch (IOException e) {

        }
        return null;
    }

    @Override
    public void parseJson(JsonTask jsonTask, JsonFile jsonFileS) {
        String filePath = jsonFileS.getFileUrl();
        log.info("大鼠甲状腺面积:{}", filePath);

        QueryWrapper<SpecialAnnotationRel> wrapper = new QueryWrapper<>();
        wrapper.eq("special_id", jsonTask.getSpecialId());
        SpecialAnnotationRel annotationRel = specialAnnotationRelMapper.selectOne(wrapper);
        Long sequenceNumber = annotationRel.getSequenceNumber();
        Annotation anno = new Annotation();
        anno.setSequenceNumber(sequenceNumber);
        log.info("sequenceNumber:{}", sequenceNumber);
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
                    elementsList = processObjectNode(objectMapper, jsonParser);
                    if (elementsList == null) {
                        return;
                    }
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
            log.info("大鼠甲状腺面积:{}", processedAnnotations.size());
            annotationService.batchProcessAndSave(anno, 1000);
        } catch (Exception e) {
            log.error("Unexpected error occurred: " + e.getMessage(), e);
        }

    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("大鼠甲状腺指标计算开始");
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();

        // H:精细轮廓总面积（甲状腺）-平方毫米
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        String accurateArea = singleSlide.getArea();

        // I:甲状旁腺组织轮廓面积-平方毫米
        BigDecimal organArea = commonJsonParser.getOrganArea(jsonTask, "108111");

        // 若甲状腺轮廓面积里包括了甲状旁腺，计算时需要用H-I，若甲状旁腺和甲状腺是分开单独识别的，则只需要H
        if (new BigDecimal(accurateArea).compareTo(BigDecimal.ZERO) > 0
                && organArea.compareTo(BigDecimal.ZERO) > 0) {
            // H-I
            BigDecimal areaNum = new BigDecimal(accurateArea).subtract(organArea);
            accurateArea = areaNum.toString();
        }

        indicatorResultsMap.put("甲状腺面积", new IndicatorAddIn("Thyroid gland area", accurateArea, "平方毫米"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
