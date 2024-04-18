package cn.staitech.fr.service.impl;

import cn.staitech.common.core.domain.R;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.config.AsyncTask;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.domain.history.HistoryDTO;
import cn.staitech.fr.domain.history.Session;
import cn.staitech.fr.domain.history.Trace;
import cn.staitech.fr.domain.history.TraceNode;
import cn.staitech.fr.domain.in.AlgorithmAnnIn;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.netty.websocket.NioWebSocketHandler;
import cn.staitech.fr.service.AlgorithmPredictionService;
import cn.staitech.fr.service.RocksdbService;
import cn.staitech.fr.service.SpecialService;
import cn.staitech.fr.utils.*;
import cn.staitech.fr.vo.annotation.AnnotationById;
import cn.staitech.fr.vo.annotation.AnnotationCountByCategory;
import cn.staitech.fr.vo.annotation.AnnotationSelectList;
import cn.staitech.fr.vo.annotation.MarkingMerge;
import cn.staitech.fr.vo.geojson.Features;
import cn.staitech.fr.vo.geojson.Properties;
import cn.staitech.fr.vo.geojson.in.RoiIn;
import cn.staitech.fr.vo.geojson.in.UpdateOperationIn;
import cn.staitech.fr.vo.geojson.in.ViewAddIn;
import cn.staitech.fr.vo.geojson.out.BatchResult;
import cn.staitech.fr.vo.measure.BroadcastVO;
import cn.staitech.system.api.domain.SysUser;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cn.staitech.fr.service.AnnotationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.overlay.OverlayOp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.staitech.fr.constant.CommonConstant.*;
import static cn.staitech.fr.utils.MarkingUtils.socketData;
import static cn.staitech.fr.utils.SendMessage.sendOneMessages;

/**
 * @author admin
 * @description 针对表【fr_annotation】的数据库操作Service实现
 * @createDate 2024-04-01 09:42:42
 */
@Service
@Slf4j
public class AnnotationServiceImpl extends ServiceImpl<AnnotationMapper, Annotation> implements AnnotationService {


    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

    private static final WKTReader WKT_READER = new WKTReader(GEOMETRY_FACTORY);
    HashMap<Long, Category> categoryHashMap = new HashMap<>();
    HashMap<Long, PathologicalIndicatorCategory> pathologicalIndicatorCategoryHashMap = new HashMap<>();
    HashMap<Long, User> userMap = new HashMap<>();
    @Resource
    private AnnotationMapper annotationMapper;
    @Resource
    private SlideMapper slideMapper;
    @Resource
    private ImageMapper imageMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private AsyncTask asyncTask;

    @Resource
    private RocksdbService rocksdbService;

    @Resource
    private SpecialService specialService;

    @Resource
    private AlgorithmPredictionService algorithmPredictionService;

    @Resource
    private PathologicalIndicatorCategoryMapper pathologicalIndicatorCategoryMapper;


    public List<Features> getFeaturesList(List<Annotation> annotations) {
        List<Features> featuresList = new ArrayList<>();
        for (Annotation annotation : annotations) {
            Features features = new Features();
            features.setGeometry(JSONObject.parseObject(annotation.getContour()));
            features.setId(null);
            features.setType("Feature");
            JSONObject jsonObject = (JSONObject) JSON.toJSON(getProperties(annotation));
            features.setProperties(jsonObject);
            featuresList.add(features);
        }
        return featuresList;
    }

