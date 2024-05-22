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
 * @desc 大鼠盲肠
 */
@Slf4j
@Component("Cecum")
public class CecumParserStrategyImpl extends AbstractCustomParserStrategy {

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
        log.info("CecumParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("大鼠盲肠结构指标计算开始");
        // 查询所有未被删除且登录机构相同的数据
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        String area = ObjectUtil.isNotEmpty(singleSlide) ? singleSlide.getArea() : "0";
        area = ObjectUtil.isEmpty(area) ? "0" : area;
        Map<String, IndicatorAddIn> resultMap = new HashMap<>();
        // 肠腔面积
        BigDecimal colonArea = commonJsonParser.getOrganArea(jsonTask, "114156").getStructureAreaNum();
        // 黏膜层面积
        BigDecimal areaNum = commonJsonParser.getOrganArea(jsonTask, "114008").getStructureAreaNum();
        // 黏膜下层面积
        BigDecimal areaNum2 = commonJsonParser.getOrganArea(jsonTask, "114009").getStructureAreaNum();
        // 肌层面积
        BigDecimal areaNum3 = commonJsonParser.getOrganArea(jsonTask, "11400C").getStructureAreaNum();
        // 淋巴小结面积
        BigDecimal areaNum4 = commonJsonParser.getOrganArea(jsonTask, "114049").getStructureAreaNum();
        // 组织轮廓
        BigDecimal areaNum5 = new BigDecimal(area);
        // 盲肠面积
        BigDecimal areaNum6 = new BigDecimal(0);
        if (areaNum5.compareTo(BigDecimal.ZERO) != 0) {
            areaNum6 = areaNum5.subtract(colonArea).setScale(3, RoundingMode.HALF_UP);
        }
        resultMap.put("肠腔面积", new IndicatorAddIn("Intestinal cavity area", colonArea.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        resultMap.put("黏膜层面积", new IndicatorAddIn("Mucosal layer area", areaNum.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        resultMap.put("黏膜下层面积", new IndicatorAddIn("Submucosal area", areaNum2.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        resultMap.put("肌层面积", new IndicatorAddIn("Muscle layer area", areaNum3.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        resultMap.put("淋巴小结面积", new IndicatorAddIn("Lymph nodule area", areaNum4.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        resultMap.put("组织轮廓面积", new IndicatorAddIn("Organizational Profile area", areaNum5.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_1));
        resultMap.put("盲肠面积", new IndicatorAddIn("Cecum area", areaNum6.setScale(3, RoundingMode.HALF_UP).toString(), "平方毫米", CommonConstant.NUMBER_0));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), resultMap);
        log.info("大鼠盲肠结构指标计算结束");
    }

    @Override
    public String getAlgorithmCode() {
        return "Cecum";
    }
}
