package cn.staitech.fr.service.strategy.json.impl.digestive;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.AiForecast;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author wudi
 * @Date 2024/5/13 10:05
 * @desc 乳腺-皮肤
 */
@Slf4j
@Component("Skin_mammary")
public class MammaryGlandParserStrategyImpl extends AbstractCustomParserStrategy {

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
        log.info("MammaryGlandParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("乳腺结构指标计算开始");
        //H-面积
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal h = new BigDecimal(0);
        if (ObjectUtil.isNotEmpty(singleSlide) && StringUtils.isNotEmpty(singleSlide.getArea())) {
            h = new BigDecimal(singleSlide.getArea());
        }
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        Integer organAreaCount = commonJsonParser.getOrganAreaCount(jsonTask, "12306C");
        //淋巴结A
        BigDecimal organAreaA = commonJsonParser.getOrganArea(jsonTask, "123005").getStructureAreaNum();
        //皮肤B
        BigDecimal organAreaB = commonJsonParser.getOrganArea(jsonTask, "1230C3").getStructureAreaNum();
        BigDecimal organArea1 = commonJsonParser.getOrganArea(jsonTask, "12306C").getStructureAreaNum();
        BigDecimal organArea2 = commonJsonParser.getOrganArea(jsonTask, "12303F").getStructureAreaNum();
        Integer organAreaCount2 = commonJsonParser.getOrganAreaCount(jsonTask, "1230C7");


        indicatorResultsMap.put("乳腺腺泡和导管数量", new IndicatorAddIn("Number of acinus and ducts", organAreaCount.toString(), "个"));
        indicatorResultsMap.put("乳腺面积", new IndicatorAddIn("Mammary gland area", h.subtract(organAreaA).subtract(organAreaB).setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米"));
        indicatorResultsMap.put("淋巴结面积", new IndicatorAddIn("Lymph node area", organAreaA.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("皮肤面积", new IndicatorAddIn("Skin area", organAreaB.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("乳腺腺泡/导管面积（全片）", new IndicatorAddIn("Breast acinar/ductal area (all)", organArea1.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("结缔组织面积", new IndicatorAddIn("Connective tissue area", organArea2.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("组织轮廓面积", new IndicatorAddIn("Organizational contour area", h.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("乳腺细胞核数量（全片）", new IndicatorAddIn("Number of breast cell nuclei (all)", organAreaCount2.toString(), "个", CommonConstant.NUMBER_1));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
        // 查询所有未被删除且登录机构相同的数据
        Map<String, Long> pathologicalMap = commonJsonParser.getPathologicalMap(jsonTask.getOrganizationId());
        //定位表
        Long sequenceNumber = commonJsonParser.getSequenceNumber(jsonTask.getSpecialId());
        Annotation annotation = new Annotation();
        annotation.setSingleSlideId(jsonTask.getSingleId());
        annotation.setSequenceNumber(sequenceNumber);
        List<AiForecast> insertEntity = new ArrayList<>();
        AiForecast aiForecast2 = new AiForecast();
        aiForecast2.setQuantitativeIndicators("皮肤面积");
        aiForecast2.setQuantitativeIndicatorsEn("Skin area");
        aiForecast2.setUnit("平方毫米");
        aiForecast2.setSingleSlideId(jsonTask.getSingleId());
        aiForecast2.setCreateTime(DateUtil.now());
        aiForecast2.setResults(organAreaB.setScale(3, RoundingMode.HALF_UP).toString());
        insertEntity.add(aiForecast2);
        AiForecast aiForecast3 = new AiForecast();
        aiForecast3.setQuantitativeIndicators("皮脂腺密度");
        aiForecast3.setQuantitativeIndicatorsEn("Density of Sebaceous glands");
        aiForecast3.setUnit("个/平方毫米");
        aiForecast3.setCreateTime(DateUtil.now());
        aiForecast3.setSingleSlideId(jsonTask.getSingleId());
        // 获取皮脂腺的数量
        annotation.setCategoryId(pathologicalMap.get("121099"));
        Integer i = annotationMapper.countDucts(annotation);
        BigDecimal decimal = new BigDecimal(i);
        aiForecast3.setResults(decimal.divide(organAreaB, 3, RoundingMode.HALF_UP) + "");
        insertEntity.add(aiForecast3);
        AiForecast aiForecast4 = new AiForecast();
        aiForecast4.setQuantitativeIndicators("毛囊密度");
        aiForecast4.setQuantitativeIndicatorsEn("Density of hair follicles");
        aiForecast4.setUnit("个/平方毫米");
        aiForecast4.setSingleSlideId(jsonTask.getSingleId());
        aiForecast4.setCreateTime(DateUtil.now());
        // 获取毛囊密度的数量
        annotation.setCategoryId(pathologicalMap.get("121098"));
        Integer i1 = annotationMapper.countDucts(annotation);
        BigDecimal decimal1 = new BigDecimal(i1);
        aiForecast4.setResults(decimal1.divide(organAreaB, 3, RoundingMode.HALF_UP) + "");
        insertEntity.add(aiForecast4);

        aiForecastService.saveBatch(insertEntity);
    }

    @Override
    public String getAlgorithmCode() {
        return "Skin_mammary";
    }
}
