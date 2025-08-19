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
import cn.staitech.fr.utils.AreaUtils;
import cn.staitech.fr.utils.MathUtils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
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
 * 
* @ClassName: SeminalVesicleGlandParserStrategyImpl
* @Description-d:精囊腺
* @author wanglibei
* @date 2025年7月22日
* @version V1.0
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
    @Autowired
    private AreaUtils areaUtils;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("SeminalVesicleGlandParserStrategyImpl init");
    }
    /**
     * 
	    A	腺上皮面积（单个）	mm2	12D074
		B	腺上皮面积（全片）	mm2	12D074
		C	腺腔面积（单个）	mm2	12D074、12D0E9
		D	腺腔面积（全片）	mm2	12D074、12D0E9
		E	腺上皮细胞核数量（单个）	个	12D074、12D0ED
		F	组织轮廓面积	mm2	12D111
		
		精囊腺面积	1=F
		腺上皮面积（全片）	2=B
		腺上皮面积占比（单个）	3=A/(A+C)
		腺泡上皮细胞核密度（单个）	4=E/A
		间质和肌层面积占比	5=(F-B-D)/F
     */
    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("精囊腺结构指标计算开始");

        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        //b腺上皮面积（全片）
        BigDecimal organArea = commonJsonParser.getOrganArea(jsonTask, "12D074").getStructureAreaNum();
       //d腺腔面积（全片）
        BigDecimal organArea1 = commonJsonParser.getOrganArea(jsonTask, "12D0E9").getStructureAreaNum();

        indicatorResultsMap.put("腺腔面积（全片）", new IndicatorAddIn("Glandular cavity area (all)", organArea1.setScale(3,RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1,areaUtils.getStructureIds("12D074","12D0E9")));

        Annotation annotation1 = new Annotation();
        annotation1.setAreaName("腺上皮面积（单个）");
        annotation1.setAreaUnit(SQ_MM);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask,"12D074",annotation1,3);
        indicatorResultsMap.put("腺上皮面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1,"12D074"));

        Annotation annotationBy = new Annotation();
        annotationBy.setAreaName("腺腔面积（单个）");
        annotationBy.setAreaUnit(SQ_MM);
        commonJsonParser.putAnnotationDynamicData(jsonTask,"12D074","12D0E9",annotationBy);
        indicatorResultsMap.put("腺腔面积（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1,areaUtils.getStructureIds("12D074","12D0E9")));

        Annotation annotationBy2 = new Annotation();
        annotationBy2.setCountName("腺上皮细胞核数量（单个）");
        commonJsonParser.putAnnotationDynamicData(jsonTask,"12D074","12D0ED",annotationBy2);
        indicatorResultsMap.put("腺上皮细胞核数量（单个）", new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1,areaUtils.getStructureIds("12D074","12D0ED")));



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
                if(add.signum() != 0){

                    lists.add(structureAreaNum.divide(add,6,RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(4));
                }
                //e
                Integer count = contourInsideOrOutside2.getCount();
                if(structureAreaNum.signum() != 0){
                    BigDecimal divide = new BigDecimal(count).divide(structureAreaNum, 4, RoundingMode.HALF_UP);
                    listNum.add(divide);

                }
                //contourInsideOrOutside.getStructurePerimeterNum();// 周长
                //contourInsideOrOutside.getCount();//数量
            }
        }
        /**
         * 
    	    A	腺上皮面积（单个）	mm2	12D074
    		B	腺上皮面积（全片）	mm2	12D074
    		C	腺腔面积（单个）	mm2	12D074、12D0E9
    		D	腺腔面积（全片）	mm2	12D074、12D0E9
    		E	腺上皮细胞核数量（单个）	个	12D074、12D0ED
    		F	组织轮廓面积	mm2	12D111
    		
    		精囊腺面积	1=F
    		腺上皮面积（全片）	2=B
    		腺上皮面积占比（单个）	3=A/(A+C)
    		腺泡上皮细胞核密度（单个）	4=E/A
    		间质和肌层面积占比	5=(F-B-D)/F
         */
        String confidenceInterval = MathUtils.getConfidenceInterval(lists);
        String confidenceInterval1 = MathUtils.getConfidenceInterval(listNum);
        indicatorResultsMap.put("精囊腺面积", new IndicatorAddIn("Seminal vesicle area", new BigDecimal(singleSlide.getArea()).setScale(3,RoundingMode.HALF_UP).toString(), SQ_MM,"12D111"));
        indicatorResultsMap.put("腺上皮面积（全片）", new IndicatorAddIn("Acinar epithelial area (all)", organArea.setScale(3,RoundingMode.HALF_UP).toString(), SQ_MM,"12D074"));
        indicatorResultsMap.put("腺上皮面积占比（单个）", new IndicatorAddIn("Acinar epithelial area% (per)", confidenceInterval, "%",areaUtils.getStructureIds("12D074","12D074","12D0E9")));
        indicatorResultsMap.put("腺泡上皮细胞核密度（单个）", new IndicatorAddIn("Nucleus density of acinar epithelium (per)", confidenceInterval1, SQ_MM_PIECE,areaUtils.getStructureIds("12D074","12D0ED","12D074")));
        //F
        BigDecimal bigDecimal = new BigDecimal(singleSlide.getArea());
        if(bigDecimal.signum() == 0){
            indicatorResultsMap.put("间质和肌层面积占比", new IndicatorAddIn("Mesenchyme and muscular area%", "0", "%",areaUtils.getStructureIds("12D111","12D074","12D074","12D0E9")));
        }else{
            BigDecimal divide = (bigDecimal.subtract(organArea).subtract(organArea1)).divide(bigDecimal, 5, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(3);
            indicatorResultsMap.put("间质和肌层面积占比", new IndicatorAddIn("Mesenchyme and muscular area%", divide.toString(), "%",areaUtils.getStructureIds("12D111","12D074","12D074","12D0E9")));

        }

        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
        //aiForecastService.addOutIndicators(jsonTask.getSingleId(), indicatorResultsMap);


    }

    @Override
    public String getAlgorithmCode() {
        return "Seminal_vesicles";
    }
}
