package cn.staitech.fr.service.strategy.json.impl.digestive;

import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.constant.CommonConstant;
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
import java.util.HashMap;
import java.util.Map;

/**
 * @Author wudi
 * @Date 2024/5/13 10:05
 * @desc 乳腺-皮肤
 */
@Slf4j
@Component("D66_Integument_and_Mammary_gland")
public class MammaryGlandParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
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

    /**
     * 结构指标计算
     * 结构	编码
     * 表皮角质层	121096
     * 表皮基底层+棘层+颗粒层	121097
     * 毛囊	121098
     * 皮脂腺	121099
     * 组织轮廓	121111
     * 算法输出指标	指标代码（仅限本文档）	单位（保留小数点后三位）	备注
     * 表皮角质层面积	A	平方毫米	若多个数据则相加输出
     * 表皮基底层+棘层+颗粒层面积	B	平方毫米	若多个数据则相加输出
     * 毛囊面积（单个）	C	103平方微米	单个毛囊面积输出
     * 毛囊数量	D	个	无
     * 皮脂腺面积	E	103平方微米	数据相加输出
     * 皮脂腺数量	F	个	无
     * 皮肤面积	G	平方毫米	此数据使用乳腺中皮肤数据
     * 毛囊面积（全片）	H	平方毫米	无
     * <p>
     * 产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后三位）	English	计算方式	备注
     * 表皮角质层面积占比	1	%	Stratum corneum area%	1=A/G
     * 表皮基底层+棘层+颗粒层面积占比	2	%	 Nucleated cell layer area%	2=B/G
     * 毛囊面积（单个）	3	103平方微米	Hair follicle area（per）	3=C	以95%置信区间和均数±标准差呈现
     * 毛囊密度	4	个/平方毫米	Density of hair follicles 	4=D/G
     * 皮脂腺密度	5	个/平方毫米	Density of Sebaceous glands 	5=F/G
     * 皮脂腺面积占比	6	%	Sebaceous glands area%	6=E/G	运算前注意统一单位
     * 毛囊面积占比	7	%	Hair follicles area%	7=H/G
     * 皮肤面积	8	平方毫米	Skin area	8=G
     *
     * @param jsonTask
     */
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
        // 表皮角质层面积
        BigDecimal organArea3 = commonJsonParser.getOrganArea(jsonTask, "121096").getStructureAreaNum();
        // 表皮基底层+棘层+颗粒层面积
        BigDecimal organArea4 = commonJsonParser.getOrganArea(jsonTask, "121097").getStructureAreaNum();
        // 毛囊数量
        Integer areaCount = commonJsonParser.getOrganAreaCount(jsonTask, "121098");
        // 皮脂腺面积
        BigDecimal organArea5 = commonJsonParser.getOrganAreaMicron(jsonTask, "121099");
        // 皮脂腺数量
        Integer organAreaCount1 = commonJsonParser.getOrganAreaCount(jsonTask, "121099");
        // 毛囊面积（全片）
        BigDecimal organArea6 = commonJsonParser.getOrganArea(jsonTask, "121098").getStructureAreaNum();
        // 毛囊密度
        BigDecimal divide = new BigDecimal(0);
        if (ObjectUtil.isNotEmpty(organAreaB) && !ObjectUtil.equals(organAreaB, new BigDecimal(0))) {
            BigDecimal decimal = new BigDecimal(areaCount);
            divide = decimal.divide(organAreaB, 3, RoundingMode.HALF_UP);
        }
        // 皮脂腺密度
        BigDecimal divide1 = new BigDecimal(0);
        if (ObjectUtil.isNotEmpty(organAreaB) && !ObjectUtil.equals(organAreaB, new BigDecimal(0))) {
            BigDecimal decimal = new BigDecimal(organAreaCount1);
            divide1 = decimal.divide(organAreaB, 3, RoundingMode.HALF_UP);
        }

        indicatorResultsMap.put("乳腺腺泡和导管数量", new IndicatorAddIn("Number of acinus and ducts", organAreaCount.toString(), "个"));
        indicatorResultsMap.put("乳腺面积", new IndicatorAddIn("Mammary gland area", h.subtract(organAreaA).subtract(organAreaB).setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米"));
        indicatorResultsMap.put("淋巴结面积", new IndicatorAddIn("Lymph node area", organAreaA.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("皮肤面积", new IndicatorAddIn("Skin area", organAreaB.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("乳腺腺泡/导管面积（全片）", new IndicatorAddIn("Breast acinar/ductal area (all)", organArea1.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("结缔组织面积", new IndicatorAddIn("Connective tissue area", organArea2.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("组织轮廓面积", new IndicatorAddIn("Organizational contour area", h.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("乳腺细胞核数量（全片）", new IndicatorAddIn("Number of breast cell nuclei (all)", organAreaCount2.toString(), "个", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("乳腺腺泡/导管面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1));

        // 表皮角质层面积
        indicatorResultsMap.put("表皮角质层面积", new IndicatorAddIn("Epidermal stratum corneum area", organArea3.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米"));
        // 表皮基底层+棘层+颗粒层面积
        indicatorResultsMap.put("表皮基底层+棘层+颗粒层面积", new IndicatorAddIn("Area of basal layer+spinous layer+granular layer of epidermis", organArea4.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米"));
        // 毛囊数量
        indicatorResultsMap.put("毛囊数量", new IndicatorAddIn("Number of mucous sacs", areaCount.toString(), "个"));
        // 皮脂腺面积
        indicatorResultsMap.put("皮脂腺面积", new IndicatorAddIn("Sebaceous gland area", organArea5.setScale(3, RoundingMode.HALF_UP).toString(), "10³平方微米"));
        // 皮脂腺数量
        indicatorResultsMap.put("皮脂腺数量", new IndicatorAddIn("Number of sebaceous glands", organAreaCount1.toString(), "个"));
        // 毛囊面积（全片）
        indicatorResultsMap.put("毛囊面积（全片）", new IndicatorAddIn("Mucous sac area (all)", organArea6.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米"));
        // 毛囊密度
        indicatorResultsMap.put("毛囊密度", new IndicatorAddIn("Mucous sac density", divide.toString(), "个/平方毫米", CommonConstant.NUMBER_1));
        // 皮脂腺密度
        indicatorResultsMap.put("皮脂腺密度", new IndicatorAddIn("Sebaceous gland density", divide1.toString(), "个/平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("毛囊面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "D66_Integument_and_Mammary_gland";
    }
}
