package cn.staitech.fr.service.strategy.json.impl;

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
 * @author wanglibei
 * @version V1.0
 * @ClassName: SternumParserStrategyImpl
 * @Description:大鼠胸骨
 * @date 2024年5月13日
 */
@Slf4j
@Component("Sternum")
public class SternumParserStrategyImpl extends AbstractCustomParserStrategy {

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
    private CommonJsonParser commonJsonParser;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        log.info("SternumParserStrategyImpl init");
    }


    @Override
    public void alculationIndicators(JsonTask jsonTask) {

        log.info("大鼠胸骨构指标计算开始");
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

        //骨髓腔:14E00E
        //红系细胞核:14E011
        //粒系细胞:14E01A
        //巨核系细胞:14E022
        //红细胞:14E004
        //脂肪细胞:	14E012
        //骨质:	14E00F
        //组织轮廓:	14E111

        List<AiForecast> insertEntity = new ArrayList<>();

        Integer result = 0;
        //粒系细胞数量C 个
        if (ObjectUtil.isNotEmpty(pathologicalMap.get("14E01A"))) {
            Annotation annotation1 = new Annotation();
            annotation1.setSingleSlideId(jsonTask.getSingleId());
            annotation1.setCategoryId(pathologicalMap.get("14E01A"));
            annotation1.setSequenceNumber(sequenceNumber);
            result = annotationMapper.countDucts(annotation1);
        }

        Integer result2 = 0;
        //红系细胞核数量B 个
        if (ObjectUtil.isNotEmpty(pathologicalMap.get("14E011"))) {
            Annotation annotation2 = new Annotation();
            annotation2.setSingleSlideId(jsonTask.getSingleId());
            annotation2.setCategoryId(pathologicalMap.get("14E011"));
            annotation2.setSequenceNumber(sequenceNumber);
            result2 = annotationMapper.countDucts(annotation2);
        }

        //粒红比  2=C/B

        if (null != result && null != result2) {
            AiForecast aiForecast1 = new AiForecast();
            aiForecast1.setQuantitativeIndicators("粒红比");
            aiForecast1.setQuantitativeIndicatorsEn("Myelocyte:erythropoiesis ratio");
            aiForecast1.setUnit("无");
            aiForecast1.setSingleSlideId(jsonTask.getSingleId());
            //保留小数点后3位
            BigDecimal bigDecimal = new BigDecimal(result);
            BigDecimal bigDecimal2 = new BigDecimal(result2);
            BigDecimal bigDecimal3 = bigDecimal.divide(bigDecimal2, 3, RoundingMode.HALF_UP);
            aiForecast1.setResults(bigDecimal3.toString());
            insertEntity.add(aiForecast1);
        }

        //胸骨面积 ==>组织轮廓面积
        if (ObjectUtil.isNotEmpty(pathologicalMap.get("14E111"))) {
            AiForecast aiForecast2 = new AiForecast();
            aiForecast2.setQuantitativeIndicators("胸骨面积");
            aiForecast2.setQuantitativeIndicatorsEn("Sternum area");
            aiForecast2.setUnit("平方毫米");
            SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
            if (StringUtils.isNotEmpty(singleSlide.getArea())) {
                aiForecast2.setResults(singleSlide.getArea());
            }
            aiForecast2.setSingleSlideId(jsonTask.getSingleId());
            insertEntity.add(aiForecast2);
        }


        aiForecastService.saveBatch(insertEntity);
    }

    @Override
    public String getAlgorithmCode() {
        return "Sternum";
    }
}
