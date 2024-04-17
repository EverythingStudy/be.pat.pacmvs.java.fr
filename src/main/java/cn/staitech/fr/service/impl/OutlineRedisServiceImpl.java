package cn.staitech.fr.service.impl;


import cn.staitech.common.redis.service.RedisService;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.service.OutlineService;
import cn.staitech.fr.utils.MarkingUtils;
import cn.staitech.fr.vo.outline.OutlineRoot;
import cn.staitech.fr.vo.outline.OutlineSelectVO;
import cn.staitech.fr.vo.outline.OutlineStatistic;
import cn.staitech.system.api.domain.SysUser;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


/**
 * (Outline)表服务实现类
 *
 * @author wangfeng
 * @since 2024-01-04 10:55:03
 */
@Slf4j
@Service("OutlineRedisServiceImpl")
public class OutlineRedisServiceImpl extends ServiceImpl<OutlineMapper, Outline> implements OutlineService {
    @Resource
    private AnnotationMapper annotationMapper;
    @Resource
    private RedisService redisService;
    @Resource
    private SlideMapper slideMapper;
    @Resource
    private UserMapper userMapper;

    @Resource
    private PathologicalIndicatorCategoryMapper pathologicalIndicatorCategoryMapper;

    public static final String REDIS_OUTLINE_ROOT = "OUTLINE_ROOT:";
    public static final String REDIS_OUTLINE_LIST = "OUTLINE_LIST:";

    /**
     * 列表查询
     *
     * @param selectVO
     * @return
     */
    @Override
    public List<Outline> selectList(OutlineSelectVO selectVO) {
        Long createBy = selectVO.getCreateBy();
        String rootKey = REDIS_OUTLINE_ROOT + createBy;
        String listKey = REDIS_OUTLINE_LIST + createBy + "_";

        com.alibaba.fastjson2.JSONObject object = redisService.getCacheObject(rootKey);

        if (object.isEmpty()) {
            return null;
        }

        OutlineRoot outlineRoot = object.toJavaObject(OutlineRoot.class);
        List<com.alibaba.fastjson2.JSONObject> srcJsonList = redisService.getCacheList(listKey + outlineRoot.getToken());
        if (CollectionUtils.isEmpty(srcJsonList)) {
            return null;
        }

        List<Outline> srcList = new ArrayList<>();

        AtomicLong outlineId = new AtomicLong(1);
        // 格式转换
        if(selectVO.getSingleSlideId() != null){
            selectVO.setSlideId(selectVO.getSingleSlideId());
        }
        for (com.alibaba.fastjson2.JSONObject jsonObject : srcJsonList) {
            Outline outline = new Outline();
            outline.setOutlineId(outlineId.getAndIncrement());
            outline.setProjectId(selectVO.getProjectId());
            outline.setImageId(selectVO.getImageId());
            outline.setSlideId(selectVO.getSlideId());
            outline.setCreateBy(selectVO.getCreateBy());
            outline.setArea(Double.valueOf(jsonObject.getBigDecimal("area").toString()));
            outline.setPerimeter(Double.valueOf(jsonObject.getBigDecimal("perimeter").toString()));
            outline.setLongAxis(Double.valueOf(jsonObject.getBigDecimal("longAxis").toString()));
            outline.setShortAxis(Double.valueOf(jsonObject.getBigDecimal("shortAxis").toString()));
            outline.setGeometry(JSONObject.parseObject(jsonObject.getString("geometry")));

            srcList.add(outline);
        }

        Double minVal = selectVO.getMinVal() != null ? selectVO.getMinVal() : 0.0;

        List<Outline> list;

        // 默认查询面积(面积1，周长2)
        if (selectVO.getBizType().equals(2)) {
            if (selectVO.getMaxVal() != null) {
                list = srcList.stream().filter(outline -> outline.getPerimeter() >= minVal).filter(outline -> outline.getPerimeter() <= selectVO.getMaxVal()).collect(Collectors.toList());
            } else {
                list = srcList.stream().filter(outline -> outline.getPerimeter() >= minVal).collect(Collectors.toList());
            }
        } else {
            if (selectVO.getMaxVal() != null) {
                list = srcList.stream().filter(outline -> outline.getArea() >= minVal).filter(outline -> outline.getArea() <= selectVO.getMaxVal()).collect(Collectors.toList());
            } else {
                list = srcList.stream().filter(outline -> outline.getArea() >= minVal).collect(Collectors.toList());
            }
        }
        return list;
    }

