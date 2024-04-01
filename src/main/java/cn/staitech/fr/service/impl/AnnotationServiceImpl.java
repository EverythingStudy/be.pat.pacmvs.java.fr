package cn.staitech.fr.service.impl;

import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.netty.websocket.NioWebSocketHandler;
import cn.staitech.fr.utils.AnnotationDataEncapsulation;
import cn.staitech.fr.utils.MarkingUtils;
import cn.staitech.fr.utils.MessageSource;
import cn.staitech.fr.utils.WktUtil;
import cn.staitech.fr.vo.annotation.AnnotationById;
import cn.staitech.fr.vo.annotation.AnnotationSelectList;
import cn.staitech.fr.vo.annotation.MarkingMerge;
import cn.staitech.fr.vo.geojson.Features;
import cn.staitech.fr.vo.geojson.Properties;
import cn.staitech.fr.vo.geojson.in.UpdateOperationIn;
import cn.staitech.fr.vo.geojson.in.ViewAddIn;
import cn.staitech.fr.vo.measure.BroadcastVO;
import cn.staitech.system.api.domain.SysUser;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cn.staitech.fr.service.AnnotationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.overlay.OverlayOp;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static cn.staitech.fr.constant.CommonConstant.*;
import static cn.staitech.fr.utils.MarkingUtils.socketData;
import static cn.staitech.fr.utils.SendMessage.sendOneMessages;

