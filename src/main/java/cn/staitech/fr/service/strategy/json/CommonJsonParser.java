package cn.staitech.fr.service.strategy.json;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.config.MapConstant;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.service.AnnotationService;
import cn.staitech.fr.vo.geojson.Properties;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
@Service
public class CommonJsonParser {
    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private PathologicalIndicatorCategoryMapper pathologicalIndicatorCategoryMapper;
    @Resource
    private AnnotationMapper annotationMapper;
    @Resource
    private ImageMapper imageMapper;
    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AnnotationService annotationService;

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
            // log.info("MapConstant.STRUCTURESIZR_MAP {}", MapConstant.STRUCTURESIZR_MAP);
            String keys = jsonTask.getOrganizationId() + "" + labelCode;
            Integer size = MapConstant.getStructureSize(keys);
            annotation.setStructureSize(size);

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

    public void parseJson(JsonTask jsonTask, JsonFile jsonFileS) {
        if (checkCategory(jsonTask)) {
            return;
        }

        // 定位表
        Long sequenceNumber = getSequenceNumber(jsonTask.getSpecialId());
        Annotation anno = new Annotation();
        anno.setSequenceNumber(sequenceNumber);

        Map<String, Long> pathologicalMap = getPathologicalMap(jsonTask.getOrganizationId());

        String finalResolutionX = getResolutionX(jsonTask);

        File jsonFile = new File(jsonFileS.getFileUrl());
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory jsonFactory = new MappingJsonFactory();

        List<JsonNode> elementsList = new ArrayList<>();
        List<Annotation> processedAnnotations;

        JsonToken current;
        int bathSize=5000;

        try (FileInputStream fis = new FileInputStream(jsonFile); JsonParser jsonParser = jsonFactory.createParser(fis)) {
            current = jsonParser.nextToken();
            if (current != JsonToken.START_OBJECT) {
                throw new RemoteException("json type error！");
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
                                    Annotation annotation = handleSingleJsonElement(element, pathologicalMap, jsonTask);
                                    if (!ObjectUtil.isEmpty(annotation)) {
                                        annotation.setContour(annotation.getContour40000());
                                        Annotation area = annotationMapper.getArea(annotation);
                                        BigDecimal decimal = new BigDecimal(ObjectUtil.isNotEmpty(area.getArea()) ? area.getArea() : "0");
                                        annotation.setArea(decimal.multiply(new BigDecimal(finalResolutionX)).multiply(new BigDecimal(finalResolutionX)).setScale(3, RoundingMode.HALF_UP).toString());
                                        String perimeter = ObjectUtil.isNotEmpty(area.getPerimeter()) ? area.getPerimeter() : "0";// 周长
                                        String multiply = new BigDecimal(perimeter).multiply(new BigDecimal(finalResolutionX)).setScale(3, RoundingMode.HALF_UP).toString();
                                        annotation.setPerimeter(multiply);
                                        return annotation;
                                    }
                                    return null;
                                }).filter(Objects::nonNull).collect(Collectors.toList());
                                anno.setList(processedAnnotations);
                                annotationService.batchProcessAndSave(anno, 1000);
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
                    Annotation annotation = handleSingleJsonElement(element, pathologicalMap, jsonTask);
                    if (!ObjectUtil.isEmpty(annotation)) {
                        annotation.setContour(annotation.getContour40000());
                        Annotation area = annotationMapper.getArea(annotation);
                        BigDecimal decimal = new BigDecimal(ObjectUtil.isNotEmpty(area.getArea()) ? area.getArea() : "0");
                        annotation.setArea(decimal.multiply(new BigDecimal(finalResolutionX)).multiply(new BigDecimal(finalResolutionX)).setScale(3, RoundingMode.HALF_UP).toString());
                        String perimeter = ObjectUtil.isNotEmpty(area.getPerimeter()) ? area.getPerimeter() : "0";// 周长
                        String multiply = new BigDecimal(perimeter).multiply(new BigDecimal(finalResolutionX)).setScale(3, RoundingMode.HALF_UP).toString();
                        annotation.setPerimeter(multiply);
                        return annotation;
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toList());
                anno.setList(processedAnnotations);
                annotationService.batchProcessAndSave(anno, 1000);
            }

            Annotation annotation = new Annotation();
            annotation.setMagnification(40000L);
            annotation.setFiligreeContour(true);
            annotation.setSingleSlideId(jsonTask.getSingleId());
            List<Annotation> annotations = annotationMapper.selectListBy(annotation);
            Annotation annotation3 = annotationMapper.collectGeometry(jsonTask.getSingleId());

