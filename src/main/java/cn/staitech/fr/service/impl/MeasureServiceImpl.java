package cn.staitech.fr.service.impl;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.User;
import cn.staitech.fr.mapper.UserMapper;
import cn.staitech.fr.netty.websocket.NioWebSocketHandler;
import cn.staitech.fr.utils.*;
import cn.staitech.fr.vo.annotation.Features;
import cn.staitech.fr.vo.annotation.Properties;
import cn.staitech.fr.vo.annotation.PropertiesBriefly;
import cn.staitech.fr.vo.annotation.in.ViewAddIn;
import cn.staitech.fr.vo.annotation.out.BroadcastVO;
import cn.staitech.fr.vo.measure.MarkingSelectListVO;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.staitech.fr.domain.Measure;
import cn.staitech.fr.service.MeasureService;
import cn.staitech.fr.mapper.MeasureMapper;
import com.ibm.icu.text.SimpleDateFormat;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static cn.staitech.fr.constant.CommonConstant.ADD_STATUS;
import static cn.staitech.fr.constant.CommonConstant.DELETE_STATUS;

/**
* @author admin
* @description 针对表【fr_measure】的数据库操作Service实现
* @createDate 2024-09-10 11:24:58
*/
@Service
public class MeasureServiceImpl extends ServiceImpl<MeasureMapper, Measure>
    implements MeasureService{



    @Resource
    private MeasureMapper measureMapper;
    @Resource
    private UserMapper userMapper;

    HashMap<Long, User> userMap = new HashMap<>();


    public List<Features> getFeaturesList(List<Measure> measures) {
        List<Features> featuresList = new ArrayList<>();
        for (Measure measure : measures) {
            Features features = new Features();
            features.setGeometry(JSONObject.parseObject(measure.getContour()));
            features.setId(null);
            features.setType("Feature");
            String s1 = JSONObject.toJSONString(getPropertiesBriefly(measure), SerializerFeature.PrettyFormat);
            JSONObject jsonObject = JSONObject.parseObject(s1);
            features.setProperties(jsonObject);
            featuresList.add(features);
        }
        return featuresList;
    }


    @Override
    public PageResponse<MarkingSelectListVO> list(Long slideId, Integer pageNum, Integer pageSize, String measureFullName) throws Exception {
        Integer resPageNum = pageNum;
        if (pageNum > 0) {
            pageNum = pageNum - 1;
        } else {
            pageNum = 0;
        }
        PageResponse<MarkingSelectListVO> resp = new PageResponse<>();
        ExcludeEmptyQueryWrapper<Measure> measureQueryWrapper = new ExcludeEmptyQueryWrapper<>();
        measureQueryWrapper.eq("slide_id", slideId).ne("location_type", "Point").like("measure_full_name", measureFullName);
        Integer markingCount = measureMapper.selectCount(measureQueryWrapper);
        ExcludeEmptyQueryWrapper<Measure> measurePointCount = new ExcludeEmptyQueryWrapper<>();
        measurePointCount.eq("slide_id", slideId).eq("location_type", "Point").like("measure_full_name", measureFullName);
        Integer measurePointCounts = measureMapper.selectCount(measurePointCount);
        measureQueryWrapper.last("limit " + pageNum * pageSize + "," + pageSize);
        List<Measure> measureList = measureMapper.selectList(measureQueryWrapper);
        markingCount = markingCount + 1;
        int pageShow = (markingCount / pageSize) + 1;

        if(measurePointCounts > 0){
            if (measureList.size() < pageSize) {
                Measure measure = new Measure();
                measure.setMeasureFullName("P");
                measure.setPointCount(measurePointCounts);
                measureList.add(measure);
            }
        }
        List<MarkingSelectListVO> markingSelectList = new ArrayList<>();
        for(Measure measure:measureList){
            MarkingSelectListVO markingSelectListVO = new MarkingSelectListVO();
            markingSelectListVO.setMarking_id(String.valueOf(measure.getMeasureId()));
            markingSelectListVO.setArea(measure.getArea());
            markingSelectListVO.setPerimeter(measure.getPerimeter());
            markingSelectListVO.setCreate_time(measure.getCreateTime());
            markingSelectListVO.setInner_angle(measure.getInnerAngle());
            markingSelectListVO.setExterior_angle(measure.getExteriorAngle());
            markingSelectListVO.setMax_distance(measure.getMaxDistance());
            markingSelectListVO.setMin_distance(measure.getMinDistance());
            markingSelectListVO.setMean_distance(measure.getMeanDistance());
            markingSelectListVO.setMeasure_full_name(measure.getMeasureFullName());
            markingSelectListVO.setPoint_count(measure.getPointCount());
            markingSelectList.add(markingSelectListVO);
        }
        resp.setTotal(markingCount);
        resp.setList(markingSelectList);
        resp.setPages(pageShow);
        resp.setPageNum(resPageNum);
        resp.setPageSize(pageSize);
        return resp;
    }


    @Override
    public List<Features> selectListBy(Long slideId) throws Exception {
        QueryWrapper<Measure> measureQueryWrapper = new QueryWrapper<>();
        measureQueryWrapper.eq("slide_id", slideId);
        List<Measure> measures = measureMapper.selectList(measureQueryWrapper);
        return getFeaturesList(measures);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insert(ViewAddIn req) throws Exception {
        if (req.getGeometry() != null && !req.getGeometry().isEmpty()) {
            MarkingUtils.addVerify(req.getGeometry());
        } else {
            throw new Exception(MessageSource.M("ARGUMENT_INVALID"));
        }
        Measure measure = trans2Marking(req);
        measure.setPerimeter(req.getPerimeter());
        measure.setArea(req.getArea());
        measure.setCreateBy(req.getCreate_by());
        measure.setAnnotationType("Measure");
        measure.setCreateTime(new Date());
        long number = 1L;
        QueryWrapper<Measure> markingQueryWrapper = new QueryWrapper<>();
        // 根据切片和测量轮廓名称查询最大值
        markingQueryWrapper.eq("slide_id", req.getSlide_id()).eq("measure_name", req.getMeasure_name()).orderByDesc("create_time").last("limit 1");
        Measure markingBy = measureMapper.selectOne(markingQueryWrapper);
        if (markingBy != null) {
            if (markingBy.getNumber() != null) {
                number += markingBy.getNumber();
            }
        }
        String measureFullName = req.getMeasure_name() + number;
        measure.setMeasureFullName(measureFullName);
        measure.setNumber(number);
        measureMapper.insert(measure);
        Measure measureBy = measureMapper.selectById(measure.getMeasureId());
        PropertiesBriefly properties = getPropertiesBriefly(measureBy);
        Features features = socketData(null, JSONObject.parseObject(measure.getContour()), properties);
        BroadcastVO broadcastVO = sendOneMessagesByAnnoType(CommonConstant.ANNO_TYPE_MEASURE, ADD_STATUS, features);
        NioWebSocketHandler.sendAll(req.getSlide_id(), broadcastVO);
        return measure.getMeasureId();
    }

    public static BroadcastVO sendOneMessagesByAnnoType(String annoType, String status, Features features) {
        BroadcastVO broadcast = new BroadcastVO();
        broadcast.setData(features);
        broadcast.setType(status);
        broadcast.setAnnotation_type(annoType);
        broadcast.setPoint_count_list(new ArrayList<>());
        return broadcast;
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


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Long markingId) throws Exception {
        if (!Optional.ofNullable(markingId).isPresent()) {
            throw new Exception(MessageSource.M("ARGUMENT_INVALID"));
        }
        Measure measure = measureMapper.selectById(markingId);
        if (!Optional.ofNullable(measure).isPresent()) {
            throw new Exception(MessageSource.M("NO_ANNOTATION_DATA"));
        }
        Measure measureBy = measureMapper.selectById(measure.getMeasureId());
        PropertiesBriefly properties = getPropertiesBriefly(measureBy);
        Features features = socketData(null, JSONObject.parseObject(measure.getContour()), properties);
        BroadcastVO broadcastVO = sendOneMessagesByAnnoType(CommonConstant.ANNO_TYPE_MEASURE, DELETE_STATUS, features);
        NioWebSocketHandler.sendAll(measure.getSlideId(), broadcastVO);



        return measureMapper.deleteById(markingId);
    }



    @Override
    public void execlExport(Long slideId, HttpServletResponse response) throws Exception {
        List<Map<String, String>> titleList = getTitleList(CommonConstant.MEASURE_COLHEAD_KEY, CommonConstant.MEASURE_COLHEAD_VALUE);
        ExcludeEmptyQueryWrapper<Measure> measureQueryWrapper = new ExcludeEmptyQueryWrapper<>();
        measureQueryWrapper.eq("slide_id", slideId).ne("location_type", "Point");
        List<Measure> measureList = measureMapper.selectList(measureQueryWrapper);
        List<Properties> propertiesList = new ArrayList<>();
        for(Measure measure:measureList){
            propertiesList.add(getProperties(measure));
        }
        ExcludeEmptyQueryWrapper<Measure> measurePointCount = new ExcludeEmptyQueryWrapper<>();
        measurePointCount.eq("slide_id", slideId).eq("location_type", "Point");
        Integer measurePointCounts = measureMapper.selectCount(measurePointCount);
        if(measurePointCounts > 0){
            cn.staitech.fr.vo.annotation.Properties properties = new cn.staitech.fr.vo.annotation.Properties();
            properties.setPoint_count(measurePointCounts);
            properties.setMeasure_full_name("P");
            propertiesList.add(properties);
        }
        ExcelTool excelTool = new ExcelTool(MessageSource.M("EXCEL_TITLE"), 20, 20);
        List<Column> titleData = excelTool.columnTransformer(titleList);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        excelTool.exportExcel(titleData, propertiesList, response.getOutputStream(), true, false);
    }

    public List<Map<String, String>> getTitleList(String[] colHeadKey, String[] colHeadValue) {
        // 定义表头
        List<Map<String, String>> list = new ArrayList<>();

        for (int i = 0; i < colHeadKey.length; i++) {
            Map<String, String> map = new HashMap<String, String>(1);
            map.put(colHeadKey[i], colHeadValue[i]);
            list.add(map);
        }
        return list;
    }


    private Measure trans2Marking(ViewAddIn view) {
        Measure measure = new Measure();
        measure.setSlideId(view.getSlide_id());
        if (null != view.getCreate_by()) {
            measure.setCreateBy(view.getCreate_by());
        }
        if (StringUtils.isNotEmpty(view.getArea())) {
            measure.setArea(view.getArea());
        }
        if (StringUtils.isNotEmpty(view.getPerimeter())) {
            measure.setPerimeter(view.getPerimeter());
        }

        if (StringUtils.isNotEmpty(view.getLocation_type())) {
            measure.setLocationType(view.getLocation_type());
        }
        if (null != view.getGeometry()) {
            measure.setContour(String.valueOf(view.getGeometry()));
        }
        if (null != view.getMeasure_type()) {
            measure.setMeasureType(view.getMeasure_type());
        }

        if (StringUtils.isNotEmpty(view.getMeasure_relation())) {
            measure.setMeasureRelation(view.getMeasure_relation());
        }
        if (StringUtils.isNotEmpty(view.getMeasure_name())) {
            measure.setMeasureName(view.getMeasure_name());
        }
        if (null != view.getMeasure_number()) {
            measure.setMeasureNumber(view.getMeasure_number());
        }
        if (StringUtils.isNotEmpty(view.getRadius())) {
            measure.setRadius(view.getRadius());
        }
        if (null != view.getMean_distance()) {
            measure.setMeanDistance(view.getMean_distance());
        }
        if (null != view.getMax_distance()) {
            measure.setMaxDistance(view.getMax_distance());
        }
        if (null != view.getMin_distance()) {
            measure.setMinDistance(view.getMin_distance());
        }
        if (StringUtils.isNotEmpty(view.getInner_angle())) {
            measure.setInnerAngle(view.getInner_angle());
        }
        if (StringUtils.isNotEmpty(view.getExterior_angle())) {
            measure.setExteriorAngle(view.getExterior_angle());
        }
        if (StringUtils.isNotEmpty(view.getCenter_point())) {
            measure.setCenterPoint(view.getCenter_point());
        }
        return measure;
    }

    public PropertiesBriefly getPropertiesBriefly(Measure measure) {
        PropertiesBriefly properties = new PropertiesBriefly();
        properties.setA0(String.valueOf(measure.getMeasureId()));
        properties.setA7(measure.getArea());
        properties.setA6(measure.getPerimeter());
        properties.setA2(measure.getAnnotationType());
        properties.setA1(measure.getLocationType());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        properties.setA12(sdf.format(measure.getCreateTime()));
        properties.setA21(measure.getMeasureName());
        properties.setA24(measure.getMeasureType());
//        properties.setNumber(measure.getNumber());
        properties.setA23(measure.getMeasureRelation());
        properties.setA22(measure.getMeasureNumber());
        properties.setA19(measure.getMeanDistance());
        properties.setA20(measure.getMinDistance());
        properties.setA18(measure.getMaxDistance());
        properties.setA16(measure.getInnerAngle());
        properties.setA15(measure.getExteriorAngle());
        properties.setA14(measure.getCenterPoint());
        properties.setA17(measure.getRadius());
        properties.setA25(measure.getMeasureFullName());
        if (measure.getCreateBy() != null) {
            User createUser = userMap.get(measure.getCreateBy());
            if (createUser == null) {
                createUser = userMapper.selectById(measure.getCreateBy());
                if (createUser != null) {
                    userMap.put(createUser.getUserId(), createUser);
                }
            }
            if (createUser != null) {
                properties.setA9(createUser.getUserName());
            }
        }
        if (measure.getUpdateBy() != null) {
            User updateUser = userMap.get(measure.getUpdateBy());
            if (updateUser == null) {
                updateUser = userMapper.selectById(measure.getUpdateBy());
                if (updateUser != null) {
                    userMap.put(updateUser.getUserId(), updateUser);
                }
            }
            if (updateUser != null) {
                properties.setA10(updateUser.getUserName());
            }
        }
        return properties;
    }





    public cn.staitech.fr.vo.annotation.Properties getProperties(Measure measure) {
        cn.staitech.fr.vo.annotation.Properties properties = new cn.staitech.fr.vo.annotation.Properties();
        properties.setMarking_id(String.valueOf(measure.getMeasureId()));
        properties.setArea(measure.getArea());
        properties.setPerimeter(measure.getPerimeter());
        properties.setAnnotation_type(measure.getAnnotationType());
        properties.setLocation_type(measure.getLocationType());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        properties.setCreate_time(sdf.format(measure.getCreateTime()));
        properties.setMeasure_name(measure.getMeasureName());
        properties.setMeasure_type(measure.getMeasureType());
        properties.setNumber(measure.getNumber());
        properties.setMeasure_relation(measure.getMeasureRelation());
        properties.setMeasure_number(measure.getMeasureNumber());
        properties.setMean_distance(measure.getMeanDistance());
        properties.setMin_distance(measure.getMinDistance());
        properties.setMax_distance(measure.getMaxDistance());
        properties.setInner_angle(measure.getInnerAngle());
        properties.setExterior_angle(measure.getExteriorAngle());
        properties.setCenter_point(measure.getCenterPoint());
        properties.setRadius(measure.getRadius());
        properties.setMeasure_full_name(measure.getMeasureFullName());
        if (measure.getCreateBy() != null) {
            User createUser = userMap.get(measure.getCreateBy());
            if (createUser == null) {
                createUser = userMapper.selectById(measure.getCreateBy());
                if (createUser != null) {
                    userMap.put(createUser.getUserId(), createUser);
                }
            }
            if (createUser != null) {
                properties.setAnnotation_owner(createUser.getUserName());
            }
        }
        if (measure.getUpdateBy() != null) {
            User updateUser = userMap.get(measure.getUpdateBy());
            if (updateUser == null) {
                updateUser = userMapper.selectById(measure.getUpdateBy());
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

}




