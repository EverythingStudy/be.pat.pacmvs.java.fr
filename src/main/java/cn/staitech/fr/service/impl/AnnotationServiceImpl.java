package cn.staitech.fr.service.impl;

import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.netty.websocket.NioWebSocketHandler;
import cn.staitech.fr.service.RocksdbService;
import cn.staitech.fr.utils.MarkingUtils;
import cn.staitech.fr.utils.MessageSource;
import cn.staitech.fr.utils.RocksDBUtil;
import cn.staitech.fr.vo.annotation.Features;
import cn.staitech.fr.vo.annotation.PropertiesBriefly;
import cn.staitech.fr.vo.annotation.in.DistanceGet;
import cn.staitech.fr.vo.annotation.in.ViewAddIn;
import cn.staitech.fr.vo.annotation.out.AnnotationDistanceOut;
import cn.staitech.fr.vo.annotation.out.BroadcastVO;
import cn.staitech.fr.vo.history.HistoryDTO;
import cn.staitech.fr.vo.history.Session;
import cn.staitech.fr.vo.history.Trace;
import cn.staitech.fr.vo.history.TraceNode;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.staitech.fr.service.AnnotationService;
import com.google.gson.Gson;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTReader;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static cn.staitech.fr.constant.CommonConstant.*;

/**
 * @author admin
 * @description 针对表【fr_contour】的数据库操作Service实现
 * @createDate 2024-09-10 09:31:06
 */