    public cn.staitech.fr.vo.geojson.Properties getProperties(Annotation annotation) {
    	cn.staitech.fr.vo.geojson.Properties properties = new cn.staitech.fr.vo.geojson.Properties();
    	properties.setMarking_id(String.valueOf(annotation.getAnnotationId()));
    	properties.setArea(annotation.getArea());
    	properties.setPerimeter(annotation.getPerimeter());
    	properties.setAnnotation_type(annotation.getAnnotationType());
    	properties.setCategory_id(annotation.getCategoryId());
    	properties.setDescription(annotation.getDescription());
    	properties.setLocation_type(annotation.getLocationType());
    	properties.setCreate_by(annotation.getCreateBy());
    	properties.setUpdate_by(annotation.getUpdateBy());
    	properties.setCreate_time(String.valueOf(annotation.getCreateTime()));
    	properties.setProject_id(annotation.getProjectId());
    	properties.setContour_type(annotation.getContourType());
        properties.setSingleSlideId(annotation.getSingleSlideId());
        properties.setSingle(annotation.getSingle());
        if (annotation.getSingle() == 1) {
            if (annotation.getCategoryId() != null) {
                PathologicalIndicatorCategory pathologicalIndicatorCategory = pathologicalIndicatorCategoryHashMap.get(annotation.getCategoryId());
                if (pathologicalIndicatorCategory == null) {
                    pathologicalIndicatorCategory = pathologicalIndicatorCategoryMapper.selectById(annotation.getCategoryId());
                    if (pathologicalIndicatorCategory != null) {
                        pathologicalIndicatorCategoryHashMap.put(pathologicalIndicatorCategory.getCategoryId(), pathologicalIndicatorCategory);
                    }
                }
                if (pathologicalIndicatorCategory != null) {
                    properties.setLabel_color(pathologicalIndicatorCategory.getRgb());
                    properties.setLabel_name(pathologicalIndicatorCategory.getNumber());
                }
            }
        } else {
            if (annotation.getCategoryId() != null) {
                Category category = categoryHashMap.get(annotation.getCategoryId());
                if (category == null) {
                    category = categoryMapper.selectById(annotation.getCategoryId());
                    if (category != null) {
                        categoryHashMap.put(category.getCategoryId(), category);
                    }
                }
                if (category != null) {
                    properties.setLabel_color(category.getChromaticValue());
                    properties.setLabel_name(category.getOrganName());
                }
            }
        }
        if (annotation.getCreateBy() != null) {
            User createUser = userMap.get(annotation.getCreateBy());
            if (createUser == null) {
                createUser = userMapper.selectById(annotation.getCreateBy());
                if (createUser != null) {
                    userMap.put(createUser.getUserId(), createUser);
                }
            }
            if (createUser != null) {
                properties.setAnnotation_owner(createUser.getUserName());
            }
        }
        if (annotation.getUpdateBy() != null) {
            User updateUser = userMap.get(annotation.getUpdateBy());
            if (updateUser == null) {
                updateUser = userMapper.selectById(annotation.getUpdateBy());
                if (updateUser != null) {
                    userMap.put(updateUser.getUserId(), updateUser);
                }
            }
            if (updateUser != null) {
                properties.setAnnotation_owner(updateUser.getUserName());
            }
        }
    	return properties;
    }


