package cn.staitech.fr.service.strategy.json;

import cn.staitech.fr.domain.*;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.PathologicalIndicatorCategoryMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: wangfeng
 * @create: 2024-05-16 16:51:08
 * @Description:
 */
@Service
public class CommonParserStrategy {

    @Resource
    private PathologicalIndicatorCategoryMapper pathologicalIndicatorCategoryMapper;

    @Resource
    private SpecialAnnotationRelMapper specialAnnotationRelMapper;

    @Resource
    private AnnotationMapper annotationMapper;

    @Resource
    private SingleSlideMapper singleSlideMapper;

    /**
     * @param jsonTask
     * @param jsonFileS
     */
    public void parseJson(JsonTask jsonTask, JsonFile jsonFileS) {

    }

    /**
     * 查询所有未被删除且登录机构相同的数据
     *
     * @param jsonTask
     * @return
     */
    public Map<String, Long> getPathologicalMap(JsonTask jsonTask) {
        QueryWrapper<PathologicalIndicatorCategory> qw = new QueryWrapper<>();
        // 查询所有未被删除且登录机构相同的数据
        qw.eq("del_flag", 0).eq("organization_id", jsonTask.getOrganizationId());
        List<PathologicalIndicatorCategory> list = pathologicalIndicatorCategoryMapper.selectList(qw);
        Map<String, Long> pathologicalMap = list.stream().collect(
                Collectors.toMap(
                        PathologicalIndicatorCategory::getStructureId,
                        PathologicalIndicatorCategory::getCategoryId,
                        (entity1, entity2) -> entity1));
        return pathologicalMap;
    }

    /**
     * 定位表
     *
     * @param jsonTask
     * @return
     */
    public Long getSequenceNumber(JsonTask jsonTask) {
        LambdaQueryWrapper<SpecialAnnotationRel> SpecialQueryWrapper = new LambdaQueryWrapper<>();
        SpecialQueryWrapper.eq(SpecialAnnotationRel::getSpecialId, jsonTask.getSpecialId());
        SpecialAnnotationRel annotationRel = specialAnnotationRelMapper.selectOne(SpecialQueryWrapper);
        Long sequenceNumber = annotationRel.getSequenceNumber();
        return sequenceNumber;
    }

    /**
     * 获取脏器轮廓面积
     *
     * @param jsonTask
     * @param structCode
     * @return
     */
    public BigDecimal getorganArea(JsonTask jsonTask, String structCode) {
        // 查询所有未被删除且登录机构相同的数据
        Map<String, Long> pathologicalMap = getPathologicalMap(jsonTask);
        // 定位表
        Long sequenceNumber = getSequenceNumber(jsonTask);

        // 非精细轮廓总面积
        Annotation annotation = new Annotation();
        annotation.setSequenceNumber(sequenceNumber);
        annotation.setSingleSlideId(jsonTask.getSingleId());//单脏器切片id
        annotation.setCategoryId(pathologicalMap.get(structCode));// 标注类别ID
        Annotation structure = annotationMapper.getStructureArea(annotation);
        if (structure == null || structure.getArea() == null) {
            return new BigDecimal("0");
        }
        String structureArea = structure.getArea();

        // 查询切片缩放
        BigDecimal resolutionNum = new BigDecimal("0.262");
        String resolution = singleSlideMapper.getImageId(jsonTask.getSlideId());
        if (StringUtils.isNotEmpty(resolution)) {
            resolutionNum = new BigDecimal(resolution);
        }

        // 计算面积
        BigDecimal organArea = BigDecimal.ZERO;
        if (StringUtils.isNotEmpty(structureArea)) {
            BigDecimal structureAreaNum = new BigDecimal(structureArea);
            organArea = structureAreaNum.multiply(resolutionNum).multiply(resolutionNum).multiply(new BigDecimal(0.000001));
        }
        return organArea;
    }
}
