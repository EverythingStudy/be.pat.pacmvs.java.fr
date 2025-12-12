package cn.staitech.fr.service.strategy.json;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.config.MapConstant;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.service.AnnotationService;
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
import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;
import org.postgis.PGgeometry;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
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
    private StructureTagMapper structureTagMapper;
    @Resource
    private AnnotationMapper annotationMapper;
    @Resource
    private ImageMapper imageMapper;
    @Resource
    private AnnotationService annotationService;
    @Resource
    private OrganTagMapper organTagMapper;
    @Resource(name = "dynamicDataThreadPool")
    private ExecutorService dynamicDataThreadPool;

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
            if (null != geometry0) {
                annotation.setContour5000(geometry0.toString());
            }
            annotation.setId(annotationId);
            // 拿到categoryId
            Long categoryId = pathologicalMap.get(labelCode);
            if (null == categoryId) {
                log.info("categoryId解析失败,labelCode:[{}]", labelCode);
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

    /**
     * 将GeoJSON转换为PostGIS几何对象
     */
    private static PGgeometry convertGeoJsonToPGGeometry(String geoJson) {
        if (StringUtils.isEmpty(geoJson)) {
            return null;
        }
        try {
            return new PGgeometry(geoJson);
        } catch (Exception e) {
            log.error("Failed to convert GeoJSON to PGGeometry: " + geoJson, e);
            return null;
        }
    }

    public void parseJson(JsonTask jsonTask, JsonFile jsonFileS) {
        log.info("parseJson -------------->  Json文件解析开始:{} {} {} {}", System.currentTimeMillis(), jsonFileS.getFileUrl(), jsonTask);
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
            log.info("parseJson --------------> Json文件解析读取文件流(FileInputStream)开始:{} {} {} {}", System.currentTimeMillis(), jsonFileS.getFileUrl(), jsonTask);

            current = jsonParser.nextToken();
            if (current != JsonToken.START_OBJECT) {
                log.error("json type error！ : {}", current);
                return;
            }

            log.info("parseJson -------------->  Json文件解析 while loop start:{} {} {} {}", System.currentTimeMillis(), jsonFileS.getFileUrl(), jsonTask);

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
                                long start = System.nanoTime();
                                annotationService.batchProcessAndSave(anno, 1000);
                                long costMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                                long costSeconds = TimeUnit.MILLISECONDS.toSeconds(costMillis);
                                log.info("单次开始批量保存时间 singleId:{},url:{},time:{} 秒", jsonTask.getSingleId(), jsonFileS.getFileUrl(), costSeconds);
                                elementsList = new ArrayList<>();
                            }
                        }
                    }

                } else {
                    jsonParser.skipChildren();
                }
            }

            log.info("parseJson --------------> Json文件解析 while loop end:{} {} {}", System.currentTimeMillis(), jsonFileS.getFileUrl(), jsonTask);

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

//            Annotation annotation = new Annotation();
//            annotation.setMagnification(40000L);
//            annotation.setFiligreeContour(true);
//            annotation.setSingleSlideId(jsonTask.getSingleId());
            //精细
