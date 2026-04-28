package cn.staitech.fr.utils;

import cn.staitech.fr.domain.*;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.mapper.StructureTagMapper;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
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
    private AnnotationMapper annotationMapper;
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private StructureTagMapper structureTagMapper;

    /**
     * 获取组织轮廓面积
     *
     * @return 面积单位-平方毫米
     */
    public String getFineContourArea(Long singleSlideId) {
        SingleSlide singleSlide = singleSlideMapper.selectById(singleSlideId);
        if (null == singleSlide || StringUtils.isEmpty(singleSlide.getArea())) {
            return "0";
        }
        return singleSlide.getArea();
    }

    /**
     * 平方毫米换算为10³平方微米
     *
     * @param str 输入值
     * @return 转换后结果
     */
    public String convertToSquareMicrometer(String str) {
        BigDecimal result = BigDecimal.ZERO;
        if (!StringUtils.isEmpty(str)) {
            BigDecimal areaNum = new BigDecimal(str).multiply(BigDecimal.valueOf(1000));
            result = areaNum.setScale(3, BigDecimal.ROUND_HALF_UP);
        }
        return result.toString();
    }

    /**
     * 微末转10³平方微米
     *
     * @param str 输入值
     * @return 转换后结果
     */
    public String micrometerToSquareMicrometer(String str) {
        BigDecimal result = BigDecimal.ZERO;
        if (!StringUtils.isEmpty(str)) {
            result = new BigDecimal(str).divide(BigDecimal.valueOf(1000), 3, BigDecimal.ROUND_HALF_UP);
        }
        return result.toString();
    }

    /**
     * 平方毫米换算为平方微米
     *
     * @param str 输入值
     * @return 转换后结果
     */
    public String convertToMicrometer(String str) {
        BigDecimal result = BigDecimal.ZERO;
        if (!StringUtils.isEmpty(str)) {
            BigDecimal areaNum = new BigDecimal(str).multiply(BigDecimal.valueOf(1000000));
            result = areaNum.setScale(3, BigDecimal.ROUND_HALF_UP);
        }
        return result.toString();
    }


    public String convertToUm(String str) {
        BigDecimal result = BigDecimal.ZERO;
        if (!StringUtils.isEmpty(str)) {
            BigDecimal areaNum = new BigDecimal(str).multiply(BigDecimal.valueOf(0.001));
            result = areaNum.setScale(3, BigDecimal.ROUND_HALF_UP);
        }
        return result.toString();
    }

    public BigDecimal convertToUm(BigDecimal str) {
        BigDecimal result = BigDecimal.ZERO;
        if (str != null) {
            BigDecimal areaNum = str.multiply(BigDecimal.valueOf(0.001));
            result = areaNum.setScale(3, BigDecimal.ROUND_HALF_UP);
        }
        return result;
    }

    /**
     * 获取脏器轮廓面积
     *
     * @param jsonTask    jsonTask
     * @param structureId 结构ID
     * @return 脏器面积-平方毫米
     */
    public BigDecimal getOrganArea(JsonTask jsonTask, String structureId) {
        Annotation annotation = commonJsonParser.getOrganArea(jsonTask, structureId);
        return annotation.getStructureAreaNum();
    }

    /**
     * 取脏器轮廓数量
     *
     * @param jsonTask    jsonTask
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
        //单脏器切片id
        annotation.setSingleSlideId(jsonTask.getSingleId());
        // 标注类别ID
        annotation.setCategoryId(pathologicalMap.get(structureId));
        return annotationMapper.countDucts(annotation);

    }


    public static String formattedNumber(String res) {
        double value = Double.parseDouble(res);
        DecimalFormat df = new DecimalFormat("0.000");
        return df.format(value);
    }

    /**
     * 定位表
     *
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
     *
     * @param organizationId 机构id
     * @return 指标的结构ID和类别ID
     */
    Map<Long, Map<String, Long>> pathologicalHasMap = new HashMap<>();

    public Map<String, Long> getPathologicalMap(Long organizationId) {
        Map<String, Long> pathlogicalMap = pathologicalHasMap.get(organizationId);
        if (pathlogicalMap == null) {
            LambdaQueryWrapper<StructureTag> categoryQueryWrapper = new LambdaQueryWrapper<>();
            categoryQueryWrapper.eq(StructureTag::getDelFlag, 0).eq(StructureTag::getOrganizationId, organizationId);
            List<StructureTag> list = structureTagMapper.selectList(categoryQueryWrapper);
            pathlogicalMap = list.stream().collect(Collectors.toMap(StructureTag::getStructureId, StructureTag::getStructureTagId, (entity1, entity2) -> entity1));
            pathologicalHasMap.put(organizationId, pathlogicalMap);
            return pathlogicalMap;
        }
        return pathlogicalMap;
    }


    /**
     * @param @param  structureIdList
     * @param @return
     * @return String
     * @throws
     * @Title: getStructureIds
     * @Description: 获取结构
     */
    public String getStructureIds(String... structureId) {
        return structureId == null || structureId.length == 0 ? "" : String.join(",", structureId);
    }

}