/**
* @author admin
* @description 针对表【fr_annotation】的数据库操作Service实现
* @createDate 2024-04-01 09:42:42
*/
@Service
public class AnnotationServiceImpl extends ServiceImpl<AnnotationMapper, Annotation>
    implements AnnotationService{


    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

    private static final WKTReader WKT_READER = new WKTReader(GEOMETRY_FACTORY);

    HashSet<Long> annotationSet = new HashSet<>();
    HashMap<Long, Category> categoryHashMap = new HashMap<>();
    HashMap<Long, SysUser> userMap = new HashMap<>();
    @Resource
    private AnnotationMapper annotationMapper;
    @Resource
    private SlideMapper slideMapper;
    @Resource
    private ImageMapper imageMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private StructureMapper structureMapper;


    public List<Features> getFeaturesList(List<Annotation> annotations) {
        List<Features> featuresList = new ArrayList<>();
        for (Annotation annotation : annotations) {
            Features features = new Features();
            features.setGeometry(JSON.parseObject(annotation.getContour()));
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
//        properties.setLabel_code(annotation.getStructureId());
        properties.setDescription(annotation.getDescription());
        properties.setLocation_type(annotation.getLocationType());
        properties.setCreate_by(annotation.getCreateBy());
        properties.setUpdate_by(annotation.getUpdateBy());
        properties.setCreate_time(String.valueOf(annotation.getCreateTime()));
        properties.setProject_id(annotation.getProjectId());
        if (annotation.getCategoryId() != null) {
            Category category = categoryHashMap.get(annotation.getCategoryId());
            if (category == null) {
                category = categoryMapper.selectById(annotation.getCategoryId());
                if (category != null) {
                    categoryHashMap.put(category.getCategoryId(), category);
                }
            }
//            if (category != null) {
//                properties.setLabel_color(category.getRgb());
//                properties.setLabel_code(category.getNumber());
//                properties.setLabel_name(category.getCategoryName());
//            }
        }
        if (annotation.getCreateBy() != null) {
            SysUser createUser = userMap.get(annotation.getCreateBy());
            if (createUser == null) {
//                createUser = userMapper.selectUserById(annotation.getCreateBy());
                if (createUser != null) {
                    userMap.put(createUser.getUserId(), createUser);
                }
            }
            if (createUser != null) {
                properties.setAnnotation_owner(createUser.getUserName());
            }
        }
        if (annotation.getUpdateBy() != null) {
            SysUser updateUser = userMap.get(annotation.getUpdateBy());
            if (updateUser == null) {
//                updateUser = userMapper.selectUserById(annotation.getUpdateBy());
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
        Slide slideBy = slideMapper.selectById(req.getSlideId());
        if (!Optional.ofNullable(slideBy).isPresent()) {
            throw new Exception(MessageSource.M("SLIDE_ABNORMAL_NO_INFORMATION"));
        }
        List<Features> list = new ArrayList<Features>();
        Annotation annotation = new Annotation();
        annotation.setSlideId(req.getSlideId());
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
            throw new Exception("更新失败，轮廓数据不能为空");
        }
        String id = null;
        String structureId = null;
        Integer structureSize = null;
        if (req.getCategory_id() != null) {
            Category category = categoryMapper.selectById(req.getCategory_id());
//            if (category != null) {
//                id = MarkingUtils.getSdId(category.getCategoryName());
//                structureId = category.getStructureId();
//            }
        }
        Slide slideBy = slideMapper.selectById(req.getSlide_id());
        Annotation annotation = new Annotation();
        annotation.setSlideId(req.getSlide_id());
        annotation.setArea(req.getArea());
        annotation.setContour(String.valueOf(req.getGeometry()));
        annotation.setLocationType(req.getLocation_type());
        annotation.setPerimeter(req.getPerimeter());
        annotation.setCreateBy(req.getCreate_by());
        annotation.setCategoryId(req.getCategory_id());
        annotation.setId(id);
        annotation.setAnnotationType("Draw");
        annotationMapper.insert(annotation);
        Annotation annotationBy = annotationMapper.selectById(annotation);
        cn.staitech.fr.vo.geojson.Properties properties = getProperties(annotationBy);
        Features features = socketData(annotation.getId(), JSON.parseObject(annotationBy.getContour()), properties);
        BroadcastVO broadcastVO = sendOneMessages(ADD_STATUS, features);
        NioWebSocketHandler.sendAll(annotation.getSlideId(), broadcastVO);
        return annotation.getAnnotationId();
    }

    @Override
    public void delete(AnnotationById req) throws Exception {
        if (!Optional.ofNullable(req.getMarking_id()).isPresent()) {
            throw new Exception(MessageSource.M("ARGUMENT_INVALID"));
        }
        Annotation annotations = new Annotation();
        annotations.setAnnotationId(req.getMarking_id());
        Annotation annotationBy = annotationMapper.selectById(annotations);
        if (!Optional.ofNullable(annotationBy).isPresent()) {
            throw new Exception(MessageSource.M("NO_ANNOTATION_DATA"));
        }
        cn.staitech.fr.vo.geojson.Properties properties = getProperties(annotationBy);
        Features features = socketData(annotationBy.getId(), JSON.parseObject(annotationBy.getContour()), properties);
        BroadcastVO broadcastVO = sendOneMessages(DELETE_STATUS, features);
        NioWebSocketHandler.sendAll(annotationBy.getSlideId(), broadcastVO);
        int res = annotationMapper.deleteById(annotations);
    }


    @Override
    public Long update(ViewAddIn req) throws Exception {
        Annotation annotations = new Annotation();
        annotations.setAnnotationId(Long.valueOf(req.getMarking_id()));
        Annotation annotationBy = annotationMapper.selectById(annotations);
        Long slideId = req.getSlide_id();

        if (!Optional.ofNullable(annotationBy).isPresent()) {
            throw new Exception(MessageSource.M("NO_ANNOTATION_DATA"));
        }
        Slide slide = slideMapper.selectById(slideId);
        if (!Optional.ofNullable(slide).isPresent()) {
            throw new Exception(MessageSource.M("NO_SLIDE_DATA"));
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
            if (req.getCategory_id() != 0 && !req.getCategory_id().equals(annotation.getCategoryId())) {
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
        annotation.setAnnotationId(Long.valueOf(req.getMarking_id()));
        annotation.setArea(req.getArea());
        annotation.setPerimeter(req.getPerimeter());
        annotation.setSlideId(req.getSlide_id());
        annotation.setContour(String.valueOf(req.getGeometry()));
        annotationMapper.updateById(annotation);
        Annotation annotationBys = annotationMapper.selectById(annotation);
        cn.staitech.fr.vo.geojson.Properties properties = getProperties(annotationBys);
        Features features = AnnotationDataEncapsulation.socketData(annotation.getId(), req.getGeometry(), properties);
        BroadcastVO broadcastVO = sendOneMessages(UPDATE_STATUS, features);
        NioWebSocketHandler.sendAll(annotation.getSlideId(), broadcastVO);
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

        Long slideId = annotationBy.getSlideId();
//        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
//        Long loginUserId = sysUser.getUserId();
//        String loginUserName = sysUser.getUserName();

        JSONObject geometryJson = MarkingUtils.padding(JSON.parseObject(annotationBy.getContour()));
        Annotation annotation = new Annotation();
        annotation.setAnnotationId(req.getMarking_id());
        annotation.setContour(String.valueOf(geometryJson));
        Geometry geometry = WKT_READER.read(MarkingUtils.jsonToWkt(JSON.parseObject(annotation.getContour())));
        Slide slide = slideMapper.selectById(slideId);
        Image image = imageMapper.selectById(slide.getImageId());
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
        NioWebSocketHandler.sendAll(annotation.getSlideId(), broadcastVO);
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
        Annotation annotationBys = annotationMapper.selectById(annotations);
        cn.staitech.fr.vo.geojson.Properties properties = getProperties(annotationBys);
        Features features = AnnotationDataEncapsulation.socketData(annotationBys.getId(), JSON.parseObject(annotation.getContour()), properties);
        BroadcastVO broadcastVO = sendOneMessages(ADD_STATUS, features);
        NioWebSocketHandler.sendAll(annotation.getSlideId(), broadcastVO);
        return res;
    }

    @Override
    public JSONObject markingMerge(MarkingMerge req) throws Exception {
        List<Annotation> annotationList = annotationMapper.selectInList(req);
        List<Geometry> geometryList = new ArrayList<>();
        for (Annotation annotation : annotationList) {
            Geometry geometry = WKT_READER.read(MarkingUtils.jsonToWkt(JSON.parseObject(annotation.getContour())));
            geometryList.add(geometry);
        }
        if (geometryList.size() > 0) {
            if (geometryList.size() == 1) {
                return JSON.parseObject(annotationList.get(0).getContour());
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


    /**
     * 二次校验
     *
     * @param req
     * @return
     * @throws Exception
     */
    @Override
    public JSONObject updateOperation(UpdateOperationIn req, String traceId, Boolean isBatch) throws Exception {
        if (annotationSet.contains(req.getMarking_id())) {
            throw new Exception(MessageSource.M("DATA.PROCESSING"));
        } else {
            annotationSet.add(req.getMarking_id());
        }
        Annotation annotations = new Annotation();
        annotations.setAnnotationId(Long.valueOf(req.getMarking_id()));
        Annotation annotation = annotationMapper.selectById(annotations);
        // 查询数据是否存在
        if (!Optional.ofNullable(annotation).isPresent()) {
            annotationSet.remove(req.getMarking_id());
            throw new Exception(MessageSource.M("NO_ANNOTATION_DATA"));
        }
        // TODO
        // 合并 - 校验飞点 TODO: MarkingUtils.updatePolygonPoint(jsonObject);
        Measure annotationBy = MarkingUtils.updateVerify(JSON.parseObject(annotation.getContour()), req.getGeometry(), req.getOperation(), req.getCheck(), req.getResolution());
        if (annotationBy.getException() != null) {
            annotationSet.remove(req.getMarking_id());
            throw new Exception(annotationBy.getException());
        }
        // 精度保留3位小数
        JSONObject jsonObject = JSONObject.parseObject(WktUtil.wktToJson(annotationBy.getData()));
        // 校验合并后的图形是否正常
        Annotation annotationBys = new Annotation();
        // 此处不设置ID则where id=null,不能正常执行。
        annotationBys.setAnnotationId(Long.valueOf(req.getMarking_id()));
        annotationBys.setContour(String.valueOf(jsonObject));
        annotationBys.setArea(annotationBy.getArea());
        annotationBys.setPerimeter(annotationBy.getPerimeter());
        annotationBys.setUpdateBy(req.getUpdate_by());
        annotationMapper.updateById(annotationBys);
        // 更新后查询数据并返回
        Annotation annotationById = annotationMapper.selectById(annotations);
        Properties properties = getProperties(annotationById);
        Features features = AnnotationDataEncapsulation.socketData(annotationBys.getId(), jsonObject, properties);
        BroadcastVO broadcastVO = sendOneMessages(UPDATE_STATUS, features);
        NioWebSocketHandler.sendAll(annotation.getSlideId(), broadcastVO);
        annotationSet.remove(req.getMarking_id());
        return jsonObject;
    }

}




