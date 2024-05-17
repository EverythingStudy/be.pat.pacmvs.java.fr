package cn.staitech.fr.service.strategy.json.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.domain.AiForecast;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.PathologicalIndicatorCategory;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.SpecialAnnotationRel;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.ImageMapper;
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
        log.info("MammaryGlandParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("乳腺结构指标计算开始");
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
        annotation1.setCategoryId(pathologicalMap.get("12306C"));
        annotation1.setSequenceNumber(sequenceNumber);
        Integer result = annotationMapper.countDucts(annotation1);
        List<AiForecast> insertEntity = new ArrayList<>();
        AiForecast aiForecast = new AiForecast();
        aiForecast.setQuantitativeIndicators("乳腺腺泡和导管数量");
        aiForecast.setQuantitativeIndicatorsEn("Number of acinus and ducts");
        aiForecast.setUnit("个");
        aiForecast.setResults(result.toString());
        aiForecast.setSingleSlideId(jsonTask.getSingleId());
        aiForecast.setCreateTime(DateUtil.now());
        insertEntity.add(aiForecast);
        //乳腺面积=H-A-B
        //H
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());

        //查询切片缩放
        String resolution = singleSlideMapper.getImageId(jsonTask.getSlideId());
        BigDecimal resolutions = new BigDecimal("0.262");
        if(StringUtils.isNotEmpty(resolution)){
            resolutions= new BigDecimal(resolution);
        }

        //计算A面积
        BigDecimal bigDecimalA = new BigDecimal(0);
        if (ObjectUtil.isNotEmpty(pathologicalMap.get("123005"))) {
            Annotation annotation = new Annotation();
            annotation.setSingleSlideId(jsonTask.getSingleId());
            annotation.setCategoryId(pathologicalMap.get("123005"));
            annotation.setSequenceNumber(sequenceNumber);
            Annotation structureArea = annotationMapper.getStructureArea(annotation);
            if (StringUtils.isNotEmpty(structureArea.getArea())) {
                BigDecimal bigDecimal1 = new BigDecimal(structureArea.getArea());
                bigDecimalA = bigDecimal1.multiply(resolutions).multiply(resolutions).multiply(new BigDecimal(0.000001));
            }
        }
        Annotation annotation = new Annotation();
        annotation.setSingleSlideId(jsonTask.getSingleId());
        annotation.setCategoryId(pathologicalMap.get("1230C3"));
        annotation.setSequenceNumber(sequenceNumber);
        Annotation structureArea = annotationMapper.getStructureArea(annotation);
        BigDecimal bigDecimalB = new BigDecimal(0);
        if ( ObjectUtil.isNotEmpty(structureArea)&&StringUtils.isNotEmpty(structureArea.getArea())) {
            BigDecimal bigDecimal1 = new BigDecimal(structureArea.getArea());
            bigDecimalB = bigDecimal1.multiply(resolutions).multiply(resolutions).multiply(new BigDecimal(0.000001));
        }
        AiForecast aiForecast1 = new AiForecast();
        aiForecast1.setQuantitativeIndicators("乳腺面积");
        aiForecast1.setQuantitativeIndicatorsEn("Mammary gland area");
        aiForecast1.setUnit("平方毫米");
        aiForecast1.setSingleSlideId(jsonTask.getSingleId());
        aiForecast1.setCreateTime(DateUtil.now());
        if (StringUtils.isNotEmpty(singleSlide.getArea())) {
            BigDecimal bigDecimal = new BigDecimal(singleSlide.getArea()).subtract(bigDecimalA).subtract(bigDecimalB).setScale(3, RoundingMode.HALF_UP);
            aiForecast1.setResults(bigDecimal.toString());
        }
        insertEntity.add(aiForecast1);
        AiForecast aiForecast2 = new AiForecast();
        aiForecast2.setQuantitativeIndicators("皮肤面积");
        aiForecast2.setQuantitativeIndicatorsEn("Skin area");
        aiForecast2.setUnit("平方毫米");
        aiForecast2.setSingleSlideId(jsonTask.getSingleId());
        aiForecast2.setCreateTime(DateUtil.now());
        aiForecast2.setResults(bigDecimalB.setScale(3, RoundingMode.HALF_UP).toString());
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
        aiForecast3.setResults(decimal.divide(bigDecimalB, 3, RoundingMode.HALF_UP)+"");
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
        aiForecast4.setResults(decimal1.divide(bigDecimalB, 3, RoundingMode.HALF_UP)+"");
        insertEntity.add(aiForecast4);

        aiForecastService.saveBatch(insertEntity);
    }

    @Override
    public String getAlgorithmCode() {
        return "Skin_mammary";
    }
}
