package cn.staitech.fr.utils;

import cn.staitech.fr.domain.*;
import cn.staitech.fr.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
     * 获取组织轮廓面积
     * @return 面积单位-平方毫米
     */
    public String getFineContourArea(Long singleSlideId) {
        SingleSlide singleSlide = singleSlideMapper.selectById(singleSlideId);
        if(null == singleSlide || StringUtils.isEmpty(singleSlide.getArea())){
            return "0";
        }
        return singleSlide.getArea();
    }

    /**
     * 平方毫米换算为10³平方微米
     * @param str 输入值
     * @return 转换后结果
     */
    public String convertToSquareMicrometer(String str){
        BigDecimal result = BigDecimal.ZERO;
        if(StringUtils.isEmpty(str)){
            BigDecimal areaNum = new BigDecimal(str).multiply(new BigDecimal(1000));
            result = areaNum.setScale(3, RoundingMode.HALF_UP);
        }

        return result.toString();
    }

    /**
     * 获取脏器轮廓面积
     * @param jsonTask jsonTask
     * @param structureId 结构ID
     * @return 脏器面积-平方毫米
     */
    public BigDecimal getOrganArea(JsonTask jsonTask,String structureId) {
        // 查询所有未被删除且登录机构相同的数据
        Map<String, Long> pathologicalMap = getPathologicalMap(jsonTask.getOrganizationId());

        // 定位表
        Long sequenceNumber = getSequenceNumber(jsonTask.getSpecialId());

        // 脏器轮廓信息
        Annotation annotation = new Annotation();
        annotation.setSequenceNumber(sequenceNumber);
        annotation.setSingleSlideId(jsonTask.getSingleId());//单脏器切片id
        annotation.setCategoryId(pathologicalMap.get(structureId));// 标注类别ID
        Annotation structure = annotationMapper.getStructureArea(annotation);
        if(null == structure || StringUtils.isEmpty(structure.getArea())){
            return BigDecimal.ZERO;
        }

        // 查询切片缩放
        BigDecimal resolutionNum = new BigDecimal("0.262");
        String resolution = singleSlideMapper.getImageId(jsonTask.getSlideId());
        if (StringUtils.isNotEmpty(resolution)) {
            resolutionNum = new BigDecimal(resolution);
        }

        // 计算面积
        BigDecimal structureAreaNum = new BigDecimal(structure.getArea());
        return structureAreaNum.multiply(resolutionNum).multiply(resolutionNum).multiply(new BigDecimal(0.000001));
    }

    /**
     * 取脏器轮廓数量
     * @param jsonTask jsonTask
     * @param structureId 结构ID
     * @return 脏器轮廓数量
     */
    public Integer getOrganAreaCount(JsonTask jsonTask, String structureId) {
        // 查询所有未被删除且登录机构相同的数据
        Map<String, Long> pathologicalMap = getPathologicalMap(jsonTask.getOrganizationId());

        // 定位表
        Long sequenceNumber = getSequenceNumber(jsonTask.getSpecialId());

        // 脏器轮廓信息
        Annotation annotation = new Annotation();
        annotation.setSequenceNumber(sequenceNumber);
        annotation.setSingleSlideId(jsonTask.getSingleId());//单脏器切片id
        annotation.setCategoryId(pathologicalMap.get(structureId));// 标注类别ID
        return annotationMapper.countDucts(annotation);

    }

    /**
     * 定位表
     * @param specialId 专题ID
     * @return 表后缀
     */
    private Long getSequenceNumber(Long specialId) {
        LambdaQueryWrapper<SpecialAnnotationRel> SpecialQueryWrapper = new LambdaQueryWrapper<>();
        SpecialQueryWrapper.eq(SpecialAnnotationRel::getSpecialId, specialId);
        SpecialAnnotationRel annotationRel = specialAnnotationRelMapper.selectOne(SpecialQueryWrapper);
        return annotationRel.getSequenceNumber();
    }

    /**
     * 查询所有未被删除且登录机构相同的数据
     * @param organizationId 机构id
     * @return 指标的结构ID和类别ID
     */
    private Map<String, Long> getPathologicalMap(Long organizationId) {
        LambdaQueryWrapper<PathologicalIndicatorCategory> CategoryQueryWrapper = new LambdaQueryWrapper<>();
        CategoryQueryWrapper.eq(PathologicalIndicatorCategory::getDelFlag, 0)
                .eq(PathologicalIndicatorCategory::getOrganizationId, organizationId);
        List<PathologicalIndicatorCategory> list = pathologicalIndicatorCategoryMapper.selectList(CategoryQueryWrapper);

        return list.stream().collect(Collectors.toMap(
                PathologicalIndicatorCategory::getStructureId,
                PathologicalIndicatorCategory::getCategoryId,
                (entity1, entity2) -> entity1));
    }

}
