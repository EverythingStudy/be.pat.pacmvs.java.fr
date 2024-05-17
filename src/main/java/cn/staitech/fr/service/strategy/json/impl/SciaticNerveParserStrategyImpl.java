package cn.staitech.fr.service.strategy.json.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.domain.AiForecast;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.mapper.AnnotationMapper;
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
 * @author wanglibei
 * @version V1.0
 * @ClassName: SciaticNerveParserStrategyImpl
 * @Description:大鼠坐骨神经
 * @date 2024年5月13日
 */
@Slf4j
@Component("Sciatic_nerve")
public class SciaticNerveParserStrategyImpl extends AbstractCustomParserStrategy {
    @Resource
    private AnnotationMapper annotationMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        log.info("SciaticNerveParserStrategyImpl init");
    }


    @Override
    public void alculationIndicators(JsonTask jsonTask) {

        log.info("大鼠坐骨神经构指标计算开始");
        // 查询所有未被删除且登录机构相同的数据
        Map<String, Long> pathologicalMap = commonJsonParser.getPathologicalMap(jsonTask.getOrganizationId());
        //定位表
        Long sequenceNumber = commonJsonParser.getSequenceNumber(jsonTask.getSpecialId());

//		结构	编码
//		神经纤维束	1400BB
//		神经外膜结缔组织	1400BA
//		算法输出指标	指标代码（仅限本文档）	单位（保留小数点后3位）	备注
//		神经纤维束面积	A	103平方微米	若多个数据则相加输出
//		神经外膜结缔组织面积	B	平方毫米	
//
//		产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后3位）	English	计算方式	备注
//		神经纤维束面积	1	103平方微米	Nerve fiber bundles area	1=A	
//		结缔组织面积	2	平方毫米	Connective tissue area	2=B-A	运算前注意统一单位
//		即神经外膜结缔组织面积

        List<AiForecast> insertEntity = new ArrayList<>();


        //神经纤维束面积	1	10³平方微米	Nerve fiber bundles area	1=A
        if (ObjectUtil.isNotEmpty(pathologicalMap.get("1400BB"))) {
            Annotation annotation1 = new Annotation();
            annotation1.setSingleSlideId(jsonTask.getSingleId());
            annotation1.setCategoryId(pathologicalMap.get("1400BB"));
            annotation1.setSequenceNumber(sequenceNumber);
            Annotation structureArea = annotationMapper.getStructureArea(annotation1);
            BigDecimal bigDecimalA = new BigDecimal(0);
            if (StringUtils.isNotEmpty(structureArea.getArea())) {
                bigDecimalA = new BigDecimal(structureArea.getArea());
            }
            //转平方微米
            bigDecimalA = bigDecimalA.multiply(new BigDecimal(1000000));

            AiForecast aiForecast1 = new AiForecast();
            aiForecast1.setQuantitativeIndicators("神经纤维束面积");
            aiForecast1.setQuantitativeIndicatorsEn("Nerve fiber bundles area");
            aiForecast1.setUnit("10³平方微米");
            aiForecast1.setSingleSlideId(jsonTask.getSingleId());
            //保留小数点后3位
            bigDecimalA = bigDecimalA.setScale(3, RoundingMode.HALF_UP);
            aiForecast1.setResults(bigDecimalA.toString());
            insertEntity.add(aiForecast1);
        }

        aiForecastService.saveBatch(insertEntity);
    }

    @Override
    public String getAlgorithmCode() {
        return "Sciatic_nerve";
    }
}
