package cn.staitech.fr.service.strategy.json.impl.intestines;

import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.service.strategy.json.ParserStrategy;
import cn.staitech.fr.utils.DecimalUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author wangfeng
 * @Date 2024/6/19 10:05
 * @desc 大鼠空肠
 */
@Slf4j
@Component("Jejunum")
public class JejunumParserStrategyImpl implements ParserStrategy {

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

    @Override
    public void parseJson(JsonTask jsonTask, JsonFile jsonFileS) {
        commonJsonParser.parseJson(jsonTask, jsonFileS);
    }

    @Override
    public boolean checkJson(JsonTask jsonTask, List<JsonFile> jsonFileList) {
        return commonJsonCheck.checkJson(jsonTask, jsonFileList);
    }

    /**
     * 指标计算
     *
     * @param jsonTask
     */
    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("指标计算开始-大鼠空肠");
        Map<String, IndicatorAddIn> map = new HashMap<>();

        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        String area = ObjectUtil.isNotEmpty(singleSlide) ? singleSlide.getArea() : "0";
        area = ObjectUtil.isEmpty(area) ? "0" : area;

        // 肠腔	118156
        // 黏膜层	118008
        // 黏膜下层 	118009
        // 肌层	11800C
        // 组织轮廓	118111

        // 肠腔面积
        BigDecimal colonArea = commonJsonParser.getOrganArea(jsonTask, "118156").getStructureAreaNum();
        // 黏膜层面积
        BigDecimal areaNumB = commonJsonParser.getOrganArea(jsonTask, "118008").getStructureAreaNum();
        // 黏膜下层面积
        BigDecimal areaNumC = commonJsonParser.getOrganArea(jsonTask, "118009").getStructureAreaNum();
        // 肌层面积
        BigDecimal areaNumD = commonJsonParser.getOrganArea(jsonTask, "11800C").getStructureAreaNum();
        // 组织轮廓
        BigDecimal tissueArea = new BigDecimal(area);
        // 结肠面积
        BigDecimal subtractEA = new BigDecimal(0);
        if (tissueArea.compareTo(BigDecimal.ZERO) != 0) {
            subtractEA = tissueArea.subtract(colonArea).setScale(7, RoundingMode.HALF_UP);
        }

        // 算法输出指标 -------------------------------------------------------------
        // 肠腔面积	A	平方毫米
        map.put("肠腔面积", new IndicatorAddIn("Intestinal cavity area", DecimalUtils.setScale3(colonArea), "平方毫米", CommonConstant.NUMBER_1));

        // 黏膜层面积	B	平方毫米	以C型或双层环状输出
        map.put("黏膜层面积", new IndicatorAddIn("Mucosal layer area", DecimalUtils.setScale3(areaNumB), "平方毫米", CommonConstant.NUMBER_1));

        // 黏膜下层面积	C	平方毫米	以C型或双层环状输出
        map.put("黏膜下层面积", new IndicatorAddIn("Submucosal area", DecimalUtils.setScale3(areaNumC), "平方毫米", CommonConstant.NUMBER_1));

        // 肌层面积	D	平方毫米	以C型或双层环状输出
        map.put("肌层面积", new IndicatorAddIn("Muscle layer area", DecimalUtils.setScale3(areaNumD), "平方毫米", CommonConstant.NUMBER_1));

        // 组织轮廓面积	E	平方毫米	无
        map.put("组织轮廓面积", new IndicatorAddIn("Tissue area", DecimalUtils.setScale3(tissueArea), "平方毫米", CommonConstant.NUMBER_1));

        // 产品呈现指标 -------------------------------------------------------------
        if (subtractEA.compareTo(BigDecimal.ZERO) != 0) {
            // 黏膜层面积占比	1	%	Mucosal area%	1=B/（E-A）
            BigDecimal mucosalAreaRateDecimal = areaNumB.divide(subtractEA, 7, RoundingMode.HALF_UP);
            map.put("黏膜层面积占比", new IndicatorAddIn("Mucosal area%", DecimalUtils.percentScale3(mucosalAreaRateDecimal), "%"));

            // 黏膜下层面积占比	2	%	Submucosal area%	2=C/（E-A）
            BigDecimal submucosalAreaRateDecimal = areaNumC.divide(subtractEA, 7, RoundingMode.HALF_UP);
            map.put("黏膜下层面积占比", new IndicatorAddIn("Submucosal area%", DecimalUtils.percentScale3(submucosalAreaRateDecimal), "%"));

            // 肌层面积占比	3	%	Muscular area%	3=D/（E-A）
            BigDecimal muscularAreaRateDecimal = areaNumD.divide(subtractEA, 7, RoundingMode.HALF_UP);
            map.put("肌层面积占比", new IndicatorAddIn("Muscular area%", DecimalUtils.percentScale3(muscularAreaRateDecimal), "%"));
        } else {
            map.put("黏膜层面积占比", new IndicatorAddIn("Mucosal area%", "0.000", "%"));
            map.put("黏膜下层面积占比", new IndicatorAddIn("Submucosal area%", "0.000", "%"));
            map.put("肌层面积占比", new IndicatorAddIn("Muscular area%", "0.000", "%"));
        }

        // 空肠面积	4	平方毫米	Jejunum area	7=E-A
        map.put("空肠面积", new IndicatorAddIn("Jejunum area", DecimalUtils.setScale3(subtractEA), "平方毫米", CommonConstant.NUMBER_0));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
        log.info("指标计算结束-大鼠空肠");
    }
}
