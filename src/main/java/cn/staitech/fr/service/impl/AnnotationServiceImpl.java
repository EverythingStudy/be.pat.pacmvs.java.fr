package cn.staitech.fr.service.impl;

import cn.staitech.common.core.domain.R;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.netty.websocket.NioWebSocketHandler;
import cn.staitech.fr.service.RocksdbService;
import cn.staitech.fr.utils.*;
import cn.staitech.fr.vo.annotation.Features;
import cn.staitech.fr.vo.annotation.PropertiesBriefly;
import cn.staitech.fr.vo.annotation.in.*;
import cn.staitech.fr.vo.annotation.out.AnnotationDistanceOut;
import cn.staitech.fr.vo.annotation.out.BroadcastVO;
import cn.staitech.fr.vo.history.HistoryDTO;
import cn.staitech.fr.vo.history.Session;
import cn.staitech.fr.vo.history.Trace;
import cn.staitech.fr.vo.history.TraceNode;
import cn.staitech.system.api.domain.SysUser;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.staitech.fr.service.AnnotationService;
import com.google.gson.Gson;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.overlay.OverlayOp;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.staitech.fr.constant.CommonConstant.*;
import static org.apache.lucene.document.DateTools.stringToDate;

/**
 * @author admin
 * @description 针对表【fr_contour】的数据库操作Service实现
 * @createDate 2024-09-10 09:31:06
 */
@Slf4j
@Service
public class AnnotationServiceImpl extends ServiceImpl<AnnotationMapper, Annotation> implements AnnotationService {


    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

    private static final WKTReader WKT_READER = new WKTReader(GEOMETRY_FACTORY);
    ExpiringMap<Long, PathologicalIndicatorCategory> pathologicalIndicatorCategoryHashMap = ExpiringMap.builder().maxSize(1000).expiration(12, TimeUnit.HOURS).variableExpiration().expirationPolicy(ExpirationPolicy.CREATED).build();
    ExpiringMap<Long, User> userMap = ExpiringMap.builder().maxSize(1000).expiration(12, TimeUnit.HOURS).variableExpiration().expirationPolicy(ExpirationPolicy.CREATED).build();

    @Resource
    private MeasureMapper measureMapper;

    @Resource
    private SlideMapper slideMapper;

    @Resource
    private ImageMapper imageMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private AnnotationDelMapper annotationDelMapper;

    @Resource
    private RocksdbService rocksdbService;

    @Resource
    private PathologicalIndicatorCategoryMapper pathologicalIndicatorCategoryMapper;

    @Resource
    private SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private AnnotationMapper annotationMapper;


    public List<Features> getFeaturesList(List<Annotation> annotations) {
        List<Features> featuresList = new ArrayList<>();
        for (Annotation annotation : annotations) {
            Features features = new Features();
            features.setGeometry(JSONObject.parseObject(annotation.getContour()));
            features.setId(null);
            features.setType("Feature");
            String s1 = JSONObject.toJSONString(getProperties(annotation), SerializerFeature.PrettyFormat);
            JSONObject jsonObject = JSONObject.parseObject(s1);
            features.setProperties(jsonObject);
            featuresList.add(features);
        }
        return featuresList;
    }

    @Override
    public List<Features> selectListBy(AnnotationSelectList req) throws Exception {
        List<Features> list = new ArrayList<>();
        Annotation annotation = new Annotation();
        annotation.setSlideId(req.getSlideId());
        annotation.setSequenceNumber(getSequenceNumber(req.getSlideId()));
        // 查询普通轮廓
        List<Annotation> annotationList = annotationMapper.selectListBy(annotation);
        List<Features> annoList = getFeaturesList(annotationList);
        if (CollectionUtils.isNotEmpty(annoList)) {
            list.addAll(annoList);
        }
        return list;
    }


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


