package cn.staitech.fr.service.impl;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.utils.uuid.IdUtils;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.domain.history.Session;
import cn.staitech.fr.domain.history.Trace;
import cn.staitech.fr.domain.history.TraceNode;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.mapper.UserMapper;
import cn.staitech.fr.netty.websocket.NioWebSocketHandler;
import cn.staitech.fr.service.SlideService;
import cn.staitech.fr.utils.ExcludeEmptyQueryWrapper;
import cn.staitech.fr.utils.MarkingUtils;
import cn.staitech.fr.utils.MessageSource;
import cn.staitech.fr.utils.SendMessage;
import cn.staitech.fr.vo.annotation.AnnotationById;
import cn.staitech.fr.vo.geojson.Features;
import cn.staitech.fr.vo.geojson.Properties;
import cn.staitech.fr.vo.geojson.in.ViewAddIn;
import cn.staitech.fr.vo.measure.BroadcastVO;
import cn.staitech.fr.vo.measure.MarkingSelectListVO;
import cn.staitech.fr.vo.measure.PointCount;
import cn.staitech.system.api.domain.SysUser;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.staitech.fr.service.MeasureService;
import cn.staitech.fr.mapper.MeasureMapper;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static cn.staitech.fr.constant.CommonConstant.ADD_STATUS;
import static cn.staitech.fr.constant.CommonConstant.DELETE_STATUS;
import static cn.staitech.fr.utils.MarkingUtils.socketData;
import static cn.staitech.fr.utils.SendMessage.sendOneMessages;

/**
 * @author admin
 * @description 针对表【fr_measure】的数据库操作Service实现
 * @createDate 2024-04-09 14:42:38
 */
@Service
public class MeasureServiceImpl extends ServiceImpl<MeasureMapper, Measure>
        implements MeasureService {

    @Resource
    private SlideMapper slideMapper;

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
            JSONObject jsonObject = (JSONObject) JSON.toJSON(getProperties(measure));
            features.setProperties(jsonObject);
            featuresList.add(features);
        }
        return featuresList;
    }


    @Override
    public PageResponse<MarkingSelectListVO> list(Long slideId, Integer pageNum, Integer pageSize, String measureFullName) throws Exception {
        Slide slideBy = slideMapper.selectById(slideId);
        if (!Optional.ofNullable(slideBy).isPresent()) {
            throw new Exception(MessageSource.M("SLIDE_ABNORMAL_NO_INFORMATION"));
        }
        Integer resPageNum = pageNum;
        if (pageNum > 0) {
            pageNum = pageNum - 1;
        } else {
            pageNum = 0;
        }
        Map<String, Object> map = new HashMap<>(16);
        map.put("slideId", slideId);
        map.put("measureFullName", measureFullName);
        map.put("pageSize", pageSize);
        map.put("pageNum", pageNum * pageSize);
        ExcludeEmptyQueryWrapper<Measure> measureQueryWrapper = new ExcludeEmptyQueryWrapper<>();
        measureQueryWrapper.eq("slide_id", slideId).ne("location_type", "Point").like("measure_full_name", measureFullName);
        Integer markingCount = measureMapper.selectCount(measureQueryWrapper);
        ExcludeEmptyQueryWrapper<Measure> measurePointCount = new ExcludeEmptyQueryWrapper<>();
        measurePointCount.eq("slide_id", slideId).eq("location_type", "Point").like("measure_full_name", measureFullName);
        Integer measurePointCounts = measureMapper.selectCount(measurePointCount);
        List<Measure> measureList = measureMapper.selectList(measureQueryWrapper);
        markingCount = markingCount + 1;
        int pageShow = (markingCount / pageSize) + 1;
        PageResponse<MarkingSelectListVO> resp = new PageResponse<>();
        if (measureList.size() < pageSize) {
            Measure measure = new Measure();
            measure.setMeasureFullName("P");
            measure.setPointCount(measurePointCounts);
            measureList.add(measure);
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
            markingSelectListVO.setPoint_count(Long.valueOf(measure.getPointCount()));
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
        Slide slideBy = slideMapper.selectById(slideId);
        if (!Optional.ofNullable(slideBy).isPresent()) {
            throw new Exception(MessageSource.M("SLIDE_ABNORMAL_NO_INFORMATION"));
        }
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
        cn.staitech.fr.vo.geojson.Properties properties = getProperties(measureBy);
        Features features = MarkingUtils.socketData(null, JSONObject.parseObject(measure.getContour()), properties);
        BroadcastVO broadcastVO = SendMessage.sendOneMessagesByAnnoType(CommonConstant.ANNO_TYPE_MEASURE, ADD_STATUS, features);
        NioWebSocketHandler.sendAll(req.getSlide_id(), broadcastVO);
        return measure.getMeasureId();
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
        cn.staitech.fr.vo.geojson.Properties properties = getProperties(measureBy);
        Features features = MarkingUtils.socketData(null, JSONObject.parseObject(measure.getContour()), properties);
        BroadcastVO broadcastVO = SendMessage.sendOneMessagesByAnnoType(CommonConstant.ANNO_TYPE_MEASURE, DELETE_STATUS, features);
        NioWebSocketHandler.sendAll(measure.getSlideId(), broadcastVO);
        return measureMapper.deleteById(markingId);
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

    public cn.staitech.fr.vo.geojson.Properties getProperties(Measure measure) {
        cn.staitech.fr.vo.geojson.Properties properties = new cn.staitech.fr.vo.geojson.Properties();
        properties.setArea(measure.getArea());
        properties.setPerimeter(measure.getPerimeter());
        properties.setAnnotation_type(measure.getAnnotationType());
        properties.setLocation_type(measure.getLocationType());
        properties.setCreate_by(measure.getCreateBy());
        properties.setUpdate_by(measure.getUpdateBy());
        properties.setCreate_time(String.valueOf(measure.getCreateTime()));
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




