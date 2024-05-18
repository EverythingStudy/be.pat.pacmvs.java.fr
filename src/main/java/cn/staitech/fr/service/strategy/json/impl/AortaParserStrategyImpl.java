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
 * @ClassName: AortaParserStrategyImpl
 * @Description:大鼠主动脉
 * @date 2024年5月13日
 */
@Slf4j
@Component("Aorta")
public class AortaParserStrategyImpl extends AbstractCustomParserStrategy {

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
        log.info("AortaParserStrategyImpl init");
    }


    @Override
    public void alculationIndicators(JsonTask jsonTask) {

        log.info("大鼠主动脉构指标计算开始");
        // 查询所有未被删除且登录机构相同的数据
        Map<String, Long> pathologicalMap = commonJsonParser.getPathologicalMap(jsonTask.getOrganizationId());
        //定位表
        Long sequenceNumber = commonJsonParser.getSequenceNumber(jsonTask.getSpecialId());

        //空腔	15D113  A     10³平方微米
        //组织轮廓	15D111  D   10³平方微米

        List<AiForecast> insertEntity = new ArrayList<>();

        BigDecimal bigDecimalD = new BigDecimal(0);
        //组织轮廓面积 D 10³平方微米
        if (ObjectUtil.isNotEmpty(pathologicalMap.get("15D111"))) {
            SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
            if (StringUtils.isNotEmpty(singleSlide.getArea())) {
                bigDecimalD = new BigDecimal(singleSlide.getArea());
            }
        }


        BigDecimal bigDecimalA = new BigDecimal(0);
        //空腔面积 D 10³平方微米
        if (ObjectUtil.isNotEmpty(pathologicalMap.get("15D113"))) {
            Annotation annotation1 = new Annotation();
            annotation1.setSingleSlideId(jsonTask.getSingleId());
            annotation1.setCategoryId(pathologicalMap.get("15D113"));
            annotation1.setSequenceNumber(sequenceNumber);
            Annotation structureArea = annotationMapper.getStructureArea(annotation1);
            if (StringUtils.isNotEmpty(structureArea.getArea())) {
                bigDecimalA = new BigDecimal(structureArea.getArea());
            }
        }

        //主动脉壁面积  1=D-A

        if (null != bigDecimalD) {

            AiForecast aiForecast1 = new AiForecast();
            aiForecast1.setQuantitativeIndicators("主动脉壁面积");
            aiForecast1.setQuantitativeIndicatorsEn("Aorta wall area");
            aiForecast1.setUnit("10³平方微米");
            aiForecast1.setSingleSlideId(jsonTask.getSingleId());
            // 执行减法操作
            BigDecimal result = bigDecimalD.subtract(bigDecimalA);
            //转平方微米
            result = result.multiply(new BigDecimal(1000000));
            //保留小数点后3位
            result = result.setScale(3, RoundingMode.HALF_UP);
            aiForecast1.setResults(result.toString());
            insertEntity.add(aiForecast1);
        }

        aiForecastService.saveBatch(insertEntity);
    }

    @Override
    public String getAlgorithmCode() {
        return "Aorta";
    }
}