    @Override
    public List<Features> selectListBy(AnnotationSelectList req) throws Exception {
        List<Features> list = new ArrayList<>();
        Annotation annotation = new Annotation();
        annotation.setSlideId(req.getSlideId());
        annotation.setCategoryId(req.getCategoryId());
        annotation.setSingleSlideId(req.getSingleSlideId());
        List<Annotation> selfAnnoList = annotationMapper.selectListBy(annotation);
        List<Features> annoList1 = getFeaturesList(selfAnnoList);
        if (CollectionUtils.isNotEmpty(annoList1)) {
            list.addAll(annoList1);
        }
        return list;
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
        String id = null;
        Category category = null;
        Annotation annotation = new Annotation();
        if (req.getCategory_id() != null) {
            if (req.getSingle_slide_id() != null) {
                PathologicalIndicatorCategory pathologicalIndicatorCategory = pathologicalIndicatorCategoryMapper.selectById(req.getCategory_id());
                if (pathologicalIndicatorCategory != null) {
                    id = MarkingUtils.getSdId(pathologicalIndicatorCategory.getCategoryName());
                }
            } else {
                category = categoryMapper.selectById(req.getCategory_id());
                if (category != null) {
                    id = MarkingUtils.getSdId(category.getOrganName());
                }
            }
        } else {
            id = MarkingUtils.getSdId(null);
        }
        if (req.getSingle_slide_id() != null) {
            annotation.setSingleSlideId(req.getSingle_slide_id());
            annotation.setSingle(1);
        }
        annotation.setSlideId(req.getSlide_id());
        annotation.setArea(req.getArea());
        annotation.setCategoryId(req.getCategory_id());
        annotation.setContour(String.valueOf(req.getGeometry()));
        annotation.setLocationType(req.getLocation_type());
        annotation.setPerimeter(req.getPerimeter());
        annotation.setCreateBy(req.getCreate_by());
        annotation.setId(id);
        annotation.setContourType(2L);
        annotation.setAnnotationType("Draw");
        annotationMapper.insert(annotation);
        Annotation annotationBy = annotationMapper.selectById(annotation);
        cn.staitech.fr.vo.geojson.Properties properties = getProperties(annotationBy);
        Features features = socketData(annotation.getId(), JSONObject.parseObject(annotationBy.getContour()), properties);
        BroadcastVO broadcastVO = sendOneMessages(ADD_STATUS, features);
        if(req.getSingle_slide_id() != null){
            NioWebSocketHandler.sendSingle(req.getSingle_slide_id(),broadcastVO);
        }else{
            NioWebSocketHandler.sendAll(annotation.getSlideId(), broadcastVO);
        }

        if (req.getSingle_slide_id() == null) {
            Image image = imageMapper.selectById(slide.getImageId());
            if (!Optional.ofNullable(image).isPresent()) {
                throw new Exception(MessageSource.M("NODATA"));
            }
            Special special = specialService.getById(slide.getSpecialId());
            List<JSONObject> contourList = selectContourList(annotation.getSlideId(), req.getCategory_id());
            asyncTask.generateThumbnail(annotation.getSlideId(), req.getCategory_id(), image.getImageUrl(), contourList, 1, category.getCategoryAbbreviation(),special.getSpecialName());
            AlgorithmAnnIn algorithmAnnIn = new AlgorithmAnnIn();
            algorithmAnnIn.setSlideId(slide.getSlideId());
            algorithmPredictionService.recognition(algorithmAnnIn);
            // 切图完成后更新切片状态
            if(slide.getProcessFlag() != 2){
                slide.setProcessFlag(2);
                slideMapper.updateById(slide);
            }
        }
        {
            Long slideId;
            if(req.getSingle_slide_id() != null){
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

    public List<JSONObject> selectContourList(Long slideId, Long categoryId) {
        QueryWrapper<Annotation> annotationQueryWrapper = new QueryWrapper<>();
        annotationQueryWrapper.select("ST_AsGeoJSON(contour) AS contour").eq("slide_id", slideId).eq("category_id", categoryId);
        List<Annotation> annotations = annotationMapper.selectList(annotationQueryWrapper);
        List<JSONObject> contourList = new ArrayList<>();
        for (Annotation annotation1 : annotations) {
            contourList.add(JSONObject.parseObject(annotation1.getContour()));
        }
        return contourList;
    }



    @Override
    public R<String> roiContDel(RoiIn req) throws Exception {
        QueryWrapper<Annotation> annotationQueryWrapper = new QueryWrapper<>();
        req.getCategoryIds().add(null);
        annotationQueryWrapper.select("annotation_id,category_id,create_by,ST_AsGeoJSON(contour) AS contour").eq("single_slide_id", req.getSingleSlideId()).eq("create_by",SecurityUtils.getUserId()).in("category_id",req.getCategoryIds());
        List<Annotation> features = annotationMapper.selectList(annotationQueryWrapper);
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
        //标注信息
        Map<Long, Annotation> markingMap = features.stream().collect(Collectors.toMap(Annotation::getAnnotationId, Function.identity()));
        //异步删除
        CompletableFuture<Integer> cf1 = CompletableFuture.supplyAsync(() -> {
            Set<Long> categoryIds = new HashSet<>();
            categoryIds.add(0L);
            Set<Long> createBys = new HashSet<>();
            if (CollectionUtils.isNotEmpty(annotationIdList)) {
                QueryWrapper<Annotation> wrapper = new QueryWrapper<>();
                wrapper.in("annotation_id", annotationIdList);
                //删除标注
                annotationMapper.delete(wrapper);
                //标注的createBy和categoryId
                for (Long markingId : annotationIdList) {
                    categoryIds.add(markingMap.get(markingId).getCategoryId());
                    createBys.add(markingMap.get(markingId).getCategoryId());
                }
            }
            BroadcastVO broadcastVO = SendMessage.sendListMessages(CommonConstant.ANNO_TYPE_DRAW, RELOAD_STATUS, null, null);
            NioWebSocketHandler.sendSingle(req.getSingleSlideId(), broadcastVO);
            return 1;
        });
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
        if (!Optional.ofNullable(req.getMarking_id()).isPresent()) {
            throw new Exception(MessageSource.M("ARGUMENT_INVALID"));
        }
        Annotation annotation = new Annotation();
        annotation.setAnnotationId(req.getMarking_id());
        Annotation annotationBy = annotationMapper.selectById(annotation);
        if (!Optional.ofNullable(annotationBy).isPresent()) {
            throw new Exception(MessageSource.M("NO_ANNOTATION_DATA"));
        }
        cn.staitech.fr.vo.geojson.Properties properties = getProperties(annotationBy);
        Features features = socketData(annotationBy.getId(), JSONObject.parseObject(annotationBy.getContour()), properties);
        BroadcastVO broadcastVO = sendOneMessages(DELETE_STATUS, features);

        if(annotationBy.getSingleSlideId() != null){
            NioWebSocketHandler.sendSingle(annotationBy.getSlideId(), broadcastVO);
        }else{
            NioWebSocketHandler.sendAll(annotationBy.getSlideId(), broadcastVO);
        }
        int res = annotationMapper.deleteById(annotation);
        if(annotationBy.getSingleSlideId() == null){
            Slide slide = slideMapper.selectById(annotationBy.getSlideId());
            if (!Optional.ofNullable(slide).isPresent()) {
                throw new Exception(MessageSource.M("NO_SLIDE_DATA"));
            }
            Image image = imageMapper.selectById(slide.getImageId());
            if (!Optional.ofNullable(image).isPresent()) {
                throw new Exception(MessageSource.M("NODATA"));
            }
            Category category = categoryMapper.selectById(annotationBy.getCategoryId());

            List<JSONObject> contourList = selectContourList(annotationBy.getSlideId(), annotationBy.getCategoryId());
            int type;
            if (contourList.size() > 0) {
                type = 1;
            } else {
                type = 2;
            }
            Special special = specialService.getById(slide.getSpecialId());
            asyncTask.generateThumbnail(annotationBy.getSlideId(), annotationBy.getCategoryId(), image.getImageUrl(), contourList, type,category.getCategoryAbbreviation(),special.getSpecialName());
            AlgorithmAnnIn algorithmAnnIn = new AlgorithmAnnIn();
            algorithmAnnIn.setSlideId(slide.getSlideId());
            algorithmPredictionService.recognition(algorithmAnnIn);
        }
        String traceId = req.getTraceId();
        boolean isBatch = req.getIsBatch();
        {
            Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
            // 删除操作RocksDB存删除前的数据
            // 撤消,恢复历史记录 用HistoryService会引起循环依赖！ -> 后续在线程池中处理 判断是批处理，还是单独处理
            // 1、创建Session,并存入ConcurrentHashMap<Long, Session>
            Long slideId;
            if(annotationBy.getSingleSlideId() != null){
                slideId = annotationBy.getSingleSlideId();
            } else {
                slideId = annotationBy.getSlideId();
            }
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


    @Override
    public Long update(ViewAddIn req) throws Exception {
        Annotation annotations = new Annotation();
        annotations.setAnnotationId(Long.valueOf(req.getMarking_id()));
        Annotation annotationBy = annotationMapper.selectById(annotations);
        Long userId = req.getUpdate_by();

        if (!Optional.ofNullable(annotationBy).isPresent()) {
            throw new Exception(MessageSource.M("NO_ANNOTATION_DATA"));
        }
        String traceId = req.getTraceId();
        boolean isBatch = req.getIsBatch();
        {
            Long slideId;
            if(annotationBy.getSingleSlideId() != null){
                slideId = annotationBy.getSingleSlideId();
            } else {
                slideId = annotationBy.getSlideId();
            }
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
//            // 3、数据持久化写入RocksDB
//            Gson gson = new Gson();
//            // 将对象转换成JSON字符串
//            String json = gson.toJson(markingBy);
//            RocksDBUtil.put(traceId, markingId, json);
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
        annotation.setId(id);
        annotation.setSlideId(req.getSlide_id());
        annotation.setAnnotationId(Long.valueOf(req.getMarking_id()));
        if(req.getGeometry() != null){
            annotation.setContour(String.valueOf(req.getGeometry()));
        }
        annotationMapper.updateById(annotation);
        Annotation annotationBys = annotationMapper.selectById(annotation);
        cn.staitech.fr.vo.geojson.Properties properties = getProperties(annotationBys);
        Features features = AnnotationDataEncapsulation.socketData(annotation.getId(), req.getGeometry(), properties);
        BroadcastVO broadcastVO = sendOneMessages(UPDATE_STATUS, features);
        if(annotationBy.getSingleSlideId() != null){
            NioWebSocketHandler.sendSingle(annotationBy.getSingleSlideId(),broadcastVO);
        }else{
            NioWebSocketHandler.sendAll(req.getSlide_id(), broadcastVO);
        }
        if(annotationBy.getSingleSlideId() == null){
            Slide slide = slideMapper.selectById(annotationBy.getSlideId());
            if (!Optional.ofNullable(slide).isPresent()) {
                throw new Exception(MessageSource.M("NO_SLIDE_DATA"));
            }
            Image image = imageMapper.selectById(slide.getImageId());
            if (!Optional.ofNullable(image).isPresent()) {
                throw new Exception(MessageSource.M("NODATA"));
            }
            List<JSONObject> contourList = selectContourList(annotation.getSlideId(), annotation.getCategoryId());
            int type;
            if (contourList.size() > 0) {
                type = 1;
            } else {
                type = 2;
            }
            Category categoryAfter = categoryMapper.selectById(req.getCategory_id());

            Special special = specialService.getById(slide.getSpecialId());

            asyncTask.generateThumbnail(annotation.getSlideId(), annotation.getCategoryId(), image.getImageUrl(), contourList, type,categoryAfter.getCategoryAbbreviation(),special.getSpecialName());
            // 改之后数据
            List<JSONObject> contourListAfter = selectContourList(annotation.getSlideId(), annotation.getCategoryId());

            String categoryAbbreviation = null;
            if(req.getCategory_id() != null){
                Category categoryBy = categoryMapper.selectById(req.getCategory_id());
                categoryAbbreviation = categoryBy.getCategoryAbbreviation();
            }else{
                categoryAbbreviation = categoryAfter.getCategoryAbbreviation();
            }
            asyncTask.generateThumbnail(annotation.getSlideId(), req.getCategory_id(), image.getImageUrl(), contourListAfter, 1,categoryAbbreviation,special.getSpecialName());
            AlgorithmAnnIn algorithmAnnIn = new AlgorithmAnnIn();
            algorithmAnnIn.setSlideId(slide.getSlideId());
            algorithmPredictionService.recognition(algorithmAnnIn);

        }
        return annotation.getAnnotationId();
    }

    @Override
    public int padding(AnnotationById req) throws Exception {


        if (!Optional.ofNullable(req.getMarking_id()).isPresent()) {
            throw new Exception(MessageSource.M("ARGUMENT_INVALID"));
        }
        Annotation annotations = new Annotation();
        annotations.setAnnotationId(req.getMarking_id());
        Annotation annotationBy = annotationMapper.selectById(annotations);
        if (!Optional.ofNullable(annotationBy).isPresent()) {
            throw new Exception(MessageSource.M("NO_ANNOTATION_DATA"));
        }


        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        Long loginUserId = sysUser.getUserId();
        {
            Long slideId;
            if(annotationBy.getSingleSlideId() != null){
                slideId = annotationBy.getSingleSlideId();
            } else {
                slideId = annotationBy.getSlideId();
            }
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
            annotation.setArea(area);
            String per = String.valueOf(geometry.getLength() * resolutions);
            annotation.setPerimeter(per);
        }
        annotation.setUpdateBy(SecurityUtils.getUserId());
        int res = annotationMapper.updateById(annotation);
        Annotation annotationBys = annotationMapper.selectById(annotations);
        cn.staitech.fr.vo.geojson.Properties properties = getProperties(annotationBys);
        Features features = AnnotationDataEncapsulation.socketData(annotationBys.getId(), geometryJson, properties);
        BroadcastVO broadcastVO = sendOneMessages(UPDATE_STATUS, features);
        NioWebSocketHandler.sendSingle(annotation.getSingleSlideId(), broadcastVO);
        return res;
    }


    @Override
    public int stickup(AnnotationById req) throws Exception {
        if (!Optional.ofNullable(req.getMarking_id()).isPresent()) {
            throw new Exception(MessageSource.M("ARGUMENT_INVALID"));
        }
        Annotation annotations = new Annotation();
        annotations.setAnnotationId(req.getMarking_id());
        Annotation annotation = annotationMapper.selectById(annotations);
        int res = annotationMapper.insert(annotation);
        Annotation annotationBys = annotationMapper.selectById(annotation.getAnnotationId());
        cn.staitech.fr.vo.geojson.Properties properties = getProperties(annotationBys);
        Features features = AnnotationDataEncapsulation.socketData(annotationBys.getId(), JSONObject.parseObject(annotation.getContour()), properties);
        BroadcastVO broadcastVO = sendOneMessages(ADD_STATUS, features);
        NioWebSocketHandler.sendSingle(annotation.getSingleSlideId(), broadcastVO);
        return res;
    }

    @Override
    public JSONObject markingMerge(MarkingMerge req) throws Exception {
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
    public JSONObject updateOperation(UpdateOperationIn req, String traceId, Boolean isBatch) throws Exception {
        Annotation annotations = new Annotation();
        annotations.setAnnotationId(req.getMarking_id());
        Annotation annotation = annotationMapper.selectById(annotations);
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
            annotation.setArea(area);
            String per = String.valueOf(Double.parseDouble(annotationArea.getPerimeter()) * resolutions);
            annotation.setPerimeter(per);
        }
        annotationMapper.updateById(annotationBys);
        // 更新后查询数据并返回
        Annotation annotationById = annotationMapper.selectById(annotations);
        Properties properties = getProperties(annotationById);
        Features features = AnnotationDataEncapsulation.socketData(annotationBys.getId(), JSONObject.parseObject(annotation1.getContour()), properties);
        BroadcastVO broadcastVO = sendOneMessages(UPDATE_STATUS, features);
        if(annotation.getSingleSlideId() != null){
            NioWebSocketHandler.sendSingle(annotation.getSingleSlideId(),broadcastVO);
        }else{
            NioWebSocketHandler.sendAll(annotation.getSlideId(), broadcastVO);
        }
        if(annotation.getSingleSlideId() == null){
            Category category = categoryMapper.selectById(annotation.getCategoryId());
            List<JSONObject> contourList = selectContourList(annotation.getSlideId(), annotation.getCategoryId());
            Special special = specialService.getById(slide.getSpecialId());
            asyncTask.generateThumbnail(annotation.getSlideId(), annotation.getCategoryId(), image.getImageUrl(), contourList, 1,category.getCategoryAbbreviation(),special.getSpecialName());
            AlgorithmAnnIn algorithmAnnIn = new AlgorithmAnnIn();
            algorithmAnnIn.setSlideId(slide.getSlideId());
            algorithmPredictionService.recognition(algorithmAnnIn);
        }
        {
            Long userId = req.getUpdate_by();
            Long slideId;
            if(annotation.getSingleSlideId() != null){
                slideId = annotation.getSingleSlideId();
            } else {
                slideId = annotation.getSlideId();
            }
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

//            // 3、数据持久化写入RocksDB
//            Gson gson = new Gson();
//            // 将对象转换成JSON字符串
//            String json = gson.toJson(markingBy);
//            RocksDBUtil.put(traceId, markingId, json);
            rocksdbService.submitTask(traceId, annotationId, annotation);
        }
        return JSONObject.parseObject(annotation1.getContour());
    }

    @Override
    public List<AnnotationCountByCategory> getCategoryCount(Long slideId) {
        return annotationMapper.getCategoryCount(slideId);
    }

    @Override
    public List<BatchResult> batch(List<ViewAddIn> list) throws Exception {
        String traceId = UUID.randomUUID().toString();

        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        Long userId = sysUser.getUserId();
        Long slideId = list.get(0).getSingle_slide_id();

        Session session = HistoryServiceImpl.refreshSession(userId, slideId);
        // 2、创建Trace,并存入Session.list,LinkedList<Trace>
        // 单条记录
        Trace trace = new Trace(userId, traceId, true);
        session.drawListAdd(trace);

        List<BatchResult> result = new ArrayList<>(list.size());
        for (ViewAddIn dto : list) {
            BatchResult batchResult = new BatchResult();
            batchResult.setFront_id(dto.getMarking_id());

            dto.setUpdate_by(userId);
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
        Properties properties = getProperties(annotationById);
        Features features = MarkingUtils.socketData(String.valueOf(annotation.getAnnotationId()), JSONObject.parseObject(annotation.getContour()), properties);
        BroadcastVO broadcastVO = SendMessage.sendListMessages(CommonConstant.ANNO_TYPE_DRAW, ADD_STATUS, features, null);
        if(annotationById.getSingleSlideId() != null){
            NioWebSocketHandler.sendSingle(annotationById.getSingleSlideId(), broadcastVO);
        } else{
            NioWebSocketHandler.sendAll(annotationById.getSlideId(), broadcastVO);
        }
        return annotationById;
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
        annotation.setUpdateTime(String.valueOf(new Date()));
        annotationMapper.updateById(annotation);
        Properties properties = getProperties(annotationBy);
        Features features = AnnotationDataEncapsulation.socketData(String.valueOf(annotationBy.getAnnotationId()), JSONObject.parseObject(req.getContour()), properties);
        BroadcastVO broadcastVO = SendMessage.sendOneMessagesByAnnoType(CommonConstant.ANNO_TYPE_DRAW, UPDATE_STATUS, features);
        if(annotationBy.getSingleSlideId() != null){
            NioWebSocketHandler.sendSingle(annotationBy.getSingleSlideId(), broadcastVO);
        } else{
            NioWebSocketHandler.sendAll(annotationBy.getSlideId(), broadcastVO);
        }
        return annotationBy;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Annotation deleteByHistory(Long annotationId) {
        Annotation annotationById = annotationMapper.selectById(annotationId);
        Properties properties = getProperties(annotationById);
        Features features = AnnotationDataEncapsulation.socketData(String.valueOf(annotationId), JSONObject.parseObject(annotationById.getContour()), properties);
        BroadcastVO broadcastVO = SendMessage.sendListMessages(CommonConstant.ANNO_TYPE_DRAW, DELETE_STATUS, features, null);
        annotationMapper.deleteById(annotationById);
        if(annotationById.getSingleSlideId() != null){
            NioWebSocketHandler.sendSingle(annotationById.getSingleSlideId(), broadcastVO);
        } else{
            NioWebSocketHandler.sendAll(annotationById.getSlideId(), broadcastVO);
        }
        return annotationById;
    }


    @Override
    public Boolean undo(HistoryDTO dto) {
        String traceId = UUID.randomUUID().toString();
        //Boolean isUndo = dto.getEnvType() == 1 ? true : false;
        Long userId = dto.getUserId();
        Long slideId;
        if(dto.getSingleSlideId() != null){
            slideId = dto.getSingleSlideId();
        }else {
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


    @Override
    public Boolean redo(HistoryDTO dto) {
        String traceId = UUID.randomUUID().toString();
        // Boolean isUndo = dto.getEnvType() == 1 ? true : false;

        Long userId = dto.getUserId();
        Long slideId;
        if(dto.getSingleSlideId() != null){
            slideId = dto.getSingleSlideId();
        }else {
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


}




