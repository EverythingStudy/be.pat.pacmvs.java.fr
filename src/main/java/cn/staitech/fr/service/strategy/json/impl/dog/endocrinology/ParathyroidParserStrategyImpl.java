package cn.staitech.fr.service.strategy.json.impl.dog.endocrinology;

import cn.staitech.common.core.utils.StringUtils;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.service.strategy.json.OutlineCustom;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 
* @ClassName: ParathyroidParserStrategyImpl
* @Description:犬-甲状旁腺
* @author wanglibei
* @date 2026年2月11日
* @version V1.0
 */


/**
 * 
 * 
@Slf4j
@Component("Parathyroid_3")
public class ParathyroidParserStrategyImpl extends AbstractCustomParserStrategy {
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private AreaUtils areaUtils;
    @Resource
    private CommonJsonCheck commonJsonCheck;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("ParathyroidParserStrategyImpl init");
    }


    @Override
    public void alculationIndicators(JsonTask jsonTask) {

        log.info("大鼠甲状旁腺构指标计算开始");
        //主细胞核:108091
        //组织轮廓:（默认都有）	108111

        //		算法输出指标	指标代码（仅限本文档）	单位（保留小数点后三位）	备注
        //		主细胞核数量	A	个	无
        //		组织轮廓面积	B	10³平方微米	若多个数据则相加输出
        //
        //		产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后三位）	English	计算方式	备注
        //		主细胞核密度	1	个/10³平方微米	Nucleus density of chief cell 	1=A/B
        //		甲状旁腺面积	2	10³平方微米	Parathyroid gland area	2=B
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());
        //主细胞核数量A 个
        Integer mucosaCountA = commonJsonParser.getOrganAreaCount(jsonTask, "108091");
        mucosaCountA = commonJsonParser.getIntegerValue(mucosaCountA);
        //组织轮廓面积==>甲状旁腺面积 B 10³平方微米
        BigDecimal areaDecimalB = BigDecimal.ZERO;
        if (StringUtils.isNotEmpty(slideArea)) {
            String area = areaUtils.convertToSquareMicrometer(slideArea);
            areaDecimalB = new BigDecimal(area);
        }
        areaDecimalB = areaDecimalB.setScale(3, RoundingMode.HALF_UP);
        areaDecimalB = commonJsonParser.getBigDecimalValue(areaDecimalB);

        //主细胞核密度 1=A/B
        BigDecimal bigDecimaE = commonJsonParser.getProportionMultiply(new BigDecimal(mucosaCountA), areaDecimalB);

        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();

        indicatorResultsMap.put("主细胞核数量", createIndicator(String.valueOf(mucosaCountA), PIECE, "108091"));

        indicatorResultsMap.put("甲状旁腺面积", createNameIndicator("Parathyroid gland area", String.valueOf(areaDecimalB.setScale(3, RoundingMode.HALF_UP)), SQ_UM_THOUSAND, "108111"));

        indicatorResultsMap.put("主细胞核密度", createNameIndicator("Nucleus density of chief cell", String.valueOf(bigDecimaE.setScale(3, RoundingMode.HALF_UP)), SQ_UM_PICE, "108091,108111"));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Parathyroid";
    }
}

*/
