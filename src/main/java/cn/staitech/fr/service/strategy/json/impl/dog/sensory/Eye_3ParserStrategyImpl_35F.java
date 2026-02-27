package cn.staitech.fr.service.strategy.json.impl.dog.sensory;

import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.DecimalUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>犬类-特殊感觉系统-眼球</p>
 * <a href="https://docs.staitech.cn/i/nodes/7gNKMlbrq3WXA1LJAw1H4Xv5p6Ad9nL1">文档</a>
 *
 * @author wangtc
 */
@Component
@Slf4j
public class Eye_3ParserStrategyImpl_35F extends AbstractCustomParserStrategy {
    private final SingleSlideMapper singleSlideMapper;
    private final AiForecastService aiForecastService;

    public Eye_3ParserStrategyImpl_35F(
            CommonJsonParser commonJsonParser,
            CommonJsonCheck commonJsonCheck,
            SingleSlideMapper singleSlideMapper,
            AiForecastService aiForecastService
    ) {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        this.singleSlideMapper = singleSlideMapper;
        this.aiForecastService = aiForecastService;
    }

    @Override
    public String getAlgorithmCode() {
        return "Eye_3";
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn> map = new LinkedHashMap<>();
        // 晶状体面积 A
        BigDecimal areaA = getOrganArea(jsonTask, "35F101").getStructureAreaNum();
        map.put("晶状体面积", new IndicatorAddIn("", DecimalUtils.setScale3(areaA), SQ_MM, CommonConstant.NUMBER_0, "35F101"));
        // 视网膜面积 B
        BigDecimal areaB = getOrganArea(jsonTask, "35F103").getStructureAreaNum();
        map.put("视网膜面积", new IndicatorAddIn("", DecimalUtils.setScale3(areaB), SQ_MM, CommonConstant.NUMBER_0, "35F103"));
        // 视网膜周长 C
        BigDecimal perimeterC = getOrganArea(jsonTask, "35F103").getStructurePerimeterNum();
        map.put("视网膜周长", new IndicatorAddIn("", DecimalUtils.setScale3(perimeterC), MM, CommonConstant.NUMBER_0, "35F103"));
        // 组织轮廓面积 D
        BigDecimal areaD = new BigDecimal(singleSlideMapper.selectById(jsonTask.getSingleId()).getArea());
//        map.put("组织轮廓面积", new IndicatorAddIn("", DecimalUtils.setScale3(areaD), SQ_MM, CommonConstant.NUMBER_0, "35F111"));
        // 晶状体面积 1=A
        map.put("晶状体面积", new IndicatorAddIn("Crystalline lens area", DecimalUtils.setScale3(areaA), SQ_MM, CommonConstant.NUMBER_1, "35F101"));
        // 视网膜面积 2=B
        map.put("视网膜面积", new IndicatorAddIn("Retina area", DecimalUtils.setScale3(areaB), SQ_MM, CommonConstant.NUMBER_1, "35F103"));
        // 视网膜平均厚度 3=2B/C
        BigDecimal thickness3 = bigDecimalDivideCheck(BigDecimal.valueOf(2).multiply(ObjectUtil.defaultIfNull(areaB, BigDecimal.ZERO)), perimeterC);
        map.put("视网膜平均厚度", new IndicatorAddIn("Average thickness of retina", DecimalUtils.setScale3(thickness3), MM, CommonConstant.NUMBER_1, "35F103"));
        // 眼球面积 4=D
        map.put("眼球面积", new IndicatorAddIn("Eye area", DecimalUtils.setScale3(areaD), SQ_MM, CommonConstant.NUMBER_1, "35F111"));
        // 保存结果
        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
    }
}
