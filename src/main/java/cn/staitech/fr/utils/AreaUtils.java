package cn.staitech.fr.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.PathologicalIndicatorCategory;
import cn.staitech.fr.domain.SpecialAnnotationRel;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.PathologicalIndicatorCategoryMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author wudi
 * @Date 2024/5/13 16:01
 * @desc
 */
@Component
public class AreaUtils {
    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private PathologicalIndicatorCategoryMapper pathologicalIndicatorCategoryMapper;
    @Resource
    private AnnotationMapper annotationMapper;
    @Resource
    private SingleSlideMapper singleSlideMapper;

    /**
     *
     * @param jsonTask
     * @param structure_id 结构组织id
     * @return 返回毫米
     */
    public String getArea(JsonTask jsonTask,String structure_id){
        QueryWrapper<PathologicalIndicatorCategory> qw = new QueryWrapper<>();
        // 查询所有未被删除且登录机构相同的数据
        qw.eq("del_flag", 0).eq("organization_id", jsonTask.getOrganizationId());
        List<PathologicalIndicatorCategory> list = pathologicalIndicatorCategoryMapper.selectList(qw);
        Map<String, Long> pathologicalMap = list.stream().collect(Collectors.toMap(PathologicalIndicatorCategory::getStructureId, PathologicalIndicatorCategory::getCategoryId, (entity1, entity2) -> entity1));
        //定位表
        QueryWrapper<SpecialAnnotationRel> wrapper = new QueryWrapper<>();
        wrapper.eq("special_id", jsonTask.getSpecialId());
        SpecialAnnotationRel annotationRel = specialAnnotationRelMapper.selectOne(wrapper);
        Long sequenceNumber = annotationRel.getSequenceNumber();

        //查询切片缩放
        String resolution = singleSlideMapper.getImageId(jsonTask.getSlideId());

        BigDecimal bigDecimalA = new BigDecimal(0.262);
        if (ObjectUtil.isNotEmpty(pathologicalMap.get(structure_id))) {
            Annotation annotation = new Annotation();
            annotation.setSingleSlideId(jsonTask.getSingleId());
            annotation.setCategoryId(pathologicalMap.get(structure_id));
            annotation.setSequenceNumber(sequenceNumber);
            Annotation structureArea = annotationMapper.getStructureArea(annotation);
            if (StringUtils.isNotEmpty(resolution) && StringUtils.isNotEmpty(structureArea.getArea())) {
                BigDecimal bigDecimal = new BigDecimal(resolution);
                BigDecimal bigDecimal1 = new BigDecimal(structureArea.getArea());
                bigDecimalA = bigDecimal1.multiply(bigDecimal).multiply(bigDecimal).multiply(new BigDecimal(0.000001));

            }
        }
        return bigDecimalA.toString();
    }
    public Integer count(JsonTask jsonTask,String structure_id){
        QueryWrapper<PathologicalIndicatorCategory> qw = new QueryWrapper<>();
        // 查询所有未被删除且登录机构相同的数据
        qw.eq("del_flag", 0).eq("organization_id", jsonTask.getOrganizationId());
        List<PathologicalIndicatorCategory> list = pathologicalIndicatorCategoryMapper.selectList(qw);
        Map<String, Long> pathologicalMap = list.stream().collect(Collectors.toMap(PathologicalIndicatorCategory::getStructureId, PathologicalIndicatorCategory::getCategoryId, (entity1, entity2) -> entity1));
        //定位表
        QueryWrapper<SpecialAnnotationRel> wrapper = new QueryWrapper<>();
        wrapper.eq("special_id", jsonTask.getSpecialId());
        SpecialAnnotationRel annotationRel = specialAnnotationRelMapper.selectOne(wrapper);
        Long sequenceNumber = annotationRel.getSequenceNumber();
        //乳腺腺泡和导管数量
        Annotation annotation1 = new Annotation();
        annotation1.setSingleSlideId(jsonTask.getSingleId());
        annotation1.setCategoryId(pathologicalMap.get(structure_id));
        annotation1.setSequenceNumber(sequenceNumber);
        Integer result = annotationMapper.countDucts(annotation1);
        return result;

    }
}
