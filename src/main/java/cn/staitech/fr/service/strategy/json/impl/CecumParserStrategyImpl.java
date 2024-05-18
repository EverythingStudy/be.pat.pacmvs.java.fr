package cn.staitech.fr.service.strategy.json.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.domain.AiForecast;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
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

/**
 * @Author
 * @Date 2024/5/13 10:05
 * @desc 大鼠结肠
 */
@Slf4j
@Component("Cecum")
public class CecumParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
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
        log.info("CecumParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("大鼠盲肠结构指标计算开始");
        // 查询所有未被删除且登录机构相同的数据
        Map<String, Long> pathologicalMap = commonJsonParser.getPathologicalMap(jsonTask.getOrganizationId());
        Long sequenceNumber = commonJsonParser.getSequenceNumber(jsonTask.getSpecialId());
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        String area = ObjectUtil.isNotEmpty(singleSlide) ? singleSlide.getArea() : "0";
        List<AiForecast> insertEntity = new ArrayList<>();
        AiForecast aiForecast = new AiForecast();
        aiForecast.setQuantitativeIndicators("盲肠面积");
        aiForecast.setQuantitativeIndicatorsEn("Cecum area");
        aiForecast.setUnit("平方毫米");
        aiForecast.setSingleSlideId(jsonTask.getSingleId());
        aiForecast.setCreateTime(DateUtil.now());
        if (ObjectUtil.isNotEmpty(pathologicalMap.get("114156"))) {
            Annotation annotation = new Annotation();
            annotation.setSingleSlideId(jsonTask.getSingleId());
            annotation.setCategoryId(pathologicalMap.get("114156"));
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
        return "Cecum";
    }
}
