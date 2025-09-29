package cn.staitech.fr.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.config.MapConstant;
import cn.staitech.fr.config.OrganStructureConfig;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.vo.geojson.Properties;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/test")
@Slf4j
public class Test {
    @Resource
    private OrganTagMapper organTagMapper;
    @Resource
    private StructureTagMapper structureTagMapper;
    @Resource
    private OrganStructureConfig organStructureConfig;
    @Resource
    private AnnotationMapper annotationMapper;
    @Resource
    private ImageMapper imageMapper;

    @PostMapping("pathological")
    public void test() {


        List<OrganStructureConfig.OrganStructure> structures = organStructureConfig.getStructures().get("0E");
        Map<String, Long> pathologicalMap = getPathologicalMap(1L);
        System.out.println(pathologicalMap);
    }

    public Map<String, Long> getPathologicalMap(Long organizationId) {
//        LambdaQueryWrapper<OrganTag> categoryQueryWrapper = new LambdaQueryWrapper<>();
//        categoryQueryWrapper.eq(OrganTag::getDelFlag, 0).eq(OrganTag::getOrganizationId, organizationId);
//        List<OrganTag> list = organTagMapper.selectList(categoryQueryWrapper);
//        Map<String, Long> map = list.stream().collect(Collectors.toMap(OrganTag::getOrganTagCode, OrganTag::getOrganTagId, (entity1, entity2) -> entity1));
//        Structure structure = new Structure();
//        structure.setOrganizationId(organizationId);
//        List<Structure> structureList = structureMapper.queryList(structure);
//        return structureList.stream().collect(Collectors.toMap(Structure::getStructureId, s -> s.getLong(map), (entity1, entity2) -> entity1));

        List<StructureTag> list = structureTagMapper.selectList(new LambdaQueryWrapper<>(StructureTag.class).eq(StructureTag::getOrganizationId, organizationId));

        return list.stream().collect(Collectors.toMap(StructureTag::getStructureId, StructureTag::getStructureTagId, (entity1, entity2) -> entity1));
    }

