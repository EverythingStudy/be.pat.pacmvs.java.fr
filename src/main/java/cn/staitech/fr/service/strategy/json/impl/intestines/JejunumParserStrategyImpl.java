package cn.staitech.fr.service.strategy.json.impl.intestines;

import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author
 * @Date 2024/5/13 10:05
 * @desc 大鼠空肠
 */
@Slf4j
@Component("Jejunum")
public class JejunumParserStrategyImpl extends AbstractCustomParserStrategy {

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
        log.info("JejunumParserStrategyImpl init");
    }

    /**
     * 结构指标计算
     * 结构	编码
     * 肠腔	118156
     * 黏膜层	118008
     * 黏膜下层 	118009
     * 肌层	11800C
     * 组织轮廓	118111
     * 算法输出指标	指标代码（仅限本文档）	单位（保留小数点后三位）	备注
     * 肠腔面积	A	平方毫米
     * 黏膜层面积	B	平方毫米	无
     * 黏膜下层面积	C	平方毫米	无
     * 肌层面积	D	平方毫米	无
     * 空肠面积	E	平方毫米	无
     * <p>
     * 产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后三位）	English	计算方式	备注
     * 黏膜层面积占比（环型）	1	%	Mucosal area%	1=（B-A）/（E-A）
     * 黏膜层面积占比（C型）	2	%	Mucosal area%	2=B/（E-A）
     * 黏膜下层面积占比（环型）	3	%	Submucosal area%	3=（C-B）/（E-A）
     * 黏膜下层面积占比
     * （C型）	4	%	Submucosal area%	4=C/（E-A）
     * 肌层面积占比（环型）	5	%	Muscular area%	5=（D-C）/（E-A）
     * 肌层面积占比
     * （C型）	6	%	Muscular area%	6=D/（E-A）
     * 空肠面积	7	平方毫米	Jejunum area	7=E-A
     *
     * @param jsonTask
     */
    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("大鼠空肠结构指标计算开始");
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        String area = ObjectUtil.isNotEmpty(singleSlide) ? singleSlide.getArea() : "0";
        area = ObjectUtil.isEmpty(area) ? "0" : area;
        Map<String, IndicatorAddIn> resultMap = new HashMap<>();
        // 肠腔面积
        BigDecimal colonArea = commonJsonParser.getOrganArea(jsonTask, "118156").getStructureAreaNum();
        // 黏膜层面积
        BigDecimal areaNum = commonJsonParser.getOrganArea(jsonTask, "118008").getStructureAreaNum();
        // 黏膜下层面积
        BigDecimal areaNum2 = commonJsonParser.getOrganArea(jsonTask, "118009").getStructureAreaNum();
        // 肌层面积
        BigDecimal areaNum3 = commonJsonParser.getOrganArea(jsonTask, "11800C").getStructureAreaNum();
        // 组织轮廓
        BigDecimal areaNum4 = new BigDecimal(area);
        // 结肠面积
        BigDecimal areaNum5 = new BigDecimal(0);
        if (areaNum4.compareTo(BigDecimal.ZERO) != 0) {
            areaNum5 = areaNum4.subtract(colonArea).setScale(3, RoundingMode.HALF_UP);
        }
        resultMap.put("肠腔面积", new IndicatorAddIn("Intestinal cavity area", colonArea.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        resultMap.put("黏膜层面积", new IndicatorAddIn("Mucosal layer area", areaNum.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        resultMap.put("黏膜下层面积", new IndicatorAddIn("Submucosal area", areaNum2.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        resultMap.put("肌层面积", new IndicatorAddIn("Muscle layer area", areaNum3.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        resultMap.put("组织轮廓面积", new IndicatorAddIn("Organizational Profile area", areaNum4.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        resultMap.put("空肠面积", new IndicatorAddIn("Jejunum area", areaNum5.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_0));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), resultMap);
        log.info("大鼠空肠结构指标计算结束");
    }

    @Override
    public String getAlgorithmCode() {
        return "Jejunum";
    }
}
