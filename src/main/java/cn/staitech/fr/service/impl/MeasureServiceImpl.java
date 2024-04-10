package cn.staitech.fr.service.impl;

import cn.hutool.db.meta.Column;
import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.utils.uuid.IdUtils;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.netty.websocket.NioWebSocketHandler;
import cn.staitech.fr.utils.*;
import cn.staitech.fr.vo.geojson.Features;
import cn.staitech.fr.vo.geojson.Properties;
import cn.staitech.fr.vo.geojson.in.MarkingUpdateIn;
import cn.staitech.fr.vo.geojson.in.UpdateOperationIn;
import cn.staitech.fr.vo.geojson.in.ViewAddIn;
import cn.staitech.fr.vo.measure.BroadcastVO;
import cn.staitech.fr.vo.measure.MeasureSelectPageVo;
import cn.staitech.fr.vo.measure.PointCount;
import cn.staitech.system.api.domain.SysUser;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.staitech.fr.domain.Measure;
import cn.staitech.fr.service.MeasureService;
import cn.staitech.fr.mapper.MeasureMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.swing.plaf.metal.MetalRadioButtonUI;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.staitech.fr.constant.CommonConstant.*;

/**
* @author admin
* @description 针对表【fr_measure】的数据库操作Service实现
* @createDate 2024-03-29 10:08:34
*/
@Service
public class MeasureServiceImpl extends ServiceImpl<MeasureMapper, Measure>
    implements MeasureService{

    @Resource
    private SlideMapper slideMapper;

    @Resource
    private MeasureMapper measureMapper;

    @Override
    public PageResponse<MeasureSelectPageVo> list(Long slideId, Integer pageNum, Integer pageSize, String measureFullName) throws Exception {
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
        // 查询总数量
        Integer markingCount = measureMapper.selectListCount(map);
        List<MeasureSelectPageVo> pointCountList = measureMapper.selectPointCountList(map);
        List<MeasureSelectPageVo> markingSelectListVoList = measureMapper.selectList(map);
        markingCount = markingCount + pointCountList.size();
        // 总页数
        int pageShow = (markingCount / pageSize) + 1;
        PageResponse<MeasureSelectPageVo> resp = new PageResponse<>();
        // 查询考核评分表中信息
        if (markingSelectListVoList.size() < pageSize) {
            for (MeasureSelectPageVo markingSelectListVO : pointCountList) {
                if (markingSelectListVoList.size() < pageSize) {
                    markingSelectListVoList.add(markingSelectListVO);
                }
            }
        }
        resp.setTotal(markingCount);
        resp.setList(markingSelectListVoList);
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
        return measureMapper.selectListBy(slideId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Long markingId) throws Exception {
        if (!Optional.ofNullable(markingId).isPresent()) {
            throw new Exception(MessageSource.M("ARGUMENT_INVALID"));
        }
        Measure markingBy = measureMapper.selectById(markingId);
        if (!Optional.ofNullable(markingBy).isPresent()) {
            throw new Exception(MessageSource.M("NO_ANNOTATION_DATA"));
        }
        Slide slide = slideMapper.selectById(markingBy.getSlideId());
        if (!Optional.ofNullable(slide).isPresent()) {
            throw new Exception(MessageSource.M("NO_SLIDE_DATA"));
        }
        Properties properties = measureMapper.selectBy(markingId);
        Features features = MarkingUtils.socketData(markingBy.getAnnotationId(), markingBy.getContour(), properties);
        List<PointCount> pointCountList = updatePoint(markingBy.getLocationType(), markingBy);
        BroadcastVO broadcastVO = SendMessage.sendListMessages(CommonConstant.ANNO_TYPE_MEASURE, DELETE_STATUS, features, pointCountList);
        NioWebSocketHandler.sendAll(markingBy.getSlideId(), broadcastVO);
        int res = measureMapper.deleteById(markingId);
        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insert(ViewAddIn req) throws Exception {
        if (req.getGeometry() != null && !req.getGeometry().isEmpty()) {
            MarkingUtils.addVerify(req.getGeometry());
        } else {
            throw new Exception("更新失败，轮廓数据不能为空");
        }

        //加slide缓存
        Slide slideBy = slideMapper.selectById(req.getSlide_id());

        if (slideBy == null) {
            throw new Exception(MessageSource.M("NO_SLIDE_DATA"));
        }

        Measure marking = trans2Marking(req);
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();

        // 获取规定的geoJson Id
        String annotationId = MarkingUtils.getSdId();
        marking.setPerimeter(req.getPerimeter());
        marking.setArea(req.getArea());
        // 若未传入标注作者,使用当前登录用户为标注作者==>必传Create_by 无默认
        marking.setCreateBy(req.getCreate_by());
        marking.setAnnotationType("Measure");
        marking.setCreateTime(new Date());
        // 查询
        Long number = 1L;
        QueryWrapper<Measure> measureQueryWrapper = new QueryWrapper<>();
        // 根据切片和测量轮廓名称查询最大值
        measureQueryWrapper.eq("slide_id", req.getSlide_id()).eq("measure_name", req.getMeasure_name()).orderByDesc("create_time").last("limit 1");
        Measure measureBy = measureMapper.selectOne(measureQueryWrapper);
        if (measureBy != null) {
            if (measureBy.getNumber() != null) {
                number += measureBy.getNumber();
            }
        }
        marking.setNumber(number);
        // 添加数据库，添加后返回自增id
        measureMapper.insert(marking);

        Properties properties = measureMapper.selectBy(marking.getMeasureId());
        Features features = MarkingUtils.socketData(annotationId, marking.getContour(), properties);
        // 如果是点类型，返回点的总数并返回
        List<PointCount> pointCountList = updatePoint(marking.getLocationType(), marking);
        BroadcastVO broadcastVO = SendMessage.sendListMessages(CommonConstant.ANNO_TYPE_MEASURE, ADD_STATUS, features, pointCountList);
        NioWebSocketHandler.sendAll(req.getSlide_id(), broadcastVO);
        return marking.getMeasureId();
    }


    @Override
    public double operationCheck(UpdateOperationIn req) throws Exception {
        Measure markingBy = measureMapper.selectById(req.getMarking_id());
        // 查询数据是否存在
        if (!Optional.ofNullable(markingBy).isPresent()) {
            throw new Exception(MessageSource.M("NO_ANNOTATION_DATA"));
        }
        return MarkingUtils.updateOperationVerify(markingBy.getContour(), req.getGeometry(), req.getOperation());
    }

    @Override
    public JSONObject updateOperation(UpdateOperationIn req) throws Exception {
        Measure measureBy = measureMapper.selectById(req.getMarking_id());
        // 查询数据是否存在
        if (!Optional.ofNullable(measureBy).isPresent()) {
            throw new Exception(MessageSource.M("NO_ANNOTATION_DATA"));
        }
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
//        Measure measureBys = MarkingUtils.updateVerify(measureBy.getContour(), req.getGeometry(), req.getOperation(), req.getCheck(), req.getResolution());
//        if(measureBys.getException() != null){
//            throw new Exception(measureBys.getException());
//        }
//        JSONObject jsonObject = JSONObject.parseObject(WktUtil.wktToJson(measureBys.getData()));
        Measure measure = new Measure();
//        measure.setContour(jsonObject);
//        measure.setArea(measureBys.getArea());
//        measure.setPerimeter(measureBys.getPerimeter());
        measure.setMeasureId(req.getMarking_id());
        measure.setUpdateBy(sysUser.getUserId());
        measure.setUpdateTime(new Date());
        measureMapper.updateById(measure);
        // 更新后查询数据并返回
        Properties properties = measureMapper.selectBy(req.getMarking_id());
        Features features = MarkingUtils.socketData(measureBy.getAnnotationId(), measure.getContour(), properties);
        BroadcastVO broadcastVO = SendMessage.sendOneMessagesByAnnoType(CommonConstant.ANNO_TYPE_MEASURE, UPDATE_STATUS, features);
        NioWebSocketHandler.sendAll(measureBy.getSlideId(), broadcastVO);
        return new JSONObject();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long update(MarkingUpdateIn req) throws Exception {
        Measure measureBy = measureMapper.selectById(req.getMarking_id());
        if (!Optional.ofNullable(measureBy).isPresent()) {
            throw new Exception(MessageSource.M("NO_ANNOTATION_DATA"));
        }
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        Slide slide = slideMapper.selectById(req.getSlide_id());
        if (!Optional.ofNullable(slide).isPresent()) {
            throw new Exception(MessageSource.M("NO_SLIDE_DATA"));
        }
        // 更新前数据
        Measure measure = updaeTrans2Marking(req);
        if (null != req.getUpdate_by()) {
            measure.setUpdateBy(req.getUpdate_by());
        } else {
            measure.setUpdateBy(SecurityUtils.getUserId());
        }
        measure.setUpdateTime(new Date());
        measure.setPerimeter(req.getPerimeter());
        measure.setArea(req.getArea());
        List<PointCount> pointCountList = updatePoint(measureBy.getLocationType(), measureBy);
        // 修改轮廓时，轮廓为空
        if (req.getCategory_id() == null && req.getDescription() == null) {
            if (req.getGeometry() != null) {
                if (req.getGeometry().isEmpty()) {
                    throw new Exception("更新失败，轮廓数据不能为空");
                }
            } else {
                throw new Exception("修改标注数据异常，更新失败");
            }
        }
        measureMapper.updateById(measure);
        // 判断标签
        if (req.getCategory_id() != null) {
            if (req.getCategory_id() != 0 && !req.getCategory_id().equals(measureBy.getCategoryId())) {
                measureBy.setCategoryId(req.getCategory_id());
                List<PointCount> newPointCountList = updatePoint(measureBy.getLocationType(), measureBy);
                pointCountList = Stream.of(pointCountList, newPointCountList).flatMap(Collection::stream).collect(Collectors.toList());
            }
        }
        Properties properties = measureMapper.selectBy(measure.getMeasureId());
        Features features = MarkingUtils.socketData(measureBy.getAnnotationId(), req.getGeometry(), properties);
        BroadcastVO broadcastVO = SendMessage.sendListMessages(CommonConstant.ANNO_TYPE_MEASURE, UPDATE_STATUS, features, pointCountList);
        // 使用websocket发送数据
        NioWebSocketHandler.sendAll(measureBy.getSlideId(), broadcastVO);
        return measure.getMeasureId();
    }


    @Override
    public void execlExport(Long slideId, HttpServletResponse response) throws Exception {
        // 构造表头的每个列头 定义表头
        List<Map<String, String>> titleList = getTitleList(CommonConstant.MEASURE_COLHEAD_KEY, CommonConstant.MEASURE_COLHEAD_VALUE);
        // 查询当前切片不为点类型的标注数据
        List<Properties> propertiesList = measureMapper.selectMeasureList(slideId);
        // 加点的记录
        QueryWrapper<Measure> markingQueryWrapper = new QueryWrapper<>();
        markingQueryWrapper.eq("slide_id", slideId).eq("location_type", "Point");
        int marking = measureMapper.selectCount(markingQueryWrapper);
        Properties properties = new Properties();
        properties.setPoint_count(marking);
        properties.setMeasure_name("P");
        propertiesList.add(properties);
        // 生成excel文件
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







    public List<PointCount> updatePoint(String locationType, Measure marking) {
        List<PointCount> pointCountList = new ArrayList<>();
        if (Objects.equals(locationType, "Point")) {
            PointCount pointCounts = measureMapper.selectCategoryCount(marking);
            marking.setPointCount(pointCounts.getPoint_count().intValue());
            measureMapper.updatePointCount(marking);
            pointCountList.add(pointCounts);
        }
        return pointCountList;
    }


    private Measure trans2Marking(ViewAddIn view) {
        Measure marking = new Measure();
        marking.setSlideId(view.getSlide_id());
        if (null != view.getCreate_by()) {
            marking.setCreateBy(view.getCreate_by());
        }
        if (StringUtils.isNotEmpty(view.getArea())) {
            marking.setArea(view.getArea());
        }
        if (StringUtils.isNotEmpty(view.getPerimeter())) {
            marking.setPerimeter(view.getPerimeter());
        }
        if (null != view.getCategory_id()) {
            marking.setCategoryId(view.getCategory_id());
        }
        if (StringUtils.isNotEmpty(view.getLocation_type())) {
            marking.setLocationType(view.getLocation_type());
        }
        if (StringUtils.isNotEmpty(view.getDescription())) {
            marking.setDescription(view.getDescription());
        }
        if (null != view.getGeometry()) {
            marking.setContour(view.getGeometry());
        }
        if (null != view.getMeasure_type()) {
            marking.setMeasureType(view.getMeasure_type());
        }
        if (StringUtils.isNotEmpty(view.getMeasure_relation())) {
            marking.setMeasureRelation(view.getMeasure_relation());
        }
        if (StringUtils.isNotEmpty(view.getMeasure_name())) {
            marking.setMeasureName(view.getMeasure_name());
        }
        if (null != view.getMeasure_number()) {
            marking.setMeasureNumber(view.getMeasure_number());
        }
        if (StringUtils.isNotEmpty(view.getRadius())) {
            marking.setRadius(view.getRadius());
        }

        if (null != view.getMean_distance()) {
            marking.setMeanDistance(view.getMean_distance());
        }
        if (null != view.getMax_distance()) {
            marking.setMaxDistance(view.getMax_distance());
        }
        if (null != view.getMin_distance()) {
            marking.setMinDistance(view.getMin_distance());
        }
        if (StringUtils.isNotEmpty(view.getInner_angle())) {
            marking.setInnerAngle(view.getInner_angle());
        }
        if (StringUtils.isNotEmpty(view.getExterior_angle())) {
            marking.setExteriorAngle(view.getExterior_angle());
        }
        if (StringUtils.isNotEmpty(view.getCenter_point())) {
            marking.setCenterPoint(view.getCenter_point());
        }
        return marking;
    }




    private Measure updaeTrans2Marking(MarkingUpdateIn view) {
        Measure marking = new Measure();
        marking.setMeasureId(view.getMarking_id());
        if (null != view.getUpdate_by()) {
            marking.setUpdateBy(view.getUpdate_by());
        }
        if (StringUtils.isNotEmpty(view.getArea())) {
            marking.setArea(view.getArea());
        }
        if (StringUtils.isNotEmpty(view.getPerimeter())) {
            marking.setPerimeter(view.getPerimeter());
        }
        if (null != view.getCategory_id()) {
            marking.setCategoryId(view.getCategory_id());
        }
        if (StringUtils.isNotEmpty(view.getLocation_type())) {
            marking.setLocationType(view.getLocation_type());
        }
        if (StringUtils.isNotEmpty(view.getDescription())) {
            marking.setDescription(view.getDescription());
        }

        if (null != view.getGeometry()) {
            marking.setContour(view.getGeometry());
        }
        if (null != view.getMeasure_type()) {
            marking.setMeasureType(view.getMeasure_type().intValue());
        }

        if (StringUtils.isNotEmpty(view.getMeasure_relation())) {
            marking.setMeasureRelation(view.getMeasure_relation());
        }
        if (StringUtils.isNotEmpty(view.getMeasure_name())) {
            marking.setMeasureName(view.getMeasure_name());
        }
        if (null != view.getMeasure_number()) {
            marking.setMeasureNumber(view.getMeasure_number().intValue());
        }
        if (StringUtils.isNotEmpty(view.getRadius())) {
            marking.setRadius(view.getRadius());
        }

        if (null != view.getMean_distance()) {
            marking.setMeanDistance(view.getMean_distance());
        }
        if (null != view.getMax_distance()) {
            marking.setMaxDistance(view.getMax_distance());
        }
        if (null != view.getMin_distance()) {
            marking.setMinDistance(view.getMin_distance());
        }
        if (StringUtils.isNotEmpty(view.getInner_angle())) {
            marking.setInnerAngle(view.getInner_angle());
        }
        if (StringUtils.isNotEmpty(view.getExterior_angle())) {
            marking.setExteriorAngle(view.getExterior_angle());
        }
        if (StringUtils.isNotEmpty(view.getCenter_point())) {
            marking.setCenterPoint(view.getCenter_point());
        }
        return marking;
    }


}