            // 循环annotations并执行删除操作
            if (CollectionUtil.isNotEmpty(annotations)) {
                annotation3.setContour(annotation3.getCollectContour());
                Annotation annotation2 = annotationMapper.stIsValid(annotation3);
                if (ObjectUtil.equals(annotation2.getResults(), "t")) {
                    annotation3.setSequenceNumber(sequenceNumber);
                    annotation3.setSingleSlideId(jsonTask.getSingleId());
                    annotationMapper.deleteAiAnnotation(annotation3);
                }
            }

        } catch (Exception e) {
            log.error("Unexpected error occurred: " + e.getMessage(), e);
        }

    }

    /**
     * get resolutionX
     * @param jsonTask
     * @return
     */
    private String getResolutionX(JsonTask jsonTask) {
        Image image = imageMapper.selectById(jsonTask.getImageId());
        String resolutionX = image.getResolutionX();
        if (StringUtils.isEmpty(resolutionX)) {
            resolutionX = "0.262";
        }
        return resolutionX;
    }

    /**
     * checkCategory
     * @param jsonTask
     * @return
     */
    private boolean checkCategory(JsonTask jsonTask) {
        QueryWrapper<Category> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("organization_id", jsonTask.getOrganizationId());
        wrapper1.eq("del_flag", 0);
        wrapper1.eq("category_id", jsonTask.getCategoryId());
        Category category = categoryMapper.selectOne(wrapper1);
        if (ObjectUtil.isEmpty(category)) {
            return true;
        }
        return false;
    }


    /**
     * 查询所有未被删除且登录机构相同的数据
     *
     * @param organizationId 机构id
     * @return 指标的结构ID和类别ID
     */
    public Map<String, Long> getPathologicalMap(Long organizationId) {
        LambdaQueryWrapper<PathologicalIndicatorCategory> CategoryQueryWrapper = new LambdaQueryWrapper<>();
        CategoryQueryWrapper.eq(PathologicalIndicatorCategory::getDelFlag, 0).eq(PathologicalIndicatorCategory::getOrganizationId, organizationId);
        List<PathologicalIndicatorCategory> list = pathologicalIndicatorCategoryMapper.selectList(CategoryQueryWrapper);

        return list.stream().collect(Collectors.toMap(PathologicalIndicatorCategory::getStructureId, PathologicalIndicatorCategory::getCategoryId, (entity1, entity2) -> entity1));
    }


    /**
     * 定位表
     *
     * @param specialId 专题ID
     * @return 表后缀
     */
    public Long getSequenceNumber(Long specialId) {
        LambdaQueryWrapper<SpecialAnnotationRel> SpecialQueryWrapper = new LambdaQueryWrapper<>();
        SpecialQueryWrapper.eq(SpecialAnnotationRel::getSpecialId, specialId);
        SpecialAnnotationRel annotationRel = specialAnnotationRelMapper.selectOne(SpecialQueryWrapper);
        return annotationRel.getSequenceNumber();
    }

    /**
     * 获取脏器轮廓面积
     *
     * @param jsonTask    jsonTask
     * @param structureId 结构ID
     * @return 脏器面积-平方毫米
     */
    public Annotation getOrganArea(JsonTask jsonTask, String structureId) {
        // 查询所有未被删除且登录机构相同的数据
        Map<String, Long> pathologicalMap = getPathologicalMap(jsonTask.getOrganizationId());
        // 定位表
        Long sequenceNumber = getSequenceNumber(jsonTask.getSpecialId());

        // 脏器轮廓信息
        Annotation annotation = new Annotation();
        annotation.setSequenceNumber(sequenceNumber);
        annotation.setSingleSlideId(jsonTask.getSingleId());//单脏器切片id
        annotation.setCategoryId(pathologicalMap.get(structureId));// 标注类别ID
        Annotation structure = annotationMapper.getStructureArea(annotation);
        if (null != structure) {
            if (StringUtils.isEmpty(structure.getArea())) {
                annotation.setStructureAreaNum(BigDecimal.ZERO);
            } else {
                BigDecimal structureAreaNum = new BigDecimal(structure.getArea());
                annotation.setStructureAreaNum(structureAreaNum.multiply(new BigDecimal("0.000001")));
            }
            if (StringUtils.isEmpty(structure.getPerimeter())) {
                annotation.setStructurePerimeterNum(BigDecimal.ZERO);
            } else {
                BigDecimal structureAreaNum = new BigDecimal(structure.getPerimeter());
                annotation.setStructureAreaNum(structureAreaNum.multiply(new BigDecimal("0.000001")));
            }
        }
        return annotation;
    }


    public Annotation getInsideOrOutside(JsonTask jsonTask, String structureId, String structureIds, Boolean InsideOrOutside) {
        // 查询所有未被删除且登录机构相同的数据
        Map<String, Long> pathologicalMap = getPathologicalMap(jsonTask.getOrganizationId());
        // 定位表
        Long sequenceNumber = getSequenceNumber(jsonTask.getSpecialId());

        // 脏器轮廓信息
        Annotation annotation = new Annotation();
        annotation.setSequenceNumber(sequenceNumber);
        annotation.setSingleSlideId(jsonTask.getSingleId());//单脏器切片id
        annotation.setCategoryId(pathologicalMap.get(structureId));// 标注类别ID
        // 查询合并后的轮廓数据
        Annotation annotationBy = annotationMapper.collectAiGeometry(annotation);
        if (annotationBy == null) {
            return new Annotation();
        }
        annotation.setContour(annotationBy.getCollectContour());
        // 校验轮廓的合理性
        String result = annotationMapper.stIsValid(annotation).getResults();
        if (Objects.equals(result, "f")) {
            return new Annotation();
        }
        annotation.setInsideOrOutside(InsideOrOutside);
        annotation.setCategoryId(pathologicalMap.get(structureIds));
        Annotation annotations = annotationMapper.getInsideOrOutside(annotation);
        if (null != annotations) {
            if (StringUtils.isEmpty(annotations.getArea())) {
                annotation.setStructureAreaNum(BigDecimal.ZERO);
            } else {
                BigDecimal structureAreaNum = new BigDecimal(annotations.getArea());
                annotation.setStructureAreaNum(structureAreaNum.multiply(new BigDecimal("0.000001")));
            }
            if (StringUtils.isEmpty(annotations.getPerimeter())) {
                annotation.setStructurePerimeterNum(BigDecimal.ZERO);
            } else {
                BigDecimal structureAreaNum = new BigDecimal(annotations.getPerimeter());
                annotation.setStructureAreaNum(structureAreaNum.multiply(new BigDecimal("0.000001")));
            }
        }
        return annotation;
    }

    /**
     * 获取脏器轮廓面积（micron）
     *
     * @param jsonTask    jsonTask
     * @param structureId 结构ID
     * @return 脏器面积-10³平方微米
     */
    public BigDecimal getOrganAreaMicron(JsonTask jsonTask, String structureId) {
        // 查询所有未被删除且登录机构相同的数据
        Map<String, Long> pathologicalMap = getPathologicalMap(jsonTask.getOrganizationId());
        // 定位表
        Long sequenceNumber = getSequenceNumber(jsonTask.getSpecialId());

        // 脏器轮廓信息
        Annotation annotation = new Annotation();
        annotation.setSequenceNumber(sequenceNumber);
        annotation.setSingleSlideId(jsonTask.getSingleId());//单脏器切片id
        annotation.setCategoryId(pathologicalMap.get(structureId));// 标注类别ID
        Annotation structure = annotationMapper.getStructureArea(annotation);
        if (null == structure || StringUtils.isEmpty(structure.getArea())) {
            return BigDecimal.ZERO;
        }

        // 查询切片缩放
        BigDecimal resolutionNum = new BigDecimal("0.262");
        String resolution = singleSlideMapper.getImageId(jsonTask.getSlideId());
        if (StringUtils.isNotEmpty(resolution)) {
            resolutionNum = new BigDecimal(resolution);
        }

        // 计算面积
        BigDecimal structureAreaNum = new BigDecimal(structure.getArea());
        return structureAreaNum.multiply(resolutionNum).multiply(resolutionNum).multiply(new BigDecimal(0.001));
    }

    /**
     * 取脏器轮廓数量
     *
     * @param jsonTask    jsonTask
     * @param structureId 结构ID
     * @return 脏器轮廓数量
     */
    public Integer getOrganAreaCount(JsonTask jsonTask, String structureId) {
        // 查询所有未被删除且登录机构相同的数据
        Map<String, Long> pathologicalMap = getPathologicalMap(jsonTask.getOrganizationId());

        // 定位表
        Long sequenceNumber = getSequenceNumber(jsonTask.getSpecialId());

        // 脏器轮廓信息
        Annotation annotation = new Annotation();
        annotation.setSequenceNumber(sequenceNumber);
        annotation.setSingleSlideId(jsonTask.getSingleId());//单脏器切片id
        annotation.setCategoryId(pathologicalMap.get(structureId));// 标注类别ID
        return annotationMapper.countDucts(annotation);

    }


}