//            Annotation annotationIsValid = annotationMapper.collectGeometryStIsValid(jsonTask.getSingleId());
            //粗轮廓
            Long singSlideId = jsonTask.getSingleId();
            Annotation annoQuery = new Annotation();
            annoQuery.setSingleSlideId(singSlideId);
            annoQuery.setTagId(jsonTask.getCategoryId());
            Annotation annotationIsValid = annotationMapper.getCollectGeometryStIsValid(annoQuery);
            // 校验合并后是否合规
            if (null != annotationIsValid) {
                if (StringUtils.isNotEmpty(annotationIsValid.getResults())) {
                    if (ObjectUtil.equals(annotationIsValid.getResults(), "t")) {
                        // 合并轮廓
//                        Annotation annotation3 = annotationMapper.collectGeometry(singSlideId);
                        Annotation annotation3 = annotationMapper.getCollectGeometryIsValid(annoQuery);
                        annotation3.setContour(annotation3.getCollectContour());
                        // 查询有效精细轮廓列表
                        annotation3.setSequenceNumber(sequenceNumber);
                        annotation3.setSingleSlideId(singSlideId);
                        annotation3.setInsideOrOutside(false);
                        //预先查询下是否有需要删除的数据，主要是为了验证一些结构丢失问题
                        /**
                         List<Annotation> delAnnoList = annotationMapper.getDelAnnotation(annotation3);
                         if(CollectionUtils.isNotEmpty(delAnnoList)) {
                         Map<Long,Long>  annoCategoryIdMap = delAnnoList.stream().collect(Collectors.toMap(Annotation::getAnnotationId, Annotation::getCategoryId));
                         Map<Long,String> annoStructureMap = new HashMap<>();
                         Set<String> structureIdSet = new HashSet<>();
                         for (Map.Entry<Long, Long> entry : annoCategoryIdMap.entrySet()) {
                         Long annotationId = entry.getKey();
                         Long categoryId = entry.getValue();
                         if(pathologicalMap.containsValue(categoryId)) {
                         String structureId = findKeyByValue(pathologicalMap, categoryId);
                         if(StringUtils.isNotEmpty(structureId)) {
                         annoStructureMap.put(annotationId, structureId);
                         structureIdSet.add(structureId);
                         }
                         }
                         }

                         //汇总下总共处理的结构标签id
                         log.info("jsonTask id:[{}] singleSlide id:[{}] slideId id:[{}],结构指标待去除数据是：[{}],总条数是:[{}条],所有的结构标签id：[{}]",
                         jsonTask.getTaskId(),
                         jsonTask.getSingleId(),
                         jsonTask.getSlideId(),
                         ObjectUtil.isNotEmpty(annoStructureMap) ? JSONUtil.toJsonStr(annoStructureMap) : "",
                         delAnnoList.size(),
                         ObjectUtil.isNotEmpty(structureIdSet) ? structureIdSet.toString() : ""
                         );
                         }
                         */
//                        Integer delTotal = annotationMapper.deleteAiAnnotation(annotation3);
//                        if(null != delTotal) {
//                            log.info("jsonTask id:[{}] singleSlide id:[{}] slideId id:[{}],精细轮廓和结构指标去除无效数据，删除的数据总条数是：[{}]", jsonTask.getTaskId(), jsonTask.getSingleId(), jsonTask.getSlideId(),delTotal);
//                        }
                    } else {
                        log.error("jsonTask id:[{}] singleSlide id:[{}] slideId id:[{}],不合规", jsonTask.getTaskId(), jsonTask.getSingleId(), jsonTask.getSlideId());
                    }
                }
            } else {
                log.error("jsonTask id:[{}] singleSlide id:[{}] slideId id:[{}],无法查询到精细轮廓", jsonTask.getTaskId(), jsonTask.getSingleId(), jsonTask.getSlideId());
            }
            // 删除甲状旁腺内所有数据
            // 查询甲状旁腺精细轮廓进行合并
            /**
             *
             if (Objects.equals(jsonTask.getAlgorithmCode(), "Thyroid_gland")) {
             Annotation annotation1 = new Annotation();
             annotation1.setMagnification(40000L);
             annotation1.setFiligreeContour(true);
             annotation1.setSingleSlideId(singSlideId);
             LambdaQueryWrapper<OrganTag> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
             categoryLambdaQueryWrapper.eq(OrganTag::getOrganEn, "Parathyroid").eq(OrganTag::getSpeciesId, 1);
             OrganTag organTag = organTagMapper.selectOne(categoryLambdaQueryWrapper);
             annotation1.setCategoryId(organTag.getOrganTagId());
             Annotation annotation4 = annotationMapper.stUnionContourArea(annotation1);
             annotation4.setContour(annotation4.getCollectContour());
             Annotation annotation2 = annotationMapper.stIsValid(annotation4);
             if (null != annotation2) {
             if (StringUtils.isNotEmpty(annotation2.getResults())) {
             if (ObjectUtil.equals(annotation2.getResults(), "t")) {
             annotation4.setSequenceNumber(sequenceNumber);
             annotation4.setSingleSlideId(jsonTask.getSingleId());
             annotation4.setInsideOrOutside(true);
             annotationMapper.deleteAiAnnotation(annotation4);
             }
             }
             }
             }
             */
            log.info("parseJson --------------> Json文件解析结束:{} {} {}", System.currentTimeMillis(), jsonFileS.getFileUrl(), jsonTask);
        } catch (Exception e) {
            log.error("Unexpected error occurred: " + e.getMessage(), e);
        }

    }

    /**
     * @param @param  map
     * @param @param  value
     * @param @return
     * @return String
     * @throws
     * @Title: findKeyByValue
     * @Description: 通过value获取key值
     */
    public static String findKeyByValue(Map<String, Long> map, Long value) {
        return map.entrySet().stream().filter(e -> Objects.equals(e.getValue(), value)).map(Map.Entry::getKey).findFirst().orElse(null);
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
        QueryWrapper<OrganTag> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("organization_id", jsonTask.getOrganizationId());
        wrapper1.eq("del_flag", 0);
        wrapper1.eq("organ_tag_id", jsonTask.getCategoryId());
        OrganTag category = organTagMapper.selectOne(wrapper1);
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
            LambdaQueryWrapper<StructureTag> categoryQueryWrapper = new LambdaQueryWrapper<>();
            categoryQueryWrapper.eq(StructureTag::getDelFlag, 0).eq(StructureTag::getOrganizationId, organizationId);
            List<StructureTag> list = structureTagMapper.selectList(categoryQueryWrapper);
            pathlogicalMap = list.stream().collect(Collectors.toMap(StructureTag::getStructureId, StructureTag::getStructureTagId, (entity1, entity2) -> entity1));
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
        // 使用线程池并行处理
        List<CompletableFuture<Annotation>> futures = new ArrayList<>();
        for (Annotation item : annotationList1) {
            CompletableFuture<Annotation> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return processSingleAnnotation(jsonTask, structureIds, annotation, item, sequenceNumber);
                } catch (Exception e) {
                    log.error("处理注解数据失败, annotationId: {}", item.getAnnotationId(), e);
                    return null;
                }
            }, dynamicDataThreadPool);
            futures.add(future);
        }
        // 等待所有任务完成并收集结果
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        try {
            allFutures.join();
        } catch (Exception e) {
            log.error("并行处理注解数据失败", e);
        }

        // 收集处理结果并批量更新
        List<Annotation> processedAnnotations = futures.stream().map(CompletableFuture::join).filter(Objects::nonNull).collect(Collectors.toList());

        // 批量更新数据库
        batchUpdateAnnotations(processedAnnotations);
    }

    private Annotation processSingleAnnotation(JsonTask jsonTask, String structureIds, Annotation inputAnnotation, Annotation item, Long sequenceNumber) {
        Annotation annotationBy = getContourInsideOrOutside(jsonTask, item.getContour(), structureIds, true);
        if (annotationBy == null) {
            return null;
        }

        // 处理动态数据
        List<String> list = new ArrayList<>();
        JSONArray jsonArray = new JSONArray();

        if (item.getDynamicDataList() != null) {
            JSONObject jsonObject = JSONObject.parseObject(item.getDynamicDataList().toString());
            if (jsonObject.getJSONArray("dynamicData") != null) {
                jsonArray = jsonObject.getJSONArray("dynamicData");
                for (int j = 0; j < jsonArray.size(); j++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(j);
                    list.add(jsonObject1.getString("name"));
                }
            }
        }

        if (inputAnnotation.getAreaName() != null && annotationBy.getStructureAreaNum() != null) {
            DynamicData dynamicData = buildDynamicData(inputAnnotation.getAreaName(), formatDecimal(annotationBy.getStructureAreaNum()), inputAnnotation.getAreaUnit());
            jsonArray = updateDynamicDataList(list, jsonArray, dynamicData);
            list = addList(list, inputAnnotation.getAreaName());
        }

        if (inputAnnotation.getPerimeterName() != null && annotationBy.getStructurePerimeterNum() != null) {
            DynamicData dynamicData = buildDynamicData(inputAnnotation.getPerimeterName(), formatDecimal(annotationBy.getStructurePerimeterNum()), inputAnnotation.getPerimeterUnit());
            jsonArray = updateDynamicDataList(list, jsonArray, dynamicData);
            list = addList(list, inputAnnotation.getPerimeterName());
        }

        if (inputAnnotation.getCountName() != null) {
            DynamicData dynamicData = buildDynamicData(inputAnnotation.getCountName(), String.valueOf(annotationBy.getCount()), inputAnnotation.getCountUnit());
            jsonArray = updateDynamicDataList(list, jsonArray, dynamicData);
        }
        if (jsonArray.size() > 0) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("dynamicData", jsonArray);
            item.setSequenceNumber(sequenceNumber);
            item.setDynamicData(jsonObject.toString());
            return item;
        }

        return null;
    }

    private DynamicData buildDynamicData(String name, String data, String unit) {
        DynamicData dynamicData = new DynamicData();
        dynamicData.setName(name);
        dynamicData.setData(data);
        dynamicData.setUnit(unit);
        return dynamicData;
    }

    private String formatDecimal(BigDecimal value) {
        return value.setScale(3, RoundingMode.HALF_UP).toString();
    }

    /**
     * 单个组织结构动态数据
     *
     * @param jsonTask
     * @param structureId
     * @param annotation
     * @param type
     */
    public void putSingleAnnotationDynamicData(JsonTask jsonTask, String structureId, Annotation annotation, Integer type) {
        Long sequenceNumber = getSequenceNumber(jsonTask.getSpecialId());
        List<Annotation> annotationList = getStructureContourList(jsonTask, structureId);

        if (CollectionUtil.isEmpty(annotationList)) {
            return;
        }

        // 批量处理数据
        List<Annotation> batchUpdates = new ArrayList<>(annotationList.size());

        // 预处理通用数据
        boolean hasAreaName = annotation.getAreaName() != null;
        boolean hasPerimeterName = annotation.getPerimeterName() != null;

        String areaUnit = annotation.getAreaUnit();
        String perimeterUnit = annotation.getPerimeterUnit();

        for (Annotation item : annotationList) {
            // 预处理动态数据
            JSONObject dynamicDataJson = getOrCreateDynamicDataJson(item);
            JSONArray jsonArray = dynamicDataJson.getJSONArray("dynamicData");
            Set<String> existingNames = extractExistingNames(jsonArray);

            // 处理面积数据
            if (hasAreaName) {
                DynamicData dynamicData = new DynamicData();
                dynamicData.setName(annotation.getAreaName());

                switch (type) {
                    case 1:
                        dynamicData.setData(convertToSquareMicrometer(item.getStructureAreaNum().toString()));
                        break;
                    case 2:
                        dynamicData.setData(convertToMicrometer(item.getStructureAreaNum().toString()));
                        break;
                    case 3:
                    default:
                        dynamicData.setData(item.getStructureAreaNum().setScale(3, RoundingMode.HALF_UP).toString());
                        break;
                }
                dynamicData.setUnit(areaUnit);
                jsonArray = updateDynamicDataList(existingNames, jsonArray, dynamicData);
            }

            // 处理周长数据
            if (hasPerimeterName) {
                DynamicData dynamicData = new DynamicData();
                dynamicData.setName(annotation.getPerimeterName());

                switch (type) {
                    case 1:
                        dynamicData.setData(convertToSquareMicrometer(item.getStructurePerimeterNum().toString()));
                        break;
                    case 2:
                        dynamicData.setData(convertToMicrometer(item.getStructurePerimeterNum().toString()));
                        break;
                    case 3:
                    default:
                        dynamicData.setData(item.getStructurePerimeterNum().setScale(3, RoundingMode.HALF_UP).toString());
                        break;
                }
                dynamicData.setUnit(perimeterUnit);
                jsonArray = updateDynamicDataList(existingNames, jsonArray, dynamicData);
            }

            // 更新注解对象
            if (jsonArray != null && jsonArray.size() > 0) {
                JSONObject resultJson = new JSONObject();
                resultJson.put("dynamicData", jsonArray);
                item.setSequenceNumber(sequenceNumber);
                item.setDynamicData(resultJson.toString());
                item.setSingleSlideId(jsonTask.getSingleId());
                batchUpdates.add(item);
            }
        }

        // 批量更新数据库
        if (!batchUpdates.isEmpty()) {
            batchUpdateAnnotations(batchUpdates);
        }
    }

    /**
     * 单个腔体组织结构动数据
     *
     * @param jsonTask
     * @param structureId
     * @param structureIds
     * @param annotation
     * @param type         1：面积转10（3）平方微米  2:平方微米
     */
    public void putAnnotationDynamicData(JsonTask jsonTask, String structureId, String structureIds, Annotation annotation, Integer type,Boolean isInside) {
        Long sequenceNumber = getSequenceNumber(jsonTask.getSpecialId());
        List<Annotation> annotationList = getStructureContourList(jsonTask, structureId);

        if (CollectionUtil.isEmpty(annotationList)) {
            return;
        }

        // 批量处理数据
        List<Annotation> batchUpdates = new ArrayList<>(annotationList.size());

        // 预处理通用数据
        boolean hasAreaName = annotation.getAreaName() != null;
        boolean hasPerimeterName = annotation.getPerimeterName() != null;
        boolean hasCountName = annotation.getCountName() != null;

        String areaUnit = annotation.getAreaUnit();
        String perimeterUnit = annotation.getPerimeterUnit();
        String countUnit = annotation.getCountUnit();

        for (Annotation item : annotationList) {
            Annotation annotationBy = getContourInsideOrOutside(jsonTask, item.getContour(), structureIds, isInside);

            if (annotationBy == null) {
                continue;
            }

            // 预处理动态数据
            JSONObject dynamicDataJson = getOrCreateDynamicDataJson(item);
            JSONArray jsonArray = dynamicDataJson.getJSONArray("dynamicData");
            Set<String> existingNames = extractExistingNames(jsonArray);

            // 处理面积数据
            if (hasAreaName) {
                DynamicData dynamicData = new DynamicData();
                dynamicData.setName(annotation.getAreaName());

                switch (type) {
                    case 1:
                        dynamicData.setData(convertToSquareMicrometer(String.valueOf(annotationBy.getStructureAreaNum())));
                        break;
                    case 2:
                        dynamicData.setData(String.valueOf(convertToMicrometer(annotationBy.getStructureAreaNum().setScale(3, RoundingMode.HALF_UP).toString())));
                        break;
                    default:
                        dynamicData.setData(String.valueOf(annotationBy.getStructureAreaNum().setScale(3, RoundingMode.HALF_UP)));
                        break;
                }
                dynamicData.setUnit(areaUnit);
                jsonArray = updateDynamicDataList(existingNames, jsonArray, dynamicData);
            }

            // 处理周长数据
            if (hasPerimeterName) {
                DynamicData dynamicData = new DynamicData();
                dynamicData.setName(annotation.getPerimeterName());
                dynamicData.setData(String.valueOf(annotationBy.getStructurePerimeterNum()));
                dynamicData.setUnit(perimeterUnit);
                jsonArray = updateDynamicDataList(existingNames, jsonArray, dynamicData);
            }

            // 处理计数数据
            if (hasCountName) {
                DynamicData dynamicData = new DynamicData();
                dynamicData.setName(annotation.getCountName());
                dynamicData.setData(String.valueOf(annotationBy.getCount()));
                dynamicData.setUnit(countUnit);
                jsonArray = updateDynamicDataList(existingNames, jsonArray, dynamicData);
            }

            // 更新注解对象
            if (jsonArray.size() > 0) {
                JSONObject resultJson = new JSONObject();
                resultJson.put("dynamicData", jsonArray);
                item.setSequenceNumber(sequenceNumber);
                item.setDynamicData(resultJson.toString());
                item.setSingleSlideId(jsonTask.getSingleId());
                batchUpdates.add(item);
            }
        }

        // 批量更新数据库
        if (!batchUpdates.isEmpty()) {
            batchUpdateAnnotations(batchUpdates);
        }
    }

    // 提取现有名称到Set中以提高查找效率
    private Set<String> extractExistingNames(JSONArray jsonArray) {
        Set<String> names = new HashSet<>();
        if (jsonArray != null) {
            for (int j = 0; j < jsonArray.size(); j++) {
                JSONObject jsonObject = jsonArray.getJSONObject(j);
                names.add(jsonObject.getString("name"));
            }
        }
        return names;
    }

    // 获取或创建动态数据JSON对象
    private JSONObject getOrCreateDynamicDataJson(Annotation annotation) {
        if (annotation.getDynamicDataList() != null) {
            return JSONObject.parseObject(annotation.getDynamicDataList().toString());
        }
        return new JSONObject();
    }

    // 优化的更新方法
    private JSONArray updateDynamicDataList(Set<String> existingNames, JSONArray jsonArray, DynamicData dynamicData) {
        if (jsonArray == null) {
            jsonArray = new JSONArray();
        }
        if (existingNames.contains(dynamicData.getName())) {
            // 更新现有数据
            for (int j = 0; j < jsonArray.size(); j++) {
                JSONObject jsonObject = jsonArray.getJSONObject(j);
                if (Objects.equals(jsonObject.getString("name"), dynamicData.getName())) {
                    jsonObject.put("data", dynamicData.getData());
                    break;
                }
            }
        } else {
            // 添加新数据
            jsonArray.add(dynamicData);
        }
        return jsonArray;
    }

    // 批量更新方法
    public void batchUpdateAnnotations(List<Annotation> annotations) {
        // 如果annotationMapper支持批量更新，使用批量操作
        // 否则可以分批执行，减少数据库连接开销
        final int batchSize = 100;
        for (int i = 0; i < annotations.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, annotations.size());
            List<Annotation> batch = annotations.subList(i, endIndex);
            // 执行批量更新逻辑
            for (Annotation annotation : batch) {
                annotationMapper.aiUpdateById(annotation);
            }
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
        return bigDecimal1.divide(bigDecimal2, 9, RoundingMode.HALF_UP);
    }


    public BigDecimal sqrt(BigDecimal number) {
        // 将BigDecimal转换为double进行开方
        double doubleValue = number.doubleValue();
        double sqrtValue = Math.sqrt(doubleValue);
        // 根据所需的精度，将double结果转换回BigDecimal
        MathContext mc = new MathContext(3, RoundingMode.HALF_UP);
        return new BigDecimal(sqrtValue, mc);
    }

    /**
     * 比值计算（保留三位小数）
     *
     * @param bigDecimal1
     * @param bigDecimal2
     */
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

    public void batchDeleteBySingleSlideId(JsonTask jsonTask) {
        Long sequenceNumber = getSequenceNumber(jsonTask.getSpecialId());

        // 脏器轮廓信息
        Annotation annotation = new Annotation();
        annotation.setSequenceNumber(sequenceNumber);
        //单脏器切片id
        annotation.setSingleSlideId(jsonTask.getSingleId());
        int batchSize = 1000;
        while (true) {
            int deletedCount = annotationMapper.deleteBySingleSlideIdBatch(annotation, batchSize);
            if (deletedCount < batchSize) {
                break;
            }
        }
    }
}
