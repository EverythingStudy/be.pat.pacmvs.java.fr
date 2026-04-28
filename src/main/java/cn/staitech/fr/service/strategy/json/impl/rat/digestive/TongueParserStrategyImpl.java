package cn.staitech.fr.service.strategy.json.impl.rat.digestive;

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
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 
* @ClassName: TongueParserStrategyImpl
* @Description-d:舌
* @author wanglibei
* @date 2025年7月21日
* @version V1.0
 */
@Slf4j
@Component("Tongue")
public class TongueParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private AiForecastService aiForecastService;

    @Resource
    private CommonJsonParser commonJsonParser;

    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private CommonJsonCheck commonJsonCheck;
    @Autowired
    private AreaUtils areaUtils;
    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("TongueParserStrategyImpl init");
    }

    @Override
    public String getAlgorithmCode() {
        return "Tongue";
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("大鼠舌结构指标面积计算：");
        //组织轮廓面积
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        //A	角质层面积	10D12E
        BigDecimal organArea = commonJsonParser.getOrganAreaMicron(jsonTask, "10D12E");
        //B	颗粒层+棘层+基底细胞层面积	10D12F
        BigDecimal organArea1 = commonJsonParser.getOrganAreaMicron(jsonTask, "10D12F");
        //C	固有层+肌层面积	10D01C
        BigDecimal organArea2 = commonJsonParser.getOrganArea(jsonTask, "10D01C").getStructureAreaNum();
        //D	组织轮廓	10D111
        BigDecimal bigDecimal = new BigDecimal(singleSlide.getArea());
        
        //一级指标 
        indicatorResultsMap.put("角质层面积", createIndicator(organArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_UM_THOUSAND, "10D12E"));
        indicatorResultsMap.put("颗粒层+棘层+基底细胞层面积", createIndicator(organArea1.setScale(3, RoundingMode.HALF_UP).toString(), SQ_UM_THOUSAND, "10D12F"));
        indicatorResultsMap.put("固有层+肌层面积", createIndicator(organArea2.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "10D01C"));
        /**
        A	角质层面积	10D12E
		B	颗粒层+棘层+基底细胞层面积	10D12F
		C	固有层+肌层面积	10D01C
		D	组织轮廓	10D111
		
		角质层面积占比	1=A/D
		颗粒层+棘层+基底细胞层面积占比	2=B/D
		固有层+肌层面积占比	3=C/D
		舌面积	4=D
         */
        //二级指标
        if(bigDecimal.signum() == 0){
            indicatorResultsMap.put("角质层面积占比", new IndicatorAddIn("Stratum corneum area%", "0", "%",areaUtils.getStructureIds("10D12E","10D111")));
            indicatorResultsMap.put("颗粒层+棘层+基底细胞层面积占比", new IndicatorAddIn("Nucleated cell layer area%", "0", "%",areaUtils.getStructureIds("10D12F","10D111")));
            indicatorResultsMap.put("固有层+肌层面积占比", new IndicatorAddIn("Lamina propria and Muscularis area%", "0", "%",areaUtils.getStructureIds("10D01C","10D111")));
        }else{
            BigDecimal multiply = bigDecimal.multiply(new BigDecimal("1000"));
            indicatorResultsMap.put("角质层面积占比", new IndicatorAddIn("Stratum corneum area%", organArea.divide(multiply,5,RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(3).toString(),"%",areaUtils.getStructureIds("10D12E","10D111")));
            indicatorResultsMap.put("颗粒层+棘层+基底细胞层面积占比", new IndicatorAddIn("Nucleated cell layer area%", organArea1.divide(multiply,5,RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(3).toString(), "%",areaUtils.getStructureIds("10D12F","10D111")));
            indicatorResultsMap.put("固有层+肌层面积占比", new IndicatorAddIn("Lamina propria and Muscularis area%", organArea2.divide(bigDecimal,5,RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(3).toString(), "%",areaUtils.getStructureIds("10D01C","10D111")));
        }
        indicatorResultsMap.put("舌面积", new IndicatorAddIn("Tongue area", new BigDecimal(singleSlide.getArea()).setScale(3,RoundingMode.HALF_UP).toString(), SQ_MM,"10D111"));
        
        
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
        //aiForecastService.addOutIndicators(jsonTask.getSingleId(), indicatorResultsMap);

    }
}