    public  Long getSequenceNumber(Long slideId) {
        Slide slide = slideMapper.selectById(slideId);
        SpecialAnnotationRel specialAnnotationRel = specialAnnotationRelMapper.selectById(slide.getSpecialId());
        return specialAnnotationRel.getSequenceNumber();
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
        Annotation annotation = new Annotation();
        if (req.getCategory_id() != null) {
            PathologicalIndicatorCategory pathologicalIndicatorCategory = pathologicalIndicatorCategoryMapper.selectById(req.getCategory_id());
            if (pathologicalIndicatorCategory != null) {
                jsonId = MarkingUtils.getSdId(pathologicalIndicatorCategory.getCategoryName());
            } else {
                jsonId = MarkingUtils.getSdId();
            }
        } else {
            jsonId = MarkingUtils.getSdId();
        }
        annotation.setSlideId(req.getSlide_id());
        annotation.setArea(req.getArea());
        annotation.setCategoryId(req.getCategory_id());
        annotation.setContour(String.valueOf(req.getGeometry()));
        annotation.setLocationType(req.getLocation_type());
        annotation.setAnnotationType("Draw");
        annotation.setPerimeter(req.getPerimeter());
        annotation.setCreateBy(req.getCreate_by());
        annotation.setJsonId(jsonId);
        Long seq = getSequenceNumber(req.getSlide_id());
        annotation.setSequenceNumber(seq);
        annotationMapper.insert(annotation);
        Annotation annotationBy = annotationMapper.selectByIds(annotation);
        PropertiesBriefly properties = getProperties(annotationBy);
        Features features = socketData(annotation.getJsonId(), JSONObject.parseObject(annotationBy.getContour()), properties);
        BroadcastVO broadcastVO = sendOneMessages(ADD_STATUS, features);
        {
            Long slideId = req.getSlide_id();
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
        NioWebSocketHandler.sendAll(req.getSlide_id(), broadcastVO);
        return annotation.getAnnotationId();
    }


    @Override
    public R<String> roiContDel(RoiIn req) throws Exception {

        req.getCategoryIds().add(null);
        req.setCreateBy(SecurityUtils.getUserId());
        req.setSequenceNumber(getSequenceNumber(req.getSlideId()));
        List<Annotation> features = annotationMapper.selectListRoiContDel(req);
        List<Long> annotationIdList;
        //roi包含
        if (req.getRoiStatus() == 0) {
            annotationIdList = roiCont(req, features);
        } else {
            annotationIdList = roiDel(req, features);
        }
        if (annotationIdList.isEmpty()) {
            return R.ok(null, MessageSource.M("OPERATE_SUCCEED"));
        }
        //异步删除
        CompletableFuture<Integer> cf1 = CompletableFuture.supplyAsync(() -> {
            if (CollectionUtils.isNotEmpty(annotationIdList)) {
                req.setAnnotationIdList(annotationIdList);
                annotationMapper.deleteRoiContDel(req);
                //标注的createBy和categoryId
                for (Long markingId : annotationIdList) {
                    Annotation annotation = new Annotation();
                    annotation.setAnnotationId(markingId);
                    annotation.setSequenceNumber(req.getSequenceNumber());
                    Annotation annotation1 = annotationMapper.selectByIds(annotation);
                    annotation1.setSequenceNumber(req.getSequenceNumber());
                    try {
                        annotationDelInsert(annotation1, req.getSequenceNumber());
                    } catch (java.text.ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return 1;
        });

        BroadcastVO broadcastVO = sendListMessages(CommonConstant.ANNO_TYPE_DRAW, RELOAD_STATUS, null);
        NioWebSocketHandler.sendAll(req.getSlideId(), broadcastVO);
        return R.ok(null, MessageSource.M("OPERATE_SUCCEED"));
    }


    public List<Long> roiCont(RoiIn viewAddIns, List<Annotation> features) throws ParseException {
        //要删除的markingId集合
        Set<Long> markingIdDel = new HashSet<>();
        //包含的markingId集合
        Set<Long> markingIdCont = new HashSet<>();
        for (JSONObject viewAddIn : viewAddIns.getGeometryList()) {
            String roiLocation = WktUtil.jsonToWkt(viewAddIn);
            Geometry roiLocations = WKT_READER.read(roiLocation);
            //roi包含
            if (viewAddIns.getRoiStatus() == 0) {
                for (Annotation features1 : features) {
                    String oldLocation = WktUtil.jsonToWkt(JSONObject.parseObject(features1.getContour()));
                    Geometry oldLocations = WKT_READER.read(oldLocation);
                    //判断是否不包含和不相交
                    if (!roiLocations.contains(oldLocations) && !roiLocations.intersects(oldLocations)) {
                        markingIdDel.add(features1.getAnnotationId());
                    } else {
                        //有相交的部分
                        markingIdCont.add(features1.getAnnotationId());
                    }
                }
            }
        }
        //选出要删除的markingId
        List<Long> listIds = markingIdDel.stream().filter(item -> !markingIdCont.contains(item)).collect(Collectors.toList());
        return listIds;

    }

    /**
     * ROI删除选出要删除的markingId
     */
    public List<Long> roiDel(RoiIn viewAddIns, List<Annotation> features) throws ParseException {
        //要删除的markingId集合
        Set<Long> markingIdDel = new HashSet<>();
        //包含的markingId集合
        for (JSONObject viewAddIn : viewAddIns.getGeometryList()) {
            String roiLocation = WktUtil.jsonToWkt(viewAddIn);
            Geometry roiLocations = WKT_READER.read(roiLocation);
            //roi删除
            if (viewAddIns.getRoiStatus() == 1) {
                for (Annotation features1 : features) {
                    String oldLocation = WktUtil.jsonToWkt(JSONObject.parseObject(features1.getContour()));
                    Geometry oldLocations = WKT_READER.read(oldLocation);
                    //判断是否包含和相交
                    if (roiLocations.contains(oldLocations) || roiLocations.intersects(oldLocations)) {
                        markingIdDel.add(features1.getAnnotationId());
                    }
                }
            }
        }
        return new ArrayList<>(markingIdDel);
    }


    @Override
    public int delete(AnnotationById req) throws Exception {
        Long seq = getSequenceNumber(req.getSlide_id());
        if (!Optional.ofNullable(req.getMarking_id()).isPresent()) {
            throw new Exception(MessageSource.M("ARGUMENT_INVALID"));
        }
        Annotation annotation = new Annotation();
        annotation.setAnnotationId(req.getMarking_id());
        annotation.setSequenceNumber(seq);
        Annotation annotationBy = annotationMapper.selectByIds(annotation);
        if (!Optional.ofNullable(annotationBy).isPresent()) {
            throw new Exception(MessageSource.M("NO_ANNOTATION_DATA"));
        }
        PropertiesBriefly properties = getProperties(annotationBy);
        Features features = socketData(annotationBy.getJsonId(), JSONObject.parseObject(annotationBy.getContour()), properties);
        BroadcastVO broadcastVO = sendOneMessages(DELETE_STATUS, features);


        NioWebSocketHandler.sendAll(annotationBy.getSlideId(), broadcastVO);

        int res = annotationMapper.deleteByIds(annotation);

        // 添加删除记录表中
        annotationDelInsert(annotationBy, seq);

        String traceId = req.getTraceId();
        boolean isBatch = req.getIsBatch();
        {
            Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
            // 删除操作RocksDB存删除前的数据
            // 撤消,恢复历史记录 用HistoryService会引起循环依赖！ -> 后续在线程池中处理 判断是批处理，还是单独处理
            // 1、创建Session,并存入ConcurrentHashMap<Long, Session>
            Long slideId;

            slideId = annotationBy.getSlideId();

            Session session = HistoryServiceImpl.refreshSession(userId, slideId);

            // 2、创建Trace,并存入Session.list,LinkedList<Trace>
            if (isBatch) {
                // 批量操作：若trace已经存在，不用再add
                Trace trace = session.getTraceById(traceId);
                trace.getNodeList().add(new TraceNode(String.valueOf(annotationBy.getAnnotationId()), "DELETE"));
            } else {
                // 单条记录
                Trace trace = new Trace(userId, traceId, false);
                trace.getNodeList().add(new TraceNode(String.valueOf(annotationBy.getAnnotationId()), "DELETE"));
                session.drawListAdd(trace);
            }

//            // 3、数据持久化写入RocksDB
//            Gson gson = new Gson();
//            // 将对象转换成JSON字符串
//            String json = gson.toJson(markingBy);
//            RocksDBUtil.put(traceId, markingId, json);
            rocksdbService.submitTask(traceId, String.valueOf(annotationBy.getAnnotationId()), annotationBy);
        }
        return res;
    }

    public void annotationDelInsert(Annotation annotationBy, Long seq) throws java.text.ParseException {
        AnnotationDel annotationDel = new AnnotationDel();
        BeanUtils.copyProperties(annotationBy, annotationDel);
        if (annotationBy.getCreateTime() != null) {
            annotationDel.setCreateTime(stringToDate(annotationBy.getCreateTime()));
        }
        if (annotationBy.getUpdateTime() != null) {
            annotationDel.setUpdateTime(stringToDate(annotationBy.getUpdateTime()));
        }
        annotationDel.setDeleteBy(SecurityUtils.getLoginUser().getSysUser().getUserId());
        annotationDel.setDeleteTime(new Date());
        annotationDel.setSequenceNumber(seq);
        annotationDelMapper.insert(annotationDel);
    }


    @Override
    public JSONObject updateOperation(UpdateOperationIn req, String traceId, Boolean isBatch) throws Exception {
        Long seq = getSequenceNumber(req.getSlide_id());
        Annotation annotations = new Annotation();
        annotations.setAnnotationId(req.getMarking_id());
        annotations.setSequenceNumber(seq);
        Annotation annotation = annotationMapper.selectByIds(annotations);
        // 查询数据是否存在
        if (!Optional.ofNullable(annotation).isPresent()) {
            throw new Exception(MessageSource.M("NO_ANNOTATION_DATA"));
        }
        boolean res = MarkingUtils.updateVerify(JSONObject.parseObject(annotation.getContour()), req.getGeometry(), req.getOperation(), req.getCheck(), req.getResolution());
        if (!res) {
            throw new Exception(MessageSource.M("GRAPHICS_MARK_NOT_RULES"));
        }
        Annotation annotationBys = new Annotation();
        annotationBys.setAnnotationId(req.getMarking_id());
        annotationBys.setContour(String.valueOf(req.getGeometry()));
        annotationBys.setUpdateBy(req.getUpdate_by());
        annotationBys.setOperation(req.getOperation());
        annotationBys.setSequenceNumber(seq);
        Annotation annotation1 = annotationMapper.mergeContour(annotationBys);
        String contourType = annotationMapper.selectContourType(annotation1).getLocationType();
        if (!Objects.equals(contourType, "POLYGON")) {
            throw new Exception(MessageSource.M("GRAPHICS_MARK_NOT_RULES"));
        }
        annotationBys.setContour(annotation1.getContour());
        Slide slide = slideMapper.selectById(annotation.getSlideId());
        if (!Optional.ofNullable(slide).isPresent()) {
            throw new Exception(MessageSource.M("NO_SLIDE_DATA"));
        }
        Image image = imageMapper.selectById(slide.getImageId());
        if (!Optional.ofNullable(image).isPresent()) {
            throw new Exception(MessageSource.M("NODATA"));
        }
        Annotation annotationArea = annotationMapper.getArea(annotationBys);
        if (image.getResolutionX() != null) {
            double resolutions = Double.parseDouble(image.getResolutionX());
            String area = String.valueOf(Double.parseDouble(annotationArea.getArea()) * resolutions * resolutions);
            annotationBys.setArea(AreaUtils.formattedNumber(area));
            String per = String.valueOf(Double.parseDouble(annotationArea.getPerimeter()) * resolutions);
            annotationBys.setPerimeter(AreaUtils.formattedNumber(per));
        }
        annotationMapper.updateByIds(annotationBys);
        // 更新后查询数据并返回
        Annotation annotationById = annotationMapper.selectByIds(annotations);
        PropertiesBriefly properties = getProperties(annotationById);
        Features features = socketData(annotationBys.getJsonId(), JSONObject.parseObject(annotation1.getContour()), properties);
        BroadcastVO broadcastVO = sendOneMessages(UPDATE_STATUS, features);

        NioWebSocketHandler.sendAll(annotation.getSlideId(), broadcastVO);
        {
            Long userId = req.getUpdate_by();
            Long slideId = annotation.getSlideId();

            String annotationId = String.valueOf(annotation.getAnnotationId());

            // 删除操作RocksDB存删除前的数据
            // 撤消,恢复历史记录 用HistoryService会引起循环依赖！ -> 后续在线程池中处理 判断是批处理，还是单独处理
            // 1、创建Session,并存入ConcurrentHashMap<Long, Session>
            Session session = new Session(userId, slideId);
            String key = userId + "_" + slideId;
            if (!HistoryServiceImpl.USER_SESSION_MAP.containsKey(key)) {
                HistoryServiceImpl.USER_SESSION_MAP.put(key, session);
            }
            session = HistoryServiceImpl.USER_SESSION_MAP.get(key);

            // 2、创建Trace,并存入Session.list,LinkedList<Trace>
            // 单条记录
            Trace trace = new Trace(userId, traceId, isBatch);
            // 批量操作

            if (isBatch && session.getTraceById(traceId) != null) {
                // 若trace已经存在，不用再add
                trace = session.getTraceById(traceId);
                trace.getNodeList().add(new TraceNode(annotationId, "UPDATEOPERATION"));
            } else {
                trace.getNodeList().add(new TraceNode(annotationId, "UPDATEOPERATION"));
                session.drawListAdd(trace);
            }
            rocksdbService.submitTask(traceId, annotationId, annotation);
        }
        return JSONObject.parseObject(annotation1.getContour());
    }


    @Override
    public List<BatchResult> batch(ViewAddInList list) throws Exception {
        String traceId = UUID.randomUUID().toString();

        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        Long userId = sysUser.getUserId();
        Long slideId = list.getList().get(0).getSlide_id();

        Session session = HistoryServiceImpl.refreshSession(userId, slideId);
        // 2、创建Trace,并存入Session.list,LinkedList<Trace>
        // 单条记录
        Trace trace = new Trace(userId, traceId, true);
        session.drawListAdd(trace);

        List<BatchResult> result = new ArrayList<>(list.getList().size());
        Long seq = getSequenceNumber(list.getSlide_id());
        for (ViewAddIn dto : list.getList()) {
            BatchResult batchResult = new BatchResult();
            batchResult.setFront_id(dto.getMarking_id());
//            dto.setSequenceNumber(seq);
            dto.setUpdate_by(userId);
            dto.setSlide_id(slideId);
            dto.setTraceId(traceId);
            dto.setIsBatch(true);
            try {
                switch (dto.getOperation()) {
                    case "INSERT":
                        Long markingIdIns = insert(dto);
                        if (markingIdIns != null) {
                            batchResult.setData(String.valueOf(markingIdIns));
                            break;
                        }
                    case "DELETE":
                        AnnotationById annotationById = new AnnotationById();
                        annotationById.setIsBatch(true);
                        annotationById.setSlide_id(slideId);
                        annotationById.setTraceId(traceId);
                        annotationById.setMarking_id(Long.valueOf(dto.getMarking_id()));
                        if (delete(annotationById) > 0) {
                            batchResult.setData(dto.getMarking_id());
                            break;
                        }
                    case "UPDATE":
                        boolean res = Objects.equals(dto.getOperation(), "UPDATE");
                        Long markingId = update(dto);
                        if (markingId != null) {
                            batchResult.setData(String.valueOf(markingId));
                            break;
                        }
                    default:
                }

                batchResult.setStatus(true);
                batchResult.setMessage(MessageSource.M("OPERATE_SUCCEED"));
            } catch (Exception e) {
                batchResult.setData(dto.getMarking_id());
                batchResult.setMessage(e.getMessage());
                batchResult.setStatus(false);
            }
            result.add(batchResult);
        }
        return result;
    }


    @Override
    public Long update(ViewAddIn req) throws Exception {
        Long seq = getSequenceNumber(req.getSlide_id());
        Annotation annotations = new Annotation();
        annotations.setAnnotationId(Long.valueOf(req.getMarking_id()));
        annotations.setSequenceNumber(seq);
        Annotation annotationBy = annotationMapper.selectByIds(annotations);

        if (!Optional.ofNullable(annotationBy).isPresent()) {
            throw new Exception(MessageSource.M("NO_ANNOTATION_DATA"));
        }
        String traceId = req.getTraceId();
        boolean isBatch = req.getIsBatch();
        Long userId = req.getUpdate_by();
        {

            Long slideId = annotationBy.getSlideId();

            // 删除操作RocksDB存删除前的数据
            // 撤消,恢复历史记录 用HistoryService会引起循环依赖！ -> 后续在线程池中处理 判断是批处理，还是单独处理
            // 1、创建Session,并存入ConcurrentHashMap<Long, Session>
            Session session = HistoryServiceImpl.refreshSession(userId, slideId);

            // 2、创建Trace,并存入Session.list,LinkedList<Trace>
            if (isBatch) {
                // 批量操作：若trace已经存在，不用再add
                Trace trace = session.getTraceById(traceId);
                trace.getNodeList().add(new TraceNode(req.getMarking_id(), "UPDATE"));
            } else {
                // 单条记录
                Trace trace = new Trace(userId, traceId, false);
                trace.getNodeList().add(new TraceNode(req.getMarking_id(), "UPDATE"));
                session.drawListAdd(trace);
            }
            rocksdbService.submitTask(traceId, req.getMarking_id(), annotationBy);
        }
        Annotation annotation = new Annotation();
        if (req.getCategory_id() == null && req.getDescription() == null) {
            if (req.getGeometry() != null) {
                if (req.getGeometry().isEmpty()) {
                    throw new Exception("更新失败，轮廓数据不能为空");
                }
            } else {
                throw new Exception("修改标注数据异常，更新失败");
            }
        }
        if (req.getCategory_id() != null) {
            if (!req.getCategory_id().equals(annotation.getCategoryId())) {
                annotation.setCategoryId(req.getCategory_id());
            }
        }
        String id = null;
        if (req.getDescription() != null) {
            annotation.setDescription(req.getDescription());
        }
        if (req.getUpdate_by() != null) {
            annotation.setUpdateBy(req.getUpdate_by());
        } else {
            annotation.setUpdateBy(SecurityUtils.getLoginUser().getSysUser().getUserId());
        }
        annotation.setJsonId(id);
        annotation.setSlideId(req.getSlide_id());
        annotation.setAnnotationId(Long.valueOf(req.getMarking_id()));
        if (req.getGeometry() != null) {
            annotation.setContour(String.valueOf(req.getGeometry()));
            Geometry geometry = WKT_READER.read(MarkingUtils.jsonToWkt(JSONObject.parseObject(annotation.getContour())));
            Slide slide = slideMapper.selectById(annotationBy.getSlideId());
            if (!Optional.ofNullable(slide).isPresent()) {
                throw new Exception(MessageSource.M("NO_SLIDE_DATA"));
            }
            Image image = imageMapper.selectById(slide.getImageId());
            if (!Optional.ofNullable(image).isPresent()) {
                throw new Exception(MessageSource.M("NODATA"));
            }
            if (image.getResolutionX() != null) {
                double resolutions = Double.parseDouble(image.getResolutionX());
                String area = String.valueOf(geometry.getArea() * resolutions * resolutions);
                annotation.setArea(AreaUtils.formattedNumber(area));
                String per = String.valueOf(geometry.getLength() * resolutions);
                annotation.setPerimeter(AreaUtils.formattedNumber(per));
            }
        }
        annotation.setSequenceNumber(seq);
        annotationMapper.updateByIds(annotation);
        Annotation annotationBys = annotationMapper.selectByIds(annotation);
        PropertiesBriefly properties = getProperties(annotationBys);
        Features features = socketData(annotation.getJsonId(), req.getGeometry(), properties);
        BroadcastVO broadcastVO = sendOneMessages(UPDATE_STATUS, features);
        NioWebSocketHandler.sendAll(req.getSlide_id(), broadcastVO);
        return annotation.getAnnotationId();
    }


    @Override
    public int padding(AnnotationById req) throws Exception {
        Long seq = getSequenceNumber(req.getSlide_id());
        if (!Optional.ofNullable(req.getMarking_id()).isPresent()) {
            throw new Exception(MessageSource.M("ARGUMENT_INVALID"));
        }
        Annotation annotations = new Annotation();
        annotations.setAnnotationId(req.getMarking_id());
        annotations.setSequenceNumber(seq);
        Annotation annotationBy = annotationMapper.selectByIds(annotations);
        if (!Optional.ofNullable(annotationBy).isPresent()) {
            throw new Exception(MessageSource.M("NO_ANNOTATION_DATA"));
        }
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        Long loginUserId = sysUser.getUserId();
        {
            Long slideId = annotationBy.getSlideId();
            String traceId = cn.staitech.common.core.utils.uuid.UUID.fastUUID().toString();
            // 删除操作RocksDB存删除前的数据
            // 撤消,恢复历史记录 用HistoryService会引起循环依赖！ -> 后续在线程池中处理 判断是批处理，还是单独处理
            // 1、创建Session,并存入ConcurrentHashMap<Long, Session>
            Session session = new Session(loginUserId, slideId);
            String key = loginUserId + "_" + slideId;
            if (!HistoryServiceImpl.USER_SESSION_MAP.containsKey(key)) {
                HistoryServiceImpl.USER_SESSION_MAP.put(key, session);
            }
            session = HistoryServiceImpl.USER_SESSION_MAP.get(key);

            // 2、创建Trace,并存入Session.list,LinkedList<Trace> - 单条记录
            Trace trace = new Trace(loginUserId, traceId, false);
            trace.getNodeList().add(new TraceNode(String.valueOf(annotationBy.getAnnotationId()), "UPDATEOPERATION"));
            session.drawListAdd(trace);

//            // 3、数据持久化写入RocksDB
//            Gson gson = new Gson();
//            // 将对象转换成JSON字符串
//            String json = gson.toJson(markingBy);
//            RocksDBUtil.put(traceId, markingId, json);
            rocksdbService.submitTask(traceId, String.valueOf(annotationBy.getAnnotationId()), annotationBy);
        }

        JSONObject geometryJson = MarkingUtils.padding(JSONObject.parseObject(annotationBy.getContour()));
        Annotation annotation = new Annotation();
        annotation.setAnnotationId(req.getMarking_id());
        annotation.setContour(String.valueOf(geometryJson));
        Geometry geometry = WKT_READER.read(MarkingUtils.jsonToWkt(JSONObject.parseObject(annotation.getContour())));
        Slide slide = slideMapper.selectById(annotationBy.getSlideId());
        if (!Optional.ofNullable(slide).isPresent()) {
            throw new Exception(MessageSource.M("NO_SLIDE_DATA"));
        }
        Image image = imageMapper.selectById(slide.getImageId());
        if (!Optional.ofNullable(image).isPresent()) {
            throw new Exception(MessageSource.M("NODATA"));
        }
        if (image.getResolutionX() != null) {
            double resolutions = Double.parseDouble(image.getResolutionX());
            String area = String.valueOf(geometry.getArea() * resolutions * resolutions);
            annotation.setArea(AreaUtils.formattedNumber(area));
            String per = String.valueOf(geometry.getLength() * resolutions);
            annotation.setPerimeter(AreaUtils.formattedNumber(per));
        }
        annotation.setSequenceNumber(seq);
        annotation.setUpdateBy(SecurityUtils.getUserId());
        int res = annotationMapper.updateByIds(annotation);
        Annotation annotationBys = annotationMapper.selectByIds(annotations);
        PropertiesBriefly properties = getProperties(annotationBys);
        Features features = socketData(annotationBys.getJsonId(), geometryJson, properties);
        BroadcastVO broadcastVO = sendOneMessages(UPDATE_STATUS, features);
        NioWebSocketHandler.sendAll(annotationBy.getSlideId(), broadcastVO);
        return res;
    }


    @Override
    public int stickup(AnnotationById req) throws Exception {
        if (!Optional.ofNullable(req.getMarking_id()).isPresent()) {
            throw new Exception(MessageSource.M("ARGUMENT_INVALID"));
        }
        Long seq = getSequenceNumber(req.getSlide_id());
        Annotation annotations = new Annotation();
        annotations.setAnnotationId(req.getMarking_id());
        annotations.setSequenceNumber(seq);
        Annotation annotation = annotationMapper.selectByIds(annotations);
        annotation.setSequenceNumber(seq);
        int res = annotationMapper.insert(annotation);
        Annotation annotationBys = annotationMapper.selectByIds(annotation);
        PropertiesBriefly properties = getProperties(annotationBys);
        Features features = socketData(annotationBys.getJsonId(), JSONObject.parseObject(annotation.getContour()), properties);
        BroadcastVO broadcastVO = sendOneMessages(ADD_STATUS, features);
        NioWebSocketHandler.sendAll(annotation.getSlideId(), broadcastVO);
        return res;
    }


    @Override
    public JSONObject markingMerge(MarkingMerge req) throws Exception {
        Long seq = getSequenceNumber(req.getSlideId());
        req.setSequenceNumber(seq);
        List<Annotation> annotationList = annotationMapper.selectInList(req);
        List<Geometry> geometryList = new ArrayList<>();
        for (Annotation annotation : annotationList) {
            Geometry geometry = WKT_READER.read(MarkingUtils.jsonToWkt(JSONObject.parseObject(annotation.getContour())));
            geometryList.add(geometry);
        }
        if (geometryList.size() > 0) {
            if (geometryList.size() == 1) {
                return JSONObject.parseObject(annotationList.get(0).getContour());
            }
            Geometry geometry = null;
            for (int i = 0; i < geometryList.size(); i++) {
                if (i == 0) {
                    geometry = geometryList.get(i);
                }
                Geometry geometryIntersection = geometry.intersection(geometryList.get(i));
                if (geometryIntersection.isEmpty()) {
                    return null;
                } else {
                    OverlayOp op = new OverlayOp(geometry, geometryList.get(i));
                    int code = OverlayOp.UNION;
                    geometry = op.getResultGeometry(code);
                }
            }
            return JSONObject.parseObject(WktUtil.wktToJson(String.valueOf(geometry)));
        } else {
            return null;
        }
    }


    @Override
    public Boolean redo(HistoryDTO dto) {
        Long seq = getSequenceNumber(dto.getSlideId());
        String traceId = UUID.randomUUID().toString();
        // Boolean isUndo = dto.getEnvType() == 1 ? true : false;

        Long userId = dto.getUserId();
        Long slideId = dto.getSlideId();


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
                        annotation.setSequenceNumber(seq);
                        Long beforeMarkingId = annotation.getAnnotationId();

                        Annotation newAnnotation = new Annotation();

                        switch (node.getOperation()) {
                            case "INSERT":
                                newAnnotation = insertByHistory(annotation);
                                refresh(drawList, String.valueOf(beforeMarkingId), annotation.getAnnotationId());
                                refresh(undoList, String.valueOf(beforeMarkingId), annotation.getAnnotationId());
                                break;
                            case "DELETE":
                                newAnnotation = deleteByHistory(Long.valueOf(markingId), seq);
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
                    annotation.setSequenceNumber(seq);
                    Annotation newAnnotation = new Annotation();

                    switch (node.getOperation()) {
                        case "INSERT":
                            newAnnotation = insertByHistory(annotation);
                            refresh(drawList, beforeMarkingId, newAnnotation.getAnnotationId());
                            refresh(undoList, beforeMarkingId, newAnnotation.getAnnotationId());
                            break;
                        case "DELETE":
                            newAnnotation = deleteByHistory(Long.valueOf(markingId), seq);
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
        Long seq = getSequenceNumber(dto.getSlideId());
        String traceId = UUID.randomUUID().toString();
        //Boolean isUndo = dto.getEnvType() == 1 ? true : false;
        Long userId = dto.getUserId();
        Long slideId = dto.getSlideId();

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
                        annotation.setSequenceNumber(seq);
                        Annotation newAnnotation = new Annotation();
                        switch (node.getOperation()) {
                            case "INSERT":
                                newAnnotation = deleteByHistory(Long.valueOf(markingId), seq);
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
                    annotation.setSequenceNumber(seq);
                    Long beforeMarkingId = annotation.getAnnotationId();
                    Annotation newAnnotation = new Annotation();

                    switch (node.getOperation()) {
                        case "INSERT":
                            newAnnotation = deleteByHistory(Long.valueOf(markingId), seq);
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
    public Annotation insertByHistory(Annotation annotation) {
        Long slideId = annotation.getSlideId();

        //加slide缓存
        Slide slideBy = slideMapper.selectById(slideId);
        if (slideBy == null) {
            return null;
        }
        annotationMapper.insert(annotation);
        Annotation annotationById = annotationMapper.selectByIds(annotation);
        PropertiesBriefly properties = getProperties(annotationById);
        Features features = socketData(String.valueOf(annotation.getAnnotationId()), JSONObject.parseObject(annotation.getContour()), properties);
        BroadcastVO broadcastVO = sendListMessages(CommonConstant.ANNO_TYPE_DRAW, ADD_STATUS, features);
        NioWebSocketHandler.sendAll(annotationById.getSlideId(), broadcastVO);
        return annotationById;
    }

    public static BroadcastVO sendOneMessagesByAnnoType(String annoType, String status, Features features) {
        BroadcastVO broadcast = new BroadcastVO();
        broadcast.setData(features);
        broadcast.setType(status);
        broadcast.setAnnotation_type(annoType);
        return broadcast;
    }


    @Override
    public Annotation updateOperationByHistory(Annotation req) {
        Annotation annotationBy = annotationMapper.selectByIds(req);
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
        annotation.setSequenceNumber(req.getSequenceNumber());
        annotationMapper.updateByIds(annotation);
        PropertiesBriefly properties = getProperties(annotationBy);
        Features features = socketData(String.valueOf(annotationBy.getAnnotationId()), JSONObject.parseObject(req.getContour()), properties);
        BroadcastVO broadcastVO = sendOneMessagesByAnnoType(CommonConstant.ANNO_TYPE_DRAW, UPDATE_STATUS, features);

        NioWebSocketHandler.sendAll(annotationBy.getSlideId(), broadcastVO);

        return annotationBy;
    }

    @Override
    public Annotation deleteByHistory(Long annotationId, Long seq) throws java.text.ParseException {
        Annotation annotation = new Annotation();
        annotation.setAnnotationId(annotationId);
        annotation.setSequenceNumber(seq);
        Annotation annotationById = annotationMapper.selectByIds(annotation);
        PropertiesBriefly properties = getProperties(annotationById);
        Features features = socketData(String.valueOf(annotationId), JSONObject.parseObject(annotationById.getContour()), properties);
        BroadcastVO broadcastVO = sendListMessages(CommonConstant.ANNO_TYPE_DRAW, DELETE_STATUS, features);
        annotationById.setSequenceNumber(seq);
        annotationMapper.deleteByIds(annotationById);
        // 添加删除记录表中
        annotationDelInsert(annotationById, seq);
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

    @Override
    public boolean getCountByCategory(CategoryStatisticsIn req) {
        boolean existence = false;
        //查询符合条件的所有的专题
        List<SpecialAnnotationRel> sAnnoRelList = specialAnnotationRelMapper.getSeqNumberList();
        if (CollectionUtils.isNotEmpty(sAnnoRelList)) {
            List<Long> sequenceNumberList = sAnnoRelList.stream().map(SpecialAnnotationRel::getSequenceNumber).collect(Collectors.toList());
            for (Long seqNumber : sequenceNumberList) {
                Annotation annotation = new Annotation();
                annotation.setCategoryId(req.getCategoryId());
                annotation.setSequenceNumber(seqNumber);
                int categoryCount = annotationMapper.getCountByCategory(annotation);
                if (categoryCount > 0) {
                    existence = true;
                    break;
                }
            }
        }
        return existence;
    }


}




