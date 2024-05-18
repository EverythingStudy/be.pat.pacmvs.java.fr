package cn.staitech.fr.service.strategy.json.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.domain.AiForecast;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
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
 * @ClassName: ParathyroidParserStrategyImpl
 * @Description:大鼠甲状旁腺
 * @date 2024年5月13日
 */
@Slf4j
@Component("Parathyroid")
public class ParathyroidParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.info("ParathyroidParserStrategyImpl init");
    }


    @Override
    public void alculationIndicators(JsonTask jsonTask) {

        log.info("大鼠甲状旁腺构指标计算开始");
        // 查询所有未被删除且登录机构相同的数据
        Map<String, Long> pathologicalMap = commonJsonParser.getPathologicalMap(jsonTask.getOrganizationId());
        //定位表
        Long sequenceNumber = commonJsonParser.getSequenceNumber(jsonTask.getSpecialId());

        //主细胞核:108091
        //组织轮廓:（默认都有）	108111

//		算法输出指标	指标代码（仅限本文档）	单位（保留小数点后三位）	备注
//		主细胞核数量	A	个	无
//		组织轮廓面积	B	10³平方微米	若多个数据则相加输出
//
//		产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后三位）	English	计算方式	备注
//		主细胞核密度	1	个/10³平方微米	Nucleus density of chief cell 	1=A/B	
//		甲状旁腺面积	2	10³平方微米	Parathyroid gland area	2=B	

        List<AiForecast> insertEntity = new ArrayList<>();

        //主细胞核密度

        Integer result = 0;
        //主细胞核数量A 个
        if (ObjectUtil.isNotEmpty(pathologicalMap.get("108091"))) {
            Annotation annotation1 = new Annotation();
            annotation1.setSingleSlideId(jsonTask.getSingleId());
            annotation1.setCategoryId(pathologicalMap.get("108091"));
            annotation1.setSequenceNumber(sequenceNumber);
            result = annotationMapper.countDucts(annotation1);
        }

        BigDecimal bigDecimalB = new BigDecimal(0);
        //组织轮廓面积==>甲状旁腺面积 B 10³平方微米
        if (ObjectUtil.isNotEmpty(pathologicalMap.get("108111"))) {
            AiForecast aiForecast2 = new AiForecast();
            aiForecast2.setQuantitativeIndicators("甲状旁腺面积");
            aiForecast2.setQuantitativeIndicatorsEn("Parathyroid gland area");
            aiForecast2.setUnit("10³平方微米");
            SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
            if (StringUtils.isNotEmpty(singleSlide.getArea())) {
                BigDecimal cimal = new BigDecimal(1000000);
                BigDecimal areaDecimal = new BigDecimal(singleSlide.getArea());
                //转平方微米
                bigDecimalB = areaDecimal.multiply(cimal).setScale(3, RoundingMode.HALF_UP);
                aiForecast2.setResults(areaDecimal.toString());
            }
            aiForecast2.setSingleSlideId(jsonTask.getSingleId());
            insertEntity.add(aiForecast2);
        }

        //主细胞核密度 1=A/B
        BigDecimal bigDecimaE = new BigDecimal(0);
        if (null != bigDecimalB) {
            AiForecast aiForecast1 = new AiForecast();
            aiForecast1.setQuantitativeIndicators("主细胞核密度");
            aiForecast1.setQuantitativeIndicatorsEn("Nucleus density of chief cell");
            aiForecast1.setUnit("个/10³平方微米");
            aiForecast1.setSingleSlideId(jsonTask.getSingleId());
            //保留小数点后3位
            BigDecimal bigDecimal = new BigDecimal(result);
            bigDecimaE = bigDecimal.divide(bigDecimalB, 3, RoundingMode.HALF_UP);
            aiForecast1.setResults(bigDecimaE.toString());
            insertEntity.add(aiForecast1);
        }

        aiForecastService.saveBatch(insertEntity);
    }

    @Override
    public String getAlgorithmCode() {
        return "Parathyroid";
    }
}
