package cn.staitech.fr.service.strategy.json.impl;

import cn.hutool.core.date.DateUtil;
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
 * @Author wudi
 * @Date 2024/5/13 10:05
 * @desc 精囊腺
 */
@Slf4j
@Component("Seminal_vesicles")
public class SeminalVesicleGlandParserStrategyImpl extends AbstractCustomParserStrategy {

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
        log.info("SeminalVesicleGlandParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("精囊腺结构指标计算开始");
        // 查询所有未被删除且登录机构相同的数据
        Map<String, Long> pathologicalMap = commonJsonParser.getPathologicalMap(jsonTask.getOrganizationId());

        // 定位表
        Long sequenceNumber = commonJsonParser.getSequenceNumber(jsonTask.getSpecialId());

        //组织轮廓面积
        List<AiForecast> insertEntity = new ArrayList<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        //面积
        AiForecast aiForecast = new AiForecast();
        aiForecast.setQuantitativeIndicators("精囊腺面积");
        aiForecast.setQuantitativeIndicatorsEn("Seminal vesicle area");
        aiForecast.setUnit("平方毫米");
        aiForecast.setResults(singleSlide.getArea());
        aiForecast.setSingleSlideId(jsonTask.getSingleId());
        aiForecast.setCreateTime(DateUtil.now());
        insertEntity.add(aiForecast);
        //腺上皮面积（全片）
        //查询切片缩放
        String resolution = singleSlideMapper.getImageId(jsonTask.getSlideId());
        BigDecimal bigDecimal = new BigDecimal("0.262");
        //计算结构面积
        Annotation annotation = new Annotation();
        annotation.setSingleSlideId(jsonTask.getSingleId());
        annotation.setCategoryId(pathologicalMap.get("12D074"));
        annotation.setSequenceNumber(sequenceNumber);
        Annotation structureArea = annotationMapper.getStructureArea(annotation);

        AiForecast aiForecast1 = new AiForecast();
        aiForecast1.setQuantitativeIndicators("腺上皮面积（全片）");
        aiForecast1.setQuantitativeIndicatorsEn("Acinar epithelial area (all)");
        aiForecast1.setUnit("平方毫米");
        aiForecast1.setSingleSlideId(jsonTask.getSingleId());
        aiForecast1.setCreateTime(DateUtil.now());
        if (StringUtils.isNotEmpty(resolution)) {
            bigDecimal = new BigDecimal(resolution);
        }
        if (StringUtils.isNotEmpty(structureArea.getArea())) {
            BigDecimal bigDecimal1 = new BigDecimal(structureArea.getArea());
            BigDecimal multiply = bigDecimal1.multiply(bigDecimal).multiply(bigDecimal).multiply(new BigDecimal(0.000001)).setScale(3, RoundingMode.HALF_UP);
            aiForecast1.setResults(multiply.toString());

        }
        insertEntity.add(aiForecast1);

        aiForecastService.saveBatch(insertEntity);

    }

    @Override
    public String getAlgorithmCode() {
        return "Seminal_vesicles";
    }
}
