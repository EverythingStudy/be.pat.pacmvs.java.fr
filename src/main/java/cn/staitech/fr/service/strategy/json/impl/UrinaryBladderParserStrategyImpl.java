package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.domain.*;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 膀胱
 */
@Slf4j
@Service("Urinary_bladder")
public class UrinaryBladderParserStrategyImpl extends AbstractCustomParserStrategy {
    @Resource
    private SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private PathologicalIndicatorCategoryMapper pathologicalIndicatorCategoryMapper;
    @Resource
    private AnnotationMapper annotationMapper;
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private ImageMapper imageMapper;

    @PostConstruct
    public void init() {
        setAiForecastService(aiForecastService);
        setAnnotationMapper(annotationMapper);
        setPathologicalIndicatorCategoryMapper(pathologicalIndicatorCategoryMapper);
        setSingleSlideMapper(singleSlideMapper);
        setSpecialAnnotationRelMapper(specialAnnotationRelMapper);
        setImageMapper(imageMapper);
        log.info("UrinaryBladderParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        /*
        indicatorResultsMap.put("黏膜上皮面积占比", new IndicatorAddIn("Mucosa epithelium area %", "", ""));
        indicatorResultsMap.put("黏膜固有层和黏膜下层面积占比", new IndicatorAddIn("Lamina propria and submucosa area %", "", ""));
        indicatorResultsMap.put("黏膜上皮细胞核密度", new IndicatorAddIn("Nucleus density of mucosal epithelial nucleus", "", ""));
        indicatorResultsMap.put("血管面积占比", new IndicatorAddIn("Vessel area %", "", ""));
        indicatorResultsMap.put("血管外红细胞面积占比", new IndicatorAddIn("Extravascular erythrocyte area%", "", ""));
        indicatorResultsMap.put("血管内红细胞面积占比", new IndicatorAddIn("Intravascular erythrocyte area%", "", ""));
        */

        // B 精细轮廓总面积-平方毫米
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        String accurateArea = singleSlide.getArea();

        // A 膀胱腔面积-平方毫米
        BigDecimal organArea = getorganArea(jsonTask);

        // 膀胱面积 B-A
        BigDecimal areaNum = new BigDecimal(accurateArea).subtract(organArea);
        String result = areaNum.setScale(3, RoundingMode.HALF_UP).toString();

        indicatorResultsMap.put("膀胱面积", new IndicatorAddIn("Urinary bladder area", result, "平方毫米"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Urinary_bladder";
    }

    /**
     * 获取脏器轮廓面积
     */
    private BigDecimal getorganArea(JsonTask jsonTask) {
        // 查询所有未被删除且登录机构相同的数据
        LambdaQueryWrapper<PathologicalIndicatorCategory> CategoryQueryWrapper = new LambdaQueryWrapper<>();
        CategoryQueryWrapper.eq(PathologicalIndicatorCategory::getDelFlag, 0)
                .eq(PathologicalIndicatorCategory::getOrganizationId, jsonTask.getOrganizationId());
        List<PathologicalIndicatorCategory> list = pathologicalIndicatorCategoryMapper.selectList(CategoryQueryWrapper);
        Map<String, Long> pathologicalMap = list.stream().collect(Collectors.toMap(PathologicalIndicatorCategory::getStructureId, PathologicalIndicatorCategory::getCategoryId, (entity1, entity2) -> entity1));

        // 定位表
        LambdaQueryWrapper<SpecialAnnotationRel> SpecialQueryWrapper = new LambdaQueryWrapper<>();
        SpecialQueryWrapper.eq(SpecialAnnotationRel::getSpecialId, jsonTask.getSpecialId());
        SpecialAnnotationRel annotationRel = specialAnnotationRelMapper.selectOne(SpecialQueryWrapper);
        Long sequenceNumber = annotationRel.getSequenceNumber();

        // 非精细轮廓总面积
        Annotation annotation = new Annotation();
        annotation.setSequenceNumber(sequenceNumber);
        annotation.setSingleSlideId(jsonTask.getSingleId());//单脏器切片id
        annotation.setCategoryId(pathologicalMap.get("11E034"));// 标注类别ID
        Annotation structure = annotationMapper.getStructureArea(annotation);
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