    /**
     * 统计
     *
     * @param list
     * @param bizType
     * @return
     */
    @Override
    public OutlineStatistic statistic(List<Outline> list, Integer bizType) {
        // 查询业务类型：1面积(默认),2周长
        bizType = bizType != null ? bizType : 1;

        // 平均值
        double average;
        // 标准偏差
        Double standardDeviation;
        // 总和
        Double sum;
        // 总个数
        Integer total = list.size();
        // 最小值
        Double minValue;
        // 最大值
        Double maxValue;

        List<Double> doubles;
        // 默认查询面积
        if (bizType.equals(2)) {
            doubles = list.stream().map(Outline::getPerimeter).collect(Collectors.toList());
        } else {
            doubles = list.stream().map(Outline::getArea).collect(Collectors.toList());
        }

        // 对周长或面积求和、最小值、最大值
        sum = doubles.stream().mapToDouble(num -> num).sum();
        minValue = doubles.stream().mapToDouble(num -> num).min().getAsDouble();
        maxValue = doubles.stream().mapToDouble(num -> num).max().getAsDouble();

        // 平均值
        average = sum / total;

        // 对面积、周长求方差
        double sumOfSquares = doubles.stream().mapToDouble(db -> Math.pow((db - average), 2)).sum();

        // 母体方差
        double variance = sumOfSquares / total;
        // 样本方差
        // double variance = sumOfSquares / (total - 1);

        // 标准差
        standardDeviation = Math.sqrt(variance);

        OutlineStatistic statistic = new OutlineStatistic();
        statistic.setList(list);
        statistic.setBizType(bizType);
        statistic.setAverage(average);
        statistic.setStandardDeviation(standardDeviation);
        statistic.setSum(sum);
        statistic.setTotal(total);
        statistic.setMinValue(minValue);
        statistic.setMaxValue(maxValue);

        return statistic;
    }

    /**
     * 异步删除所有当前用户、非当前token的记录
     *
     * @param createBy 用户ID
     * @param token    用户token
     */
    @Async
    @Override
    public void removeByCreateByAndToken(Long createBy, String token) {
        String rootKey = REDIS_OUTLINE_ROOT + createBy;
        String listKey = REDIS_OUTLINE_LIST + createBy + "_";

        com.alibaba.fastjson2.JSONObject object = redisService.getCacheObject(rootKey);
        OutlineRoot outlineRoot = object.toJavaObject(OutlineRoot.class);

        // 当前用户所有数据的Key
        Collection<String> keyCollection = redisService.keys(listKey + "*");
        if (CollectionUtils.isEmpty(keyCollection)) {
            return;
        }

        if (token != null) {
            // 清空当前用户非当前token的数据
            for (String keyStr : keyCollection) {
                if (!keyStr.equals(listKey + outlineRoot.getToken())) {
                    redisService.deleteObject(keyStr);
                }
            }
        } else {
            // 清空当前用户全部数据
            redisService.deleteObject(keyCollection);
        }
    }

    /**
     * 异步删除所有当前用户、非当前slideId的记录
     *
     * @param createBy 用户ID
     * @param slideId  SlideID
     */
    @Async
    @Override
    public void removeBycreateBySlideId(Long createBy, Long slideId) {
        String rootKey = REDIS_OUTLINE_ROOT + createBy;
        String listKey = REDIS_OUTLINE_LIST + createBy + "_";

        com.alibaba.fastjson2.JSONObject object = redisService.getCacheObject(rootKey);
        OutlineRoot outlineRoot = object.toJavaObject(OutlineRoot.class);
        // 当前用户所有数据的Key
        Collection<String> keyCollection = redisService.keys(listKey + "*");
        if (CollectionUtils.isEmpty(keyCollection)) {
            return;
        }
        if (outlineRoot.getSingleSlideId().equals(slideId)) {
            // 清空当前用户非当前token的数据
            for (String keyStr : keyCollection) {
                if (!keyStr.equals(listKey + outlineRoot.getToken())) {
                    redisService.deleteObject(keyStr);
                }
            }
        } else {
            // 清空当前用户全部数据
            redisService.deleteObject(keyCollection);
        }
    }

    /**
     * 批量保存
     *
     * @param list
     * @param selectVO
     */
    @Async
    @Override
    public void saveAll(List<Outline> list, OutlineSelectVO selectVO) {
        Long categoryId = selectVO.getCategoryId();
        Long createBy = selectVO.getCreateBy();

        // 逐一添加
        for (Outline outline : list) {
            try {
//                annotationMapper.insertOutline(outline, slide, user, categoryId);
//                @Override
//                @Transactional(rollbackFor = Exception.class)
//                public String insertOutline(Outline outline, cn.staitech.anno.project.domain.Slide slide, SysUser user, Long categoryId) throws Exception {

                Annotation annotation = new Annotation();
                String id = null;
                if (categoryId != null) {
                    PathologicalIndicatorCategory pathologicalIndicatorCategory = pathologicalIndicatorCategoryMapper.selectById(categoryId);
                    if (pathologicalIndicatorCategory != null) {
                        id = MarkingUtils.getSdId(pathologicalIndicatorCategory.getCategoryName());
                    }
                } else {
                    id = MarkingUtils.getSdId(null);
                }
                annotation.setId(id);
                annotation.setCategoryId(categoryId);
                annotation.setContour(String.valueOf(outline.getGeometry()));
                annotation.setSingleSlideId(outline.getSlideId());
                annotation.setArea(outline.getArea().toString());
                annotation.setSingleSlideId(selectVO.getSingleSlideId());
                annotation.setPerimeter(outline.getPerimeter().toString());
                annotation.setCreateBy(outline.getCreateBy());
                annotation.setAnnotationType("Draw");
                annotation.setCreateTime(String.valueOf(new Date()));
                annotation.setSingle(1);
                annotation.setContourType(2L);
                // 添加数据库，添加后返回自增id
                annotationMapper.insert(annotation);

            } catch (Exception e) {
                log.info("save marking error：{} {} {}", e, outline.getOutlineId(), outline.getGeometry());
            }
        }

        // WebSocket广播
//        markingService.reload(slideId);
        // 删除所有当前用户的记录
        removeByCreateByAndToken(createBy, null);
    }
}
