package cn.staitech.fr.service.strategy.json.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.PathologicalIndicatorCategoryMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.service.strategy.json.ParserStrategy;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author:
 * @create: 2024-05-10 14:18:48
 * @Description: Coagulating_glang Json Parser 大鼠凝固腺
 */
@Slf4j
@Component("Coagulating_glang")
public class CoagulatingGlangParserStrategyImpl implements ParserStrategy {

    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private PathologicalIndicatorCategoryMapper pathologicalIndicatorCategoryMapper;
    @Resource
    private AnnotationMapper annotationMapper;
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;


    @Override
    public void parseJson(JsonTask jsonTask, JsonFile jsonFileS) {
        commonJsonParser.parseJson(jsonTask, jsonFileS);
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        QueryWrapper<PathologicalIndicatorCategory> qw = new QueryWrapper<>();
        // 查询所有未被删除且登录机构相同的数据
        qw.eq("del_flag", 0).eq("organization_id", jsonTask.getOrganizationId());
        List<PathologicalIndicatorCategory> list = pathologicalIndicatorCategoryMapper.selectList(qw);
        Map<String, Long> pathologicalMap = list.stream().collect(Collectors.toMap(PathologicalIndicatorCategory::getStructureId, PathologicalIndicatorCategory::getCategoryId, (entity1, entity2) -> entity1));
        QueryWrapper<SpecialAnnotationRel> wrapper = new QueryWrapper<>();
        wrapper.eq("special_id", jsonTask.getSpecialId());
        SpecialAnnotationRel annotationRel = specialAnnotationRelMapper.selectOne(wrapper);
        Long sequenceNumber = annotationRel.getSequenceNumber();
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        indicatorResultsMap.put("大鼠凝固腺面积", new IndicatorAddIn("Coagulating gland area", singleSlide.getArea(), "平方毫米"));
        Annotation annotation = new Annotation();
        annotation.setSingleSlideId(jsonTask.getSingleId());
       // annotation.setSlideId(jsonTask.getSlideId());
        annotation.setCategoryId(pathologicalMap.get("12B074"));
        annotation.setSequenceNumber(sequenceNumber);
        Annotation structureArea = annotationMapper.getStructureArea(annotation);
        indicatorResultsMap.put("腺上皮面积（全片）", new IndicatorAddIn("Acinar epithelial area (all)", ObjectUtil.isNotEmpty(structureArea) ? structureArea.getArea() : "0", "平方毫米"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }
}