@Slf4j
@Service
public class AnnotationServiceImpl extends ServiceImpl<AnnotationMapper, Annotation>
        implements AnnotationService {


    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

    private static final WKTReader WKT_READER = new WKTReader(GEOMETRY_FACTORY);
    ExpiringMap<Long, PathologicalIndicatorCategory> pathologicalIndicatorCategoryHashMap = ExpiringMap.builder().maxSize(1000).expiration(12, TimeUnit.HOURS).variableExpiration().expirationPolicy(ExpirationPolicy.CREATED).build();
    ExpiringMap<Long, User> userMap = ExpiringMap.builder().maxSize(1000).expiration(12, TimeUnit.HOURS).variableExpiration().expirationPolicy(ExpirationPolicy.CREATED).build();

    @Resource
    private MeasureMapper measureMapper;

    @Resource
    private SlideMapper slideMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RocksdbService rocksdbService;

    @Resource
    private PathologicalIndicatorCategoryMapper pathologicalIndicatorCategoryMapper;

    @Resource
    private SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private AnnotationMapper annotationMapper;


//    public List<Features> getFeaturesList(List<Annotation> annotations) {
//        List<Features> featuresList = new ArrayList<>();
//        for (Annotation annotation : annotations) {
//            Features features = new Features();
//            features.setGeometry(JSONObject.parseObject(annotation.getContour()));
//            features.setId(null);
//            features.setType("Feature");
//            String s1 = JSONObject.toJSONString(getProperties(annotation), SerializerFeature.PrettyFormat);
//            JSONObject jsonObject = JSONObject.parseObject(s1);
//            features.setProperties(jsonObject);
//            featuresList.add(features);
//        }
//        return featuresList;
//    }

    public PropertiesBriefly getProperties(Annotation annotation) {
        PropertiesBriefly properties = new PropertiesBriefly();
        properties.setA0(String.valueOf(annotation.getAnnotationId()));
        properties.setA1(annotation.getLocationType());
        properties.setA2(annotation.getAnnotationType());
        properties.setA3(annotation.getCategoryId());
        properties.setA6(annotation.getPerimeter());
        properties.setA7(annotation.getArea());
        properties.setA8(annotation.getDescription());
        properties.setA11(annotation.getCreateBy());
        properties.setA12(String.valueOf(annotation.getCreateTime()));
        properties.setA13(annotation.getUpdateBy());
        properties.setA29(annotation.getCellType());
        properties.setA30(annotation.getDynamicDataList());

        if (annotation.getCategoryId() != null) {
            PathologicalIndicatorCategory pathologicalIndicatorCategory = pathologicalIndicatorCategoryHashMap.get(annotation.getCategoryId());
            if (pathologicalIndicatorCategory == null) {
                pathologicalIndicatorCategory = pathologicalIndicatorCategoryMapper.selectById(annotation.getCategoryId());
                if (pathologicalIndicatorCategory != null) {
                    pathologicalIndicatorCategoryHashMap.put(pathologicalIndicatorCategory.getCategoryId(), pathologicalIndicatorCategory);
                }
            }
            if (pathologicalIndicatorCategory != null) {
                properties.setA4(pathologicalIndicatorCategory.getRgb());
                properties.setA5(pathologicalIndicatorCategory.getNumber());
            }
        }

        if (annotation.getCreateBy() != null) {
            User createUser = userMap.get(annotation.getCreateBy());
            if (createUser == null) {
                createUser = userMapper.selectById(annotation.getCreateBy());
                if (createUser != null) {
                    userMap.put(annotation.getCreateBy(), createUser);
                } else {
                    userMap.put(annotation.getCreateBy(), new User());
                }
            }
            if (createUser != null && createUser.getUserId() != null) {
                properties.setA9(createUser.getUserName());
            }
        }
        if (annotation.getUpdateBy() != null) {
            User updateUser = userMap.get(annotation.getUpdateBy());
            if (updateUser == null) {
                updateUser = userMapper.selectById(annotation.getUpdateBy());
                if (updateUser != null) {
                    userMap.put(annotation.getUpdateBy(), updateUser);
                } else {
                    userMap.put(annotation.getUpdateBy(), new User());
                }
            }
            if (updateUser != null && updateUser.getUserId() != null) {
                properties.setA10(updateUser.getUserName());
            }
        }
        return properties;
    }

    public static Features socketData(String annotationId, JSONObject geometry, PropertiesBriefly properties) {
        Features features = new Features();
        features.setGeometry(geometry);
        features.setId(annotationId);
        features.setType("Feature");
        String s1 = JSONObject.toJSONString(properties, SerializerFeature.PrettyFormat);
        JSONObject jsonObject = JSONObject.parseObject(s1);
        features.setProperties(jsonObject);
        return features;
    }

    public static BroadcastVO sendOneMessages(String status, Features features) {
        BroadcastVO broadcast = new BroadcastVO();
        broadcast.setData(features);
        broadcast.setType(status);
        broadcast.setAnnotation_type("Draw");
        return broadcast;
    }


    @Override
    public Long insert(ViewAddIn req) throws Exception {
        if (req.getSlide_id() == null) {
            throw new Exception(MessageSource.M("MarkingDelIn.slideId.notNull"));
        }
        if (req.getGeometry() != null && !req.getGeometry().isEmpty()) {
            MarkingUtils.addVerify(req.getGeometry());
        } else {
            throw new Exception(MessageSource.M("ARGUMENT_INVALID"));
        }
        Slide slide = slideMapper.selectById(req.getSlide_id());
        if (!Optional.ofNullable(slide).isPresent()) {
            throw new Exception(MessageSource.M("NO_SLIDE_DATA"));
        }
        String jsonId = null;
        Category category = null;
        Annotation annotation = new Annotation();
        if (req.getCategory_id() != null) {
            PathologicalIndicatorCategory pathologicalIndicatorCategory = pathologicalIndicatorCategoryMapper.selectById(req.getCategory_id());
            if (pathologicalIndicatorCategory != null) {
                jsonId = MarkingUtils.getSdId(pathologicalIndicatorCategory.getCategoryName());
            }
        } else {
            jsonId = MarkingUtils.getSdId(null);
        }
        annotation.setSlideId(req.getSlide_id());
        annotation.setArea(req.getArea());
        annotation.setCategoryId(req.getCategory_id());
        annotation.setContour(String.valueOf(req.getGeometry()));
        annotation.setLocationType(req.getLocation_type());
        annotation.setPerimeter(req.getPerimeter());
        annotation.setCreateBy(req.getCreate_by());
        annotation.setJsonId(jsonId);
        annotation.setAnnotationType("Draw");
        annotationMapper.insert(annotation);
        Annotation annotationBy = annotationMapper.selectById(annotation);
        PropertiesBriefly properties = getProperties(annotationBy);
        Features features = socketData(annotation.getJsonId(), JSONObject.parseObject(annotationBy.getContour()), properties);
        BroadcastVO broadcastVO = sendOneMessages(ADD_STATUS, features);

        NioWebSocketHandler.sendAll(req.getSlide_id(), broadcastVO);


//            Image image = imageMapper.selectById(slide.getImageId());
//            if (!Optional.ofNullable(image).isPresent()) {
//                throw new Exception(MessageSource.M("NODATA"));
//            }
//            Special special = specialService.getById(slide.getSpecialId());
//            List<JSONObject> contourList = selectContourList(annotation.getSlideId(), req.getCategory_id());
//            asyncTask.generateThumbnail(annotation.getSlideId(), req.getCategory_id(), image.getImageUrl(), contourList, 1, category.getCategoryAbbreviation(), special.getSpecialName());
//            AlgorithmAnnIn algorithmAnnIn = new AlgorithmAnnIn();
//            algorithmAnnIn.setSlideId(slide.getSlideId());
//            algorithmAnnIn.setOrganizationId(SecurityUtils.getLoginUser().getSysUser().getOrganizationId());
//            algorithmPredictionService.recognition(algorithmAnnIn);
        // 切图完成后更新切片状态
//        if (slide.getProcessFlag() != 2) {
//            slide.setProcessFlag(2);
//            slideMapper.updateById(slide);
//        }
        {
            Long slideId;
            if (req.getSingle_slide_id() != null) {
                slideId = req.getSingle_slide_id();
            } else {
                slideId = req.getSlide_id();
            }
            String traceId = req.getTraceId();
            Long userId = req.getCreate_by();
            Session session = HistoryServiceImpl.refreshSession(userId, slideId);
            if (req.getIsBatch()) {
                Trace trace = session.getTraceById(traceId);
                trace.getNodeList().add(new TraceNode(String.valueOf(annotation.getAnnotationId()), "INSERT"));
            } else {
                Trace trace = new Trace(userId, traceId, false);
                trace.getNodeList().add(new TraceNode(String.valueOf(annotation.getAnnotationId()), "INSERT"));
                session.drawListAdd(trace);
            }
            rocksdbService.submitTask(traceId, String.valueOf(annotation.getAnnotationId()), annotation);
        }
        return annotation.getAnnotationId();
    }


    @Override
    public Boolean redo(HistoryDTO dto) {
        String traceId = UUID.randomUUID().toString();
        // Boolean isUndo = dto.getEnvType() == 1 ? true : false;

        Long userId = dto.getUserId();
        Long slideId;
        if (dto.getSingleSlideId() != null) {
            slideId = dto.getSingleSlideId();
        } else {
            slideId = dto.getSlideId();
        }

        String key = userId + "_" + slideId;
        Session session = HistoryServiceImpl.USER_SESSION_MAP.get(key);
        LinkedList<Trace> drawList = session.getDrawList();
        LinkedList<Trace> undoList = session.getUndoList();

        if (!undoList.isEmpty()) {
            Trace trace = undoList.get(undoList.size() - 1);
            Boolean isBatch = trace.getIsBatch();
            List<TraceNode> traceNodeList = trace.getNodeList();

            if (isBatch) {
                Trace newTrace = new Trace(userId, traceId, true);

                for (int i = traceNodeList.size() - 1; i >= 0; i--) {
                    try {
                        TraceNode node = traceNodeList.get(i);
                        String markingId = node.getId();

                        Gson gson = new Gson();
                        String json = RocksDBUtil.get(trace.getTraceId(), markingId);
                        Annotation annotation = gson.fromJson(json, Annotation.class);
                        Long beforeMarkingId = annotation.getAnnotationId();

                        Annotation newAnnotation = new Annotation();

                        switch (node.getOperation()) {
                            case "INSERT":
                                newAnnotation = insertByHistory(annotation);
                                refresh(drawList, String.valueOf(beforeMarkingId), annotation.getAnnotationId());
                                refresh(undoList, String.valueOf(beforeMarkingId), annotation.getAnnotationId());
                                break;
                            case "DELETE":
                                newAnnotation = deleteByHistory(Long.valueOf(markingId));
                                break;
                            case "UPDATE":
                            case "UPDATEOPERATION":
                                newAnnotation = updateOperationByHistory(annotation);
                                break;
                            default:
                        }

                        newTrace.getNodeList().add(new TraceNode(String.valueOf(newAnnotation.getAnnotationId()), node.getOperation()));
//                        json = gson.toJson(newMarking);
//                        RocksDBUtil.put(traceId, newMarking.getMarking_id(), json);
                        rocksdbService.submitTask(traceId, String.valueOf(newAnnotation.getAnnotationId()), newAnnotation);
                    } catch (Exception e) {
                        log.info("redo：{}", e);
                    }
                }

                newTrace.setTraceId(traceId);
                drawList.add(newTrace);
                if (undoList.size() > 0) {
                    undoList.remove(undoList.size() - 1);
                }
            } else {

                try {
                    TraceNode node = traceNodeList.get(0);
                    String markingId = node.getId();

                    Gson gson = new Gson();
                    String json = RocksDBUtil.get(trace.getTraceId(), markingId);
                    Annotation annotation = gson.fromJson(json, Annotation.class);
                    String beforeMarkingId = String.valueOf(annotation.getAnnotationId());

                    Annotation newAnnotation = new Annotation();

                    switch (node.getOperation()) {
                        case "INSERT":
                            newAnnotation = insertByHistory(annotation);
                            refresh(drawList, beforeMarkingId, newAnnotation.getAnnotationId());
                            refresh(undoList, beforeMarkingId, newAnnotation.getAnnotationId());
                            break;
                        case "DELETE":
                            newAnnotation = deleteByHistory(Long.valueOf(markingId));
                            break;
                        case "UPDATE":
                        case "UPDATEOPERATION":
                            newAnnotation = updateOperationByHistory(annotation);
                            break;
                        default:
                    }

                    trace.setTraceId(traceId);
                    drawList.add(trace);
//                    json = gson.toJson(newMarking);
//                    RocksDBUtil.put(traceId, newMarking.getMarking_id(), json);
                    rocksdbService.submitTask(traceId, String.valueOf(newAnnotation.getAnnotationId()), newAnnotation);
                    if (undoList.size() > 0) {
                        undoList.remove(undoList.size() - 1);
                    }
                } catch (Exception e) {
                    log.info("redo：{}", e);
                }
            }
        }
        return true;
    }

    @Override
    public Boolean undo(HistoryDTO dto) {
        String traceId = UUID.randomUUID().toString();
        //Boolean isUndo = dto.getEnvType() == 1 ? true : false;
        Long userId = dto.getUserId();
        Long slideId;
        if (dto.getSingleSlideId() != null) {
            slideId = dto.getSingleSlideId();
        } else {
            slideId = dto.getSlideId();
        }
        String key = userId + "_" + slideId;
        Session session = HistoryServiceImpl.USER_SESSION_MAP.get(key);
        LinkedList<Trace> drawList = session.getDrawList();
        LinkedList<Trace> undoList = session.getUndoList();

        if (!drawList.isEmpty()) {
            Trace trace = drawList.get(drawList.size() - 1);
            Boolean isBatch = trace.getIsBatch();
            List<TraceNode> traceNodeList = trace.getNodeList();

            if (isBatch) {
                Trace newTrace = new Trace(userId, traceId, true);
                for (int i = traceNodeList.size() - 1; i >= 0; i--) {
                    TraceNode node = traceNodeList.get(i);
                    String markingId = node.getId();
                    try {
                        Gson gson = new Gson();
                        String json = RocksDBUtil.get(trace.getTraceId(), markingId);
                        Annotation annotation = gson.fromJson(json, Annotation.class);
                        String beforeMarkingId = String.valueOf(annotation.getAnnotationId());

                        Annotation newAnnotation = new Annotation();

                        switch (node.getOperation()) {
                            case "INSERT":
                                newAnnotation = deleteByHistory(Long.valueOf(markingId));
                                break;
                            case "DELETE":
                                newAnnotation = insertByHistory(annotation);
                                refresh(drawList, beforeMarkingId, newAnnotation.getAnnotationId());
                                refresh(undoList, beforeMarkingId, newAnnotation.getAnnotationId());
                                break;
                            case "UPDATE":
                            case "UPDATEOPERATION":
                                newAnnotation = updateOperationByHistory(annotation);
                                break;
                            default:
                        }

                        for (TraceNode traceNode : trace.getNodeList()) {
                            if (traceNode.getId().equals(beforeMarkingId)) {
                                traceNode.setId(String.valueOf(newAnnotation.getAnnotationId()));
                            }
                        }

                        newTrace.getNodeList().add(new TraceNode(String.valueOf(newAnnotation.getAnnotationId()), node.getOperation()));
//                        json = gson.toJson(newMarking);
//                        RocksDBUtil.put(traceId, newMarking.getMarking_id(), json);
                        rocksdbService.submitTask(traceId, String.valueOf(newAnnotation.getAnnotationId()), newAnnotation);
                    } catch (Exception e) {
                    }
                }
                newTrace.setTraceId(traceId);
                session.undoListAdd(newTrace);
            } else {
                TraceNode node = traceNodeList.get(0);
                String markingId = node.getId();

                try {
                    Gson gson = new Gson();
                    String json = RocksDBUtil.get(trace.getTraceId(), markingId);
                    Annotation annotation = gson.fromJson(json, Annotation.class);
                    Long beforeMarkingId = annotation.getAnnotationId();
                    Annotation newAnnotation = new Annotation();

                    switch (node.getOperation()) {
                        case "INSERT":
                            newAnnotation = deleteByHistory(Long.valueOf(markingId));
                            break;
                        case "DELETE":
                            newAnnotation = insertByHistory(annotation);
                            refresh(drawList, String.valueOf(beforeMarkingId), newAnnotation.getAnnotationId());
                            refresh(undoList, String.valueOf(beforeMarkingId), newAnnotation.getAnnotationId());
                            break;
                        case "UPDATE":
                        case "UPDATEOPERATION":
                            newAnnotation = updateOperationByHistory(annotation);
                            break;
                        default:
                    }

                    for (TraceNode traceNode : trace.getNodeList()) {
                        if (traceNode.getId().equals(beforeMarkingId)) {
                            traceNode.setId(String.valueOf(newAnnotation.getAnnotationId()));
                        }
                    }

                    trace.setTraceId(traceId);
                    session.undoListAdd(trace);
//                    json = gson.toJson(newMarking);
//                    RocksDBUtil.put(traceId, newMarking.getMarking_id(), json);
                    rocksdbService.submitTask(traceId, String.valueOf(newAnnotation.getAnnotationId()), newAnnotation);
                } catch (Exception e) {
                    log.info("undo：{}", e);
                }

            }
            // Index: -1, Size: 0
            if (drawList.size() > 0) {
                drawList.remove(drawList.size() - 1);
            }
        }

        return true;
    }



    public void refresh(LinkedList<Trace> list, String oldId, Long newId) {
        if (!list.isEmpty()) {
            for (Trace trace : list) {
                List<TraceNode> traceNodeList = trace.getNodeList();
                for (TraceNode traceNode : traceNodeList) {
                    String oldNodeId = traceNode.getId();
                    try {
                        if (oldNodeId.equals(oldId.toString())) {
                            traceNode.setId(newId.toString());

                            Gson gson = new Gson();
                            String json = RocksDBUtil.get(trace.getTraceId(), oldNodeId);
                            Annotation annotation = gson.fromJson(json, Annotation.class);
                            annotation.setAnnotationId(newId);

//                            json = gson.toJson(marking);
//                            RocksDBUtil.put(trace.getTraceId(), newId, json);
                            rocksdbService.submitTask(trace.getTraceId(), trace.getTraceId(), annotation);
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }
    }

    public static BroadcastVO sendListMessages(String annoType, String status, Features features) {
        BroadcastVO broadcast = new BroadcastVO();
        broadcast.setData(features);
        broadcast.setType(status);
        broadcast.setAnnotation_type(annoType);
        return broadcast;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Annotation insertByHistory(Annotation annotation) {
        Long slideId = annotation.getSlideId();

        //加slide缓存
        Slide slideBy = slideMapper.selectById(slideId);
        if (slideBy == null) {
            return null;
        }
        annotationMapper.insert(annotation);
        Annotation annotationById = annotationMapper.selectById(annotation.getAnnotationId());
        PropertiesBriefly properties = getProperties(annotationById);
        Features features = socketData(String.valueOf(annotation.getAnnotationId()), JSONObject.parseObject(annotation.getContour()), properties);
        BroadcastVO broadcastVO = sendListMessages(CommonConstant.ANNO_TYPE_DRAW, ADD_STATUS, features);
        NioWebSocketHandler.sendAll(annotationById.getSlideId(), broadcastVO);
        return annotationById;
    }

    public static BroadcastVO sendOneMessagesByAnnoType(String annoType, String status,Features features) {
        BroadcastVO broadcast = new BroadcastVO();
        broadcast.setData(features);
        broadcast.setType(status);
        broadcast.setAnnotation_type(annoType);
        return broadcast;
    }


    @Override
    public Annotation updateOperationByHistory(Annotation req) {
        Annotation annotationBy = annotationMapper.selectById(req.getAnnotationId());
        if (!Optional.ofNullable(annotationBy).isPresent()) {
            return null;
        }
        Annotation annotation = new Annotation();
        BeanUtils.copyProperties(annotationBy, annotation);
        annotation.setAnnotationId(req.getAnnotationId());
        annotation.setContour(req.getContour());
        annotation.setArea(req.getArea());
        annotation.setPerimeter(req.getPerimeter());
        annotation.setUpdateBy(SecurityUtils.getUserId());
        annotation.setUpdateTime(new Date());
        annotationMapper.updateById(annotation);
        PropertiesBriefly properties = getProperties(annotationBy);
        Features features = socketData(String.valueOf(annotationBy.getAnnotationId()), JSONObject.parseObject(req.getContour()), properties);
        BroadcastVO broadcastVO = sendOneMessagesByAnnoType(CommonConstant.ANNO_TYPE_DRAW, UPDATE_STATUS, features);

            NioWebSocketHandler.sendAll(annotationBy.getSlideId(), broadcastVO);

        return annotationBy;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Annotation deleteByHistory(Long annotationId) {
        Annotation annotationById = annotationMapper.selectById(annotationId);
        PropertiesBriefly properties = getProperties(annotationById);
        Features features = socketData(String.valueOf(annotationId), JSONObject.parseObject(annotationById.getContour()), properties);
        BroadcastVO broadcastVO = sendListMessages(CommonConstant.ANNO_TYPE_DRAW, DELETE_STATUS, features);
        annotationMapper.deleteById(annotationById);

            NioWebSocketHandler.sendAll(annotationById.getSlideId(), broadcastVO);

        return annotationById;
    }


    @Override
    public AnnotationDistanceOut getDistance(DistanceGet req) {
        AnnotationDistanceOut annotationDistanceOut = new AnnotationDistanceOut();
        LambdaQueryWrapper<SpecialAnnotationRel> queryWrapper = new LambdaQueryWrapper<SpecialAnnotationRel>().eq(SpecialAnnotationRel::getSpecialId, req.getSpecialId());
        SpecialAnnotationRel specialAnnotationRel = specialAnnotationRelMapper.selectOne(queryWrapper);
        String contourOne = selectContour(req.getAnnotationIdOne(), req.getAnnotationTypeOne(), specialAnnotationRel.getSequenceNumber());
        String contourTwo = selectContour(req.getAnnotationIdTwo(), req.getAnnotationTypeTwo(), specialAnnotationRel.getSequenceNumber());
        Annotation annotation = new Annotation();
        annotation.setContourOne(contourOne);
        annotation.setContourTwo(contourTwo);
        // 计算两个图形之间最短距离的两个点
        Annotation annotationDistance = annotationMapper.stClosestPoint(annotation);
        annotationDistanceOut.setContourTypeOne(JSONObject.parseObject(annotationDistance.getContourOne()));
        annotationDistanceOut.setContourTypeTwo(JSONObject.parseObject(annotationDistance.getContourTwo()));
        // 计算两个图形之间的最短距离
        Annotation annotationMinDistance = annotationMapper.stDistance(annotation);
        Double minDistance = Double.parseDouble(String.format("%.3f", annotationMinDistance.getMinDistance()));
        annotationDistanceOut.setMinDistance(minDistance);
        // 计算两个图形之间的平均距离
        Annotation annotationAvgDistance = annotationMapper.avgDistance(annotation);
        Double meanDistance = Double.parseDouble(String.format("%.3f", annotationAvgDistance.getMeanDistance()));
        annotationDistanceOut.setMeanDistance(meanDistance);
        return annotationDistanceOut;
    }


    private String selectContour(Long annotationId, String annotationType, Long sequenceNumber) {
        String contour = null;
        if (Objects.equals(annotationType, "Draw")) {
            // 查询fr_ai_annotation表中信息
            Annotation annotation = new Annotation();
            annotation.setAnnotationId(annotationId);
            annotation.setSequenceNumber(sequenceNumber);
            contour = annotationMapper.selectByIds(annotation).getContour();
        } else if (Objects.equals(annotationType, "Measure")) {
            // 根据主键查询fr_measure表中信息
            contour = measureMapper.selectById(annotationId).getContour();
        }
        return contour;
    }


}




