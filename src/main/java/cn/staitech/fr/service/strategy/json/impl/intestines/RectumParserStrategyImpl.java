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
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
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
 * @desc 大鼠直肠
 */
@Slf4j
@Component("Rectum")
public class RectumParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private CommonJsonCheck commonJsonCheck;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("RectumParserStrategyImpl init");
    }

    /**
     * 结构指标计算
     * 结构	编码
     * 肠腔	116156
     * 黏膜层	116008
     * 黏膜下层 	116009
     * 肌层	11600C
     * 组织轮廓	116111
     * <p>
     * 算法输出指标	指标代码（仅限本文档）	单位（保留小数点后三位）	备注
     * 肠腔面积	A	平方毫米
     * 黏膜层面积	B	平方毫米	无
     * 黏膜下层面积	C	平方毫米	无
     * 肌层面积	D	平方毫米	无
     * 组织轮廓面积	E	平方毫米	无
     * 产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后三位）	English	计算方式	备注
     * 黏膜层面积占比（环型）	1	%	Mucosal area%	1=（B-A）/（E-A）	无
     * 黏膜层面积占比（C型）	2	%	Mucosal area%	2=B/（E-A）	无
     * 黏膜下层面积占比（环型）	3	%	Submucosal area%	3=（C-B）/（E-A）	无
     * 黏膜下层面积占比
     * （C型）	4	%	Submucosal area%	4=C/（E-A）	无
     * 肌层面积占比（环型）	5	%	Muscular area%	5=（D-C）/（E-A）	无
     * 肌层面积占比
     * （C型）	6	%	Muscular area%	6=D/（E-A）	无
     * 直肠面积	7	平方毫米	Rectum area	7=E-A	无
     *
     * @param jsonTask
     */
    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("大鼠直肠结构指标计算开始");
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        String area = ObjectUtil.isNotEmpty(singleSlide) ? singleSlide.getArea() : "0";
        area = ObjectUtil.isEmpty(area) ? "0" : area;
        Map<String, IndicatorAddIn> map = new HashMap<>();
        // 肠腔面积
        BigDecimal colonArea = commonJsonParser.getOrganArea(jsonTask, "118156").getStructureAreaNum();
        // 黏膜层面积
        BigDecimal areaNum = commonJsonParser.getOrganArea(jsonTask, "118008").getStructureAreaNum();
        // 黏膜下层面积
        BigDecimal areaNum2 = commonJsonParser.getOrganArea(jsonTask, "118009").getStructureAreaNum();
        // 肌层面积
        BigDecimal areaNum3 = commonJsonParser.getOrganArea(jsonTask, "11800C").getStructureAreaNum();
        // 组织轮廓面积
        BigDecimal areaNum4 = new BigDecimal(area);
        // 直肠面积 E-A
        BigDecimal eSubtractA = new BigDecimal(0);
        if (areaNum4.compareTo(BigDecimal.ZERO) != 0) {
            eSubtractA = areaNum4.subtract(colonArea).setScale(3, RoundingMode.HALF_UP);
        }
        // 算法输出指标 -------------------------------------------------------------

        // 肠腔面积	A	平方毫米
        map.put("肠腔面积", new IndicatorAddIn("Intestinal cavity area", colonArea.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));

        // 黏膜层面积	B	平方毫米	以C型或双层环状输出
        map.put("黏膜层面积", new IndicatorAddIn("Mucosal layer area", areaNum.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));

        // 黏膜下层面积	C	平方毫米	以C型或双层环状输出
        map.put("黏膜下层面积", new IndicatorAddIn("Submucosal area", areaNum2.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));

        // 肌层面积	D	平方毫米	以C型或双层环状输出
        map.put("肌层面积", new IndicatorAddIn("Muscle layer area", areaNum3.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        // 组织轮廓面积	E	平方毫米	无
        map.put("组织轮廓面积", new IndicatorAddIn("Organizational Profile area", areaNum4.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));

        // 产品呈现指标 -------------------------------------------------------------
        // 黏膜层面积占比	1	%	Mucosal area%	1=B/（E-A）	无
        String mucosalAreaRate = areaNum.divide(eSubtractA).setScale(3, RoundingMode.HALF_UP).toString();
        map.put("黏膜层面积占比", new IndicatorAddIn("Mucosal area%", mucosalAreaRate, "%"));

        // 黏膜下层面积占比	2	%	Submucosal area%	2=C/（E-A）	无
        String submucosalAreaRate = areaNum2.divide(eSubtractA).setScale(3, RoundingMode.HALF_UP).toString();
        map.put("黏膜下层面积占比", new IndicatorAddIn("Submucosal area%", submucosalAreaRate, "%"));

        // 肌层面积占比	3	%	Muscular area%	3=D/（E-A）	无
        String muscularAreaRate = areaNum3.divide(eSubtractA).setScale(3, RoundingMode.HALF_UP).toString();
        map.put("肌层面积占比", new IndicatorAddIn("Muscular area%", muscularAreaRate, "%"));

        // 直肠面积	4	平方毫米	Rectum area	4=E-A	无
        map.put("直肠面积", new IndicatorAddIn("Rectum area", eSubtractA.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_0));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
        log.info("大鼠直肠结构指标计算结束");
    }

    @Override
    public String getAlgorithmCode() {
        return "Rectum";
    }
}
