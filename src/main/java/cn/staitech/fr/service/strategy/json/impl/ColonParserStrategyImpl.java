package cn.staitech.fr.service.strategy.json.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.PathologicalIndicatorCategoryMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author
 * @Date 2024/5/13 10:05
 * @desc 大鼠结肠
 */
@Slf4j
@Component("Colon")
public class ColonParserStrategyImpl extends AbstractCustomParserStrategy {

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

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        log.info("ColonParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("大鼠结肠结构指标计算开始");
        QueryWrapper<PathologicalIndicatorCategory> qw = new QueryWrapper<>();
        // 查询所有未被删除且登录机构相同的数据
        qw.eq("del_flag", 0).eq("organization_id", jsonTask.getOrganizationId());
        List<PathologicalIndicatorCategory> list = pathologicalIndicatorCategoryMapper.selectList(qw);
        Map<String, Long> pathologicalMap = list.stream().collect(Collectors.toMap(PathologicalIndicatorCategory::getStructureId, PathologicalIndicatorCategory::getCategoryId, (entity1, entity2) -> entity1));
        QueryWrapper<SpecialAnnotationRel> wrapper = new QueryWrapper<>();
        wrapper.eq("special_id", jsonTask.getSpecialId());
        SpecialAnnotationRel annotationRel = specialAnnotationRelMapper.selectOne(wrapper);
        Long sequenceNumber = annotationRel.getSequenceNumber();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        String area = ObjectUtil.isNotEmpty(singleSlide) ? singleSlide.getArea() : "0";
        List<AiForecast> insertEntity = new ArrayList<>();
        AiForecast aiForecast = new AiForecast();
        aiForecast.setQuantitativeIndicators("结肠面积");
        aiForecast.setQuantitativeIndicatorsEn("Colon area");
        aiForecast.setUnit("平方毫米");
        aiForecast.setSingleSlideId(jsonTask.getSingleId());
        aiForecast.setCreateTime(DateUtil.now());
        if (ObjectUtil.isNotEmpty(pathologicalMap.get("115156"))) {
            Annotation annotation = new Annotation();
            annotation.setSingleSlideId(jsonTask.getSingleId());
            annotation.setCategoryId(pathologicalMap.get("115156"));
            annotation.setSequenceNumber(sequenceNumber);
            Annotation structureArea = annotationMapper.getStructureArea(annotation);
            String area1 = StringUtils.isNotEmpty(structureArea.getArea()) ? structureArea.getArea() : "0";
            BigDecimal area2 = new BigDecimal(area1);
            BigDecimal decimal = new BigDecimal(area).subtract(area2).setScale(3, RoundingMode.HALF_UP);
            aiForecast.setResults(decimal.toString());
        }
        insertEntity.add(aiForecast);
        aiForecastService.saveBatch(insertEntity);

    }

    @Override
    public String getAlgorithmCode() {
        return "Colon";
    }
}
