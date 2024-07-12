package cn.staitech.fr.service.strategy.json;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.config.MapConstant;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.service.AnnotationService;
import cn.staitech.fr.utils.AreaUtils;
import cn.staitech.fr.vo.geojson.Properties;
import com.alibaba.fastjson.JSONArray;
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
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CommonJsonParser
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
            //
            if (null != geometry0) {
                annotation.setContour5000(geometry0.toString());
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
            annotation.setCellType(properties.getCell_type());
            JsonNode node2 = geometry.get("type");
            annotation.setLocationType(node2.asText());
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
        int bathSize = 5000;

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
                                    Annotation annotation = handleSingleJsonElement(element, pathologicalMap, jsonTask);
                                    if (!ObjectUtil.isEmpty(annotation)) {
                                        return processAnnotation(finalResolutionX, annotation);
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
                        return processAnnotation(finalResolutionX, annotation);
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
                    // 查询有效精细轮廓列表
//                    List<Annotation> annotationType3 = annotationMapper.selectAnnotationIsValid(annotation);
//                    List<String> contourList = annotationType3.stream().map(Annotation::getContour).collect(Collectors.toList());
//                    annotation3.setContourList(contourList);
                    annotation3.setSequenceNumber(sequenceNumber);
                    annotation3.setSingleSlideId(jsonTask.getSingleId());
                    annotation3.setInsideOrOutside(false);
                    annotationMapper.deleteAiAnnotation(annotation3);
                }
            }
            // 删除甲状旁腺内所有数据
            // 查询甲状旁腺精细轮廓进行合并
            if (Objects.equals(jsonTask.getAlgorithmCode(), "Thyroid_gland")) {
                Annotation annotation1 = new Annotation();
                annotation1.setMagnification(40000L);
                annotation1.setFiligreeContour(true);
                annotation1.setSingleSlideId(jsonTask.getSingleId());
                LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
                categoryLambdaQueryWrapper.eq(Category::getOrganEn, "Parathyroid").eq(Category::getSpecies, 1);
                Category category = categoryMapper.selectOne(categoryLambdaQueryWrapper);
                annotation1.setCategoryId(category.getCategoryId());
                Annotation annotation4 = annotationMapper.stUnionContourArea(annotation1);
                annotation4.setContour(annotation4.getCollectContour());
                Annotation annotation2 = annotationMapper.stIsValid(annotation4);
                if (ObjectUtil.equals(annotation2.getResults(), "t")) {
                    annotation4.setSequenceNumber(sequenceNumber);
                    annotation4.setSingleSlideId(jsonTask.getSingleId());
                    annotation4.setInsideOrOutside(true);
                    annotationMapper.deleteAiAnnotation(annotation4);
                }
            }
        } catch (Exception e) {
            log.error("Unexpected error occurred: " + e.getMessage(), e);
        }

    }

    /**
     * 处理周长、面积等
     *
     * @param finalResolutionX
     * @param annotation
     * @return
     */
    private Annotation processAnnotation(String finalResolutionX, Annotation annotation) {
        annotation.setContour(annotation.getContour40000());
        Annotation area = annotationMapper.getArea(annotation);
        BigDecimal decimal = new BigDecimal(ObjectUtil.isNotEmpty(area.getArea()) ? area.getArea() : "0");
        annotation.setArea(decimal.multiply(new BigDecimal(finalResolutionX)).multiply(new BigDecimal(finalResolutionX)).setScale(3, RoundingMode.HALF_UP).toString());
        String perimeter = ObjectUtil.isNotEmpty(area.getPerimeter()) ? area.getPerimeter() : "0";// 周长
        String multiply = new BigDecimal(perimeter).multiply(new BigDecimal(finalResolutionX)).setScale(3, RoundingMode.HALF_UP).toString();
        annotation.setPerimeter(multiply);
        return annotation;
    }

    /**
     * get resolutionX
     *
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
     *
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
    Map<Long, Map<String, Long>> pathologicalHasMap = new HashMap<>();

    public Map<String, Long> getPathologicalMap(Long organizationId) {
        Map<String, Long> pathlogicalMap = pathologicalHasMap.get(organizationId);
        if (pathlogicalMap == null) {
            LambdaQueryWrapper<PathologicalIndicatorCategory> CategoryQueryWrapper = new LambdaQueryWrapper<>();
            CategoryQueryWrapper.eq(PathologicalIndicatorCategory::getDelFlag, 0).eq(PathologicalIndicatorCategory::getOrganizationId, organizationId);
            List<PathologicalIndicatorCategory> list = pathologicalIndicatorCategoryMapper.selectList(CategoryQueryWrapper);
            pathlogicalMap = list.stream().collect(Collectors.toMap(PathologicalIndicatorCategory::getStructureId, PathologicalIndicatorCategory::getCategoryId, (entity1, entity2) -> entity1));
            pathologicalHasMap.put(organizationId, pathlogicalMap);
            return pathlogicalMap;
        }
        return pathlogicalMap;
    }


    /**
     * 定位表
     *
     * @param specialId 专题ID
     * @return 表后缀
     */

    HashMap<Long, Long> sequenceNumberMap = new HashMap<>();

    public Long getSequenceNumber(Long specialId) {
        Long sequenceNumber = sequenceNumberMap.get(specialId);
        if (ObjectUtil.isEmpty(sequenceNumber)) {
            LambdaQueryWrapper<SpecialAnnotationRel> SpecialQueryWrapper = new LambdaQueryWrapper<>();
            SpecialQueryWrapper.eq(SpecialAnnotationRel::getSpecialId, specialId);
            SpecialAnnotationRel annotationRel = specialAnnotationRelMapper.selectOne(SpecialQueryWrapper);
            sequenceNumberMap.put(specialId, annotationRel.getSequenceNumber());
            return annotationRel.getSequenceNumber();
        }
        return sequenceNumber;


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
                annotation.setStructurePerimeterNum(structureAreaNum.multiply(new BigDecimal("0.001")));
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
            Annotation annotation1 = annotationMapper.stMakeValid(annotation);
            String result1 = annotationMapper.stIsValid(annotation1).getResults();
            if (Objects.equals(result1, "t")) {
                annotation.setContour(annotation1.getContour());
            } else {
                return new Annotation();
            }
        }
        annotation.setInsideOrOutside(InsideOrOutside);
        annotation.setCategoryId(pathologicalMap.get(structureIds));
        // 查询面积和周长

        return getAnnotationMessage(annotation);
    }


    public Annotation getContourInsideOrOutside(JsonTask jsonTask, String contour, String structureIds, Boolean InsideOrOutside) {
        // 查询所有未被删除且登录机构相同的数据
        Map<String, Long> pathologicalMap = getPathologicalMap(jsonTask.getOrganizationId());
        // 定位表
        Long sequenceNumber = getSequenceNumber(jsonTask.getSpecialId());

        // 脏器轮廓信息
        Annotation annotation = new Annotation();
        annotation.setSequenceNumber(sequenceNumber);

        annotation.setContour(contour);
        // 校验轮廓的合理性
        String result = annotationMapper.stIsValid(annotation).getResults();
        if (Objects.equals(result, "f")) {
            Annotation annotation1 = annotationMapper.stMakeValid(annotation);
            String result1 = annotationMapper.stIsValid(annotation1).getResults();
            if (Objects.equals(result1, "t")) {
                annotation.setContour(annotation1.getContour());
            } else {
                return new Annotation();
            }
        }
        annotation.setSingleSlideId(jsonTask.getSingleId());
        annotation.setInsideOrOutside(InsideOrOutside);
        annotation.setCategoryId(pathologicalMap.get(structureIds));
        // 查询面积和周长
        return getAnnotationMessage(annotation);
    }


    public void putAnnotationDynamicData(JsonTask jsonTask, String structureId, String structureIds, Annotation annotation) {
        Long sequenceNumber = getSequenceNumber(jsonTask.getSpecialId());
        List<Annotation> annotationList1 = getStructureContourList(jsonTask, structureId);
        for (Annotation i : annotationList1) {
            Annotation annotationBy = getContourInsideOrOutside(jsonTask, i.getContour(), structureIds, true);
            // 判断每个元素的data
            List<String> list = new ArrayList<>();
            JSONArray jsonArray = new JSONArray();
            if (i.getDynamicDataList() != null) {
                JSONObject jsonObject = JSONObject.parseObject(i.getDynamicDataList().toString());
                if (jsonObject.getJSONArray("dynamicData") != null) {
                    jsonArray = jsonObject.getJSONArray("dynamicData");
                    for (int j = 0; j < jsonArray.size(); j++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(j);
                        list.add(jsonObject1.getString("name"));
                    }
                }
            }
            if (annotation.getAreaName() != null) {
                DynamicData dynamicData = new DynamicData();
                dynamicData.setName(annotation.getAreaName());
                dynamicData.setData(annotationBy.getStructureAreaNum().setScale(3, RoundingMode.HALF_UP).toString());
                dynamicData.setUnit(annotation.getAreaUnit());
                jsonArray = updateDynamicDataList(list, jsonArray, dynamicData);
                list = addList(list, annotation.getAreaName());
            }
            if (annotation.getPerimeterName() != null) {
                DynamicData dynamicData = new DynamicData();
                dynamicData.setName(annotation.getPerimeterName());
                dynamicData.setData(String.valueOf(annotationBy.getStructurePerimeterNum().setScale(3, RoundingMode.HALF_UP)));
                dynamicData.setUnit(annotation.getPerimeterUnit());
                jsonArray = updateDynamicDataList(list, jsonArray, dynamicData);
                list = addList(list, annotation.getPerimeter());
            }
            if (annotation.getCountName() != null) {
                DynamicData dynamicData = new DynamicData();
                dynamicData.setName(annotation.getCountName());
                dynamicData.setData(String.valueOf(annotationBy.getCount()));
                dynamicData.setUnit(annotation.getCountUnit());
                jsonArray = updateDynamicDataList(list, jsonArray, dynamicData);
            }
            if (jsonArray.size() > 0) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("dynamicData", jsonArray);
                i.setSequenceNumber(sequenceNumber);
                i.setDynamicData(jsonObject.toString());
                annotationMapper.aiUpdateById(i);
            }
        }
    }

    /**
     * @param jsonTask
     * @param structureId
     * @param structureIds
     * @param annotation
     * @param type         1：面积转10（3）平方微米  2:平方微米
     */
    public void putAnnotationDynamicData(JsonTask jsonTask, String structureId, String structureIds, Annotation annotation, Integer type) {
        Long sequenceNumber = getSequenceNumber(jsonTask.getSpecialId());
        List<Annotation> annotationList1 = getStructureContourList(jsonTask, structureId);
        for (Annotation i : annotationList1) {
            Annotation annotationBy = getContourInsideOrOutside(jsonTask, i.getContour(), structureIds, true);
            // 判断每个元素的data
            List<String> list = new ArrayList<>();
            JSONArray jsonArray = new JSONArray();
            if (i.getDynamicDataList() != null) {
                JSONObject jsonObject = JSONObject.parseObject(i.getDynamicDataList().toString());
                if (jsonObject.getJSONArray("dynamicData") != null) {
                    jsonArray = jsonObject.getJSONArray("dynamicData");
                    for (int j = 0; j < jsonArray.size(); j++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(j);
                        list.add(jsonObject1.getString("name"));
                    }
                }
            }
            if (annotation.getAreaName() != null) {
                DynamicData dynamicData = new DynamicData();
                dynamicData.setName(annotation.getAreaName());
                if (type == 1) {
                    dynamicData.setData(convertToSquareMicrometer(String.valueOf(annotationBy.getStructureAreaNum())));
                } else if (type == 2) {
                    dynamicData.setData(String.valueOf(convertToMicrometer(annotationBy.getStructureAreaNum().setScale(3, RoundingMode.HALF_UP).toString())));
                }
                dynamicData.setUnit(annotation.getAreaUnit());
                jsonArray = updateDynamicDataList(list, jsonArray, dynamicData);
                list = addList(list, annotation.getAreaName());
            }
            if (annotation.getPerimeterName() != null) {
                DynamicData dynamicData = new DynamicData();
                dynamicData.setName(annotation.getPerimeterName());
                dynamicData.setData(String.valueOf(annotationBy.getStructurePerimeterNum()));
                dynamicData.setUnit(annotation.getPerimeterUnit());
                jsonArray = updateDynamicDataList(list, jsonArray, dynamicData);
                list = addList(list, annotation.getPerimeterName());
            }
            if (annotation.getCountName() != null) {
                DynamicData dynamicData = new DynamicData();
                dynamicData.setName(annotation.getCountName());
                dynamicData.setData(String.valueOf(annotationBy.getCount()));
                dynamicData.setUnit(annotation.getCountUnit());
                jsonArray = updateDynamicDataList(list, jsonArray, dynamicData);
            }
            if (jsonArray.size() > 0) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("dynamicData", jsonArray);
                i.setSequenceNumber(sequenceNumber);
                i.setDynamicData(jsonObject.toString());
                annotationMapper.aiUpdateById(i);
            }
        }
    }


    //  1：面积转10（3）平方微米  2:平方微米 3:平方毫米
    public void putSingleAnnotationDynamicData(JsonTask jsonTask, String structureId, Annotation annotation, Integer type) {
        Long sequenceNumber = getSequenceNumber(jsonTask.getSpecialId());
        // 查询出单个标注
        List<Annotation> annotationList1 = getStructureContourList(jsonTask, structureId);
        for (Annotation i : annotationList1) {
            // 判断每个元素的data
            List<String> list = new ArrayList<>();
            JSONArray jsonArray = new JSONArray();
            if (i.getDynamicDataList() != null) {
                JSONObject jsonObject = JSONObject.parseObject(i.getDynamicDataList().toString());
                if (jsonObject.getJSONArray("dynamicData") != null) {
                    jsonArray = jsonObject.getJSONArray("dynamicData");
                    for (int j = 0; j < jsonArray.size(); j++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(j);
                        list.add(jsonObject1.getString("name"));
                    }
                }
            }
            if (annotation.getAreaName() != null) {
                DynamicData dynamicData = new DynamicData();
                dynamicData.setName(annotation.getAreaName());
                if (type == 1) {
                    dynamicData.setData(String.valueOf(convertToSquareMicrometer(i.getStructureAreaNum().toString())));
                } else if (type == 2) {
                    dynamicData.setData(String.valueOf(convertToMicrometer(i.getStructureAreaNum().toString())));
                } else if (type == 3) {
                    dynamicData.setData(String.valueOf(i.getStructureAreaNum().setScale(3, RoundingMode.HALF_UP)));
                }
                dynamicData.setUnit(annotation.getAreaUnit());
                jsonArray = updateDynamicDataList(list, jsonArray, dynamicData);
                list = addList(list, annotation.getAreaName());
            }
            if (annotation.getPerimeterName() != null) {
                DynamicData dynamicData = new DynamicData();
                dynamicData.setName(annotation.getPerimeterName());
                if (type == 1) {
                    dynamicData.setData(String.valueOf(convertToSquareMicrometer(i.getStructurePerimeterNum().toString())));
                } else if (type == 2) {
                    dynamicData.setData(String.valueOf(convertToMicrometer(i.getStructurePerimeterNum().toString())));
                } else if (type == 3) {
                    dynamicData.setData(String.valueOf(i.getStructurePerimeterNum().setScale(3, RoundingMode.HALF_UP)));
                }
                dynamicData.setUnit(annotation.getPerimeterUnit());
                jsonArray = updateDynamicDataList(list, jsonArray, dynamicData);
            }
            if (jsonArray.size() > 0) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("dynamicData", jsonArray);
                i.setSequenceNumber(sequenceNumber);
                i.setDynamicData(jsonObject.toString());
                annotationMapper.aiUpdateById(i);
            }
        }
    }


    public void putAnnotationDynamicDataBy(JsonTask jsonTask, Annotation annotation) {
        Long sequenceNumber = getSequenceNumber(jsonTask.getSpecialId());
        DynamicData dynamicData = new DynamicData();
        // 判断每个元素的data
        List<String> list = new ArrayList<>();
        JSONArray jsonArray = new JSONArray();
        if (annotation.getDynamicDataList() != null) {
            JSONObject jsonObject = JSONObject.parseObject(annotation.getDynamicDataList().toString());
            if (jsonObject.getJSONArray("dynamicData") != null) {
                jsonArray = jsonObject.getJSONArray("dynamicData");
                for (int j = 0; j < jsonArray.size(); j++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(j);
                    list.add(jsonObject1.getString("name"));
                }
            }
        }
        if (annotation.getAreaName() != null) {
            dynamicData.setName(annotation.getAreaName());
            dynamicData.setData(String.valueOf(annotation.getAreaValue()));
            dynamicData.setUnit(annotation.getAreaUnit());
            jsonArray = updateDynamicDataList(list, jsonArray, dynamicData);
        }
        if (annotation.getPerimeterName() != null) {
            dynamicData.setName(annotation.getPerimeterName());
            dynamicData.setData(String.valueOf(annotation.getPerimeterValue()));
            dynamicData.setUnit(annotation.getPerimeterUnit());
            jsonArray = updateDynamicDataList(list, jsonArray, dynamicData);
        }
        if (jsonArray.size() > 0) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("dynamicData", jsonArray);
            annotation.setSequenceNumber(sequenceNumber);
            annotation.setDynamicData(jsonObject.toString());
            annotationMapper.aiUpdateById(annotation);
        }
    }


    public JSONArray updateDynamicDataList(List<String> nameList, JSONArray jsonArray, DynamicData dynamicData) {
        if (nameList.contains(dynamicData.getName())) {
            for (int j = 0; j < jsonArray.size(); j++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(j);
                if (Objects.equals(jsonObject1.getString("name"), dynamicData.getName())) {
                    jsonObject1.put("data", dynamicData.getData());
                }
            }
        } else {
            jsonArray.add(dynamicData);
        }
        return jsonArray;
    }

    public List<String> addList(List<String> nameList, String name) {
        if (!nameList.contains(name)) {
            nameList.add(name);
        }
        return nameList;
    }


    public String convertToMicrometer(String str) {
        BigDecimal result = BigDecimal.ZERO;
        if (!StringUtils.isEmpty(str)) {
            BigDecimal areaNum = new BigDecimal(str).multiply(BigDecimal.valueOf(1000000));
            result = areaNum.setScale(3, BigDecimal.ROUND_HALF_UP);
        }
        return result.toString();
    }

    public String convertToSquareMicrometer(String str) {
        BigDecimal result = BigDecimal.ZERO;
        if (!StringUtils.isEmpty(str)) {
            BigDecimal areaNum = new BigDecimal(str).multiply(BigDecimal.valueOf(1000));
            result = areaNum.setScale(3, BigDecimal.ROUND_HALF_UP);
        }
        return result.toString();
    }


    public Annotation getAnnotationMessage(Annotation annotation) {
        Annotation annotations = annotationMapper.getInsideOrOutside(annotation);
        if (null != annotations) {
            if (StringUtils.isEmpty(annotations.getArea())) {
                annotations.setStructureAreaNum(BigDecimal.ZERO);
            } else {
                BigDecimal structureAreaNum = new BigDecimal(annotations.getArea());
                annotations.setStructureAreaNum(structureAreaNum.multiply(new BigDecimal("0.000001")));
            }
            if (StringUtils.isEmpty(annotations.getPerimeter())) {
                annotations.setStructurePerimeterNum(BigDecimal.ZERO);
            } else {
                BigDecimal structurePerimeterNum = new BigDecimal(annotations.getPerimeter());
                annotations.setStructurePerimeterNum(structurePerimeterNum.multiply(new BigDecimal("0.001")));
            }
        }
        return annotations;
    }

    public List<Annotation> getStructureContourList(JsonTask jsonTask, String structureId) {
        Map<String, Long> pathologicalMap = getPathologicalMap(jsonTask.getOrganizationId());
        Long sequenceNumber = getSequenceNumber(jsonTask.getSpecialId());
        Annotation annotation = new Annotation();
        annotation.setSequenceNumber(sequenceNumber);
        annotation.setSingleSlideId(jsonTask.getSingleId());
        annotation.setCategoryId(pathologicalMap.get(structureId));
        return annotationMapper.aiSelectList(annotation);
    }


    /**
     * 获取脏器轮廓面积（micron）
     *
     * @param jsonTask    jsonTask
     * @param structureId 结构ID
     * @return 脏器面积10³平方微米
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
        // 计算面积
        BigDecimal structureAreaNum = new BigDecimal(structure.getArea());
        return structureAreaNum.multiply(new BigDecimal(0.001)).setScale(3, RoundingMode.HALF_UP);
    }

    /**
     * 占比计算（保留三位小数）
     *
     * @param bigDecimal1
     * @param bigDecimal2
     */
    public BigDecimal getProportion(BigDecimal bigDecimal1, BigDecimal bigDecimal2) {
        BigDecimal proportion;
        if (null == bigDecimal1 || null == bigDecimal2 || bigDecimal1.compareTo(BigDecimal.ZERO) == 0 || bigDecimal2.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        proportion = bigDecimal1.divide(bigDecimal2, 6, RoundingMode.DOWN);
        proportion = proportion.multiply(new BigDecimal("100")).setScale(3, RoundingMode.DOWN);
        return proportion;
    }


    public BigDecimal bigDecimalDivideCheck(BigDecimal bigDecimal1, BigDecimal bigDecimal2) {
        if (null == bigDecimal1 || null == bigDecimal2 || bigDecimal1.compareTo(BigDecimal.ZERO) == 0 || bigDecimal2.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return bigDecimal1.divide(bigDecimal2, 3, RoundingMode.HALF_UP);
    }

    public BigDecimal bigDecimalDivideChecks(BigDecimal bigDecimal1, BigDecimal bigDecimal2) {
        if (null == bigDecimal1 || null == bigDecimal2 || bigDecimal1.compareTo(BigDecimal.ZERO) == 0 || bigDecimal2.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return bigDecimal1.divide(bigDecimal2,9, RoundingMode.HALF_UP);
    }


    public BigDecimal sqrt(BigDecimal number) {
        // 将BigDecimal转换为double进行开方
        double doubleValue = number.doubleValue();
        double sqrtValue = Math.sqrt(doubleValue);
        // 根据所需的精度，将double结果转换回BigDecimal
        MathContext mc = new MathContext(3, RoundingMode.HALF_UP);
        return new BigDecimal(sqrtValue, mc);
    }


    public BigDecimal getProportionMultiply(BigDecimal bigDecimal1, BigDecimal bigDecimal2) {
        BigDecimal proportion;
        if (null == bigDecimal1 || null == bigDecimal2 || bigDecimal1.compareTo(BigDecimal.ZERO) == 0 || bigDecimal2.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        proportion = bigDecimal1.divide(bigDecimal2, 3, RoundingMode.HALF_UP);
        return proportion;
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

    /**
     * @param bigDecimal
     */
    public BigDecimal getBigDecimalValue(BigDecimal bigDecimal) {
        if (null == bigDecimal) {
            return BigDecimal.ZERO;
        }
        return bigDecimal;
    }

    /**
     * @param
     */
    public Integer getIntegerValue(Integer intValue) {
        if (null == intValue) {
            return 0;
        }
        return intValue;
    }


}
