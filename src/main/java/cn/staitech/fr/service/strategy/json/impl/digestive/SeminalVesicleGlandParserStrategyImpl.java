package cn.staitech.fr.service.strategy.json.impl.digestive;

import cn.hutool.core.collection.CollectionUtil;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.MathUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author wudi
 * @Date 2024/5/13 10:05
 * @desc 精囊腺
 */
@Slf4j
@Component("Seminal_vesicles")
public class SeminalVesicleGlandParserStrategyImpl extends AbstractCustomParserStrategy {

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
        log.info("SeminalVesicleGlandParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("精囊腺结构指标计算开始");

        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal organArea = commonJsonParser.getOrganArea(jsonTask, "12D074").getStructureAreaNum();
        BigDecimal organArea1 = commonJsonParser.getOrganArea(jsonTask, "12D0E9").getStructureAreaNum();

        indicatorResultsMap.put("腺腔面积（全片）", new IndicatorAddIn("Glandular cavity area (all)", organArea1.toString(), "平方毫米", CommonConstant.NUMBER_1));
        indicatorResultsMap.put("腺上皮面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1));
        indicatorResultsMap.put("腺腔面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1));
        List<Annotation> structureContourList = commonJsonParser.getStructureContourList(jsonTask, "12D074");
        List<BigDecimal> lists = new ArrayList<>();
        List<BigDecimal> listNum = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(structureContourList)) {
            for (Annotation annotation : structureContourList) {
                //A
                BigDecimal structureAreaNum = annotation.getStructureAreaNum();
                Annotation contourInsideOrOutside = commonJsonParser.getContourInsideOrOutside(jsonTask, annotation.getContour(), "12D0E9", true);
                Annotation contourInsideOrOutside2 = commonJsonParser.getContourInsideOrOutside(jsonTask, annotation.getContour(), "12D0ED", true);

                //c
                BigDecimal structureAreaNum1 = contourInsideOrOutside.getStructureAreaNum();// 面积
                BigDecimal add = structureAreaNum.add(structureAreaNum1);
                if(add.compareTo(BigDecimal.ZERO)!=0){

                    lists.add(structureAreaNum.divide(add,4, RoundingMode.HALF_UP));
                }
                //e
                Integer count = contourInsideOrOutside2.getCount();
                if(structureAreaNum.compareTo(BigDecimal.ZERO)!=0){
                    BigDecimal divide = new BigDecimal(count).divide(structureAreaNum, 4, RoundingMode.HALF_UP);
                    listNum.add(divide);

                }
                //contourInsideOrOutside.getStructurePerimeterNum();// 周长
                //contourInsideOrOutside.getCount();//数量
            }
        }
        String confidenceInterval = MathUtils.getConfidenceInterval(lists);
        String confidenceInterval1 = MathUtils.getConfidenceInterval(listNum);
        indicatorResultsMap.put("精囊腺面积", new IndicatorAddIn("Seminal vesicle area", singleSlide.getArea(), "平方毫米"));
        indicatorResultsMap.put("腺上皮面积（全片）", new IndicatorAddIn("Acinar epithelial area (all)", organArea.toString(), "平方毫米"));
        indicatorResultsMap.put("腺上皮面积占比（单个）", new IndicatorAddIn("Acinar epithelial area% (per)", confidenceInterval, "%"));
        indicatorResultsMap.put("腺泡上皮细胞核密度（单个）", new IndicatorAddIn("Nucleus density of acinar epithelium (per)", confidenceInterval1, "个/平方毫米"));
        BigDecimal bigDecimal = new BigDecimal(singleSlide.getArea());
        if(bigDecimal.compareTo(BigDecimal.ZERO)==0){
            indicatorResultsMap.put("间质和肌层面积占比", new IndicatorAddIn("Mesenchyme and muscular area%", "0", "%"));
        }else{
            BigDecimal divide = (bigDecimal.subtract(organArea).subtract(organArea1)).divide(bigDecimal, 3, RoundingMode.HALF_UP);
            indicatorResultsMap.put("间质和肌层面积占比", new IndicatorAddIn("Mesenchyme and muscular area%", divide.toString(), "%"));

        }

        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
        //aiForecastService.addOutIndicators(jsonTask.getSingleId(), indicatorResultsMap);


    }

    @Override
    public String getAlgorithmCode() {
        return "Seminal_vesicles";
    }
}