    @PostMapping("parser")
    public void test1() {
        File jsonFile = new File("D:\\home\\data\\aiJson\\65\\105\\210\\11214A.json");
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory jsonFactory = new MappingJsonFactory();

        List<JsonNode> elementsList = new ArrayList<>();
        List<Annotation> processedAnnotations;

        JsonToken current;
        int bathSize = 5000;
        Annotation anno = new Annotation();
        anno.setSequenceNumber(1L);
        String finalResolutionX = getResolutionX();
        try (FileInputStream fis = new FileInputStream(jsonFile); JsonParser jsonParser = jsonFactory.createParser(fis)) {

            current = jsonParser.nextToken();
            if (current != JsonToken.START_OBJECT) {
                log.error("json type error！ : {}", current);
                return;
            }

            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jsonParser.getCurrentName();
                current = jsonParser.nextToken();
                if ("features".equals(fieldName)) {
                    if (current == JsonToken.START_ARRAY) {
                        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                            String node = jsonParser.readValueAsTree().toString();
                            JsonNode jsonNode = mapper.readTree(node);
                            elementsList.add(jsonNode);
                            if (elementsList.size() >= bathSize) {
                                processedAnnotations = elementsList.parallelStream().map(element -> {
                                    Annotation annotation = handleSingleJsonElement(element);
//                                    if (!ObjectUtil.isEmpty(annotation)) {
//                                        return processAnnotation(finalResolutionX, annotation);
//                                    }
                                    return annotation;
                                }).filter(Objects::nonNull).collect(Collectors.toList());
                                anno.setList(processedAnnotations);
                                long start = System.nanoTime();
                                annotationMapper.batchSave(anno);
                                long costMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                                long costSeconds = TimeUnit.MILLISECONDS.toSeconds(costMillis);
                                //log.info("单次开始批量保存时间 singleId:{},url:{},time:{} 秒", jsonTask.getSingleId(), jsonFileS.getFileUrl(), costSeconds);
                                elementsList = new ArrayList<>();
                            }
                        }
                    }

                } else {
                    jsonParser.skipChildren();
                }
            }
            if (CollectionUtil.isNotEmpty(elementsList)) {
                processedAnnotations = elementsList.parallelStream().map(element -> {
                    Annotation annotation = handleSingleJsonElement(element);
                    if (!ObjectUtil.isEmpty(annotation)) {
                        return processAnnotation(finalResolutionX, annotation);
                    }
                    return annotation;
                }).filter(Objects::nonNull).collect(Collectors.toList());
                anno.setList(processedAnnotations);
                annotationMapper.batchSave(anno);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (JsonParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String getResolutionX() {
        Image image = imageMapper.selectById(18790);
        String resolutionX = image.getResolutionX();
        if (StringUtils.isEmpty(resolutionX)) {
            resolutionX = "0.262";
        }
        return resolutionX;
    }

    private Annotation processAnnotation(String finalResolutionX, Annotation annotation) {
        annotation.setContour(annotation.getContour40000());
        Annotation area = annotationMapper.getArea(annotation);
        //面积
        String annoArea = area.getArea();
        BigDecimal areaNumber = new BigDecimal(annoArea.trim());
        // 面积为负数，需要获取有效的面积
        if (areaNumber.compareTo(BigDecimal.ZERO) < 0) {
            annoArea = area.getEffectiveArea();
        }
        BigDecimal decimal = new BigDecimal(ObjectUtil.isNotEmpty(annoArea) ? annoArea : "0");
        annotation.setArea(decimal.multiply(new BigDecimal(finalResolutionX)).multiply(new BigDecimal(finalResolutionX)).setScale(3, RoundingMode.HALF_UP).toString());
        String perimeter = ObjectUtil.isNotEmpty(area.getPerimeter()) ? area.getPerimeter() : "0";// 周长
        String multiply = new BigDecimal(perimeter).multiply(new BigDecimal(finalResolutionX)).setScale(3, RoundingMode.HALF_UP).toString();
        annotation.setPerimeter(multiply);
        return annotation;
    }

    private static Annotation handleSingleJsonElement(JsonNode element) {
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
            JsonNode geometry0 = element.get("geometry0");
            // geometry转换成JSONObject
            JSONObject jsonObject0 = JSONObject.parseObject(JSONObject.toJSONString(geometry0));
            if (null == jsonObject0) {
                log.info("geometry0解析失败");
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
            //
            if (null != geometry0) {
                annotation.setContour5000(geometry0.toString());
            }
            if (null != geometry) {
                annotation.setContourB40000(CommonJsonParser.geoJsonToWkb(geometry.toString()));
            }
            if (null != geometry10000) {
                annotation.setContourB10000(CommonJsonParser.geoJsonToWkb(geometry10000.toString()));
            }
            if (null != geometry2500) {
                annotation.setContourB2500(CommonJsonParser.geoJsonToWkb(geometry2500.toString()));
            }
            if (null != geometry625) {
                annotation.setContourB625(CommonJsonParser.geoJsonToWkb(geometry625.toString()));
            }
            //
            if (null != geometry0) {
                annotation.setContourB5000(CommonJsonParser.geoJsonToWkb(geometry0.toString()));
            }
            annotation.setId(annotationId);
            return annotation;
        } else {
            log.error("Expected an object, but got a non-object node: " + element);
            return null;
        }
    }

    public static byte[] geoJsonToWkb(String geoJson) {
        // 使用GeoTools或JTS解析GeoJSON
        GeometryJSON gjson = new GeometryJSON();
        Geometry geometry = null;
        try {
            geometry = gjson.read(geoJson);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 转换为WKB格式
        WKBWriter wkbWriter = new WKBWriter();
        return wkbWriter.write(geometry);
    }
}
