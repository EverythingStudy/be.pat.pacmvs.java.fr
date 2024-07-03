package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import cn.staitech.fr.utils.MathUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 附睾-EP
 */
@Slf4j
@Service("Epididymide")
public class EpididymideParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.info("EpididymideParserStrategyImpl init");
    }

    /**
     结构	编码
     输出小管/附睾管黏膜上皮	12F0F5
     输出小管/附睾管管腔	12F0F4
     精子	12F0F7
     输出小管/附睾管黏膜上皮细胞核	12F0F6
     血管	12F003
     组织轮廓	12F111
     算法输出指标	指标代码（仅限本文档）	单位（保留小数点后3位）	备注
     输出小管/附睾管黏膜上皮面积（单个）	A	103平方微米	单个输出小管/附睾管黏膜上皮面积	没做扣减
     输出小管/附睾管黏膜上皮面积（全片）	B	平方毫米	数据相加输出
     输出小管/附睾管黏膜上皮周长（单个）	C	毫米	单个输出小管/附睾管黏膜周长
     输出小管/附睾管管腔面积（单个）	D	103平方微米	单个输出小管/附睾管黏膜管腔面积
     输出小管/附睾管管腔面积（全片）	E	平方毫米	数据相加输出
     精子面积（单个）	F	103平方微米	单个输出小管/附睾管精子面积
     精子面积（全片）	G	平方毫米	数据相加输出
     黏膜上皮细胞核数量（单个）	H	个	单个输出小管/附睾管黏膜上皮细胞核数量
     血管面积	I	平方毫米	数据相加输出
     组织轮廓面积	J	平方毫米

     产品呈现指标	指标代码（仅限本文档）	单位（保留小数点后3位）	English	计算方式	备注
     附睾面积	1	平方毫米	Epididymal area	1=J
     输出小管和附睾管面积占比（全片）	2	%	Efferent ducts and epididymal ducts area%（all）	2=B/J
     间质面积占比	3	%	Mesenchyme area%	3=1-B/J
     黏膜上皮面积占比（单个）	4	%	Mucosal epithelium area% (per)	4=1-D/A	单个即单个输出小管或附睾管
     以95%置信区间和均数±标准差呈现
     精子面积占比（单个）	5	%	Sperm area% (per)	5=F/D	单个即单个输出小管或附睾管
     以95%置信区间和均数±标准差呈现
     精子面积占比（全片）	6	%	Sperm area% (all)	6=G/E
     黏膜上皮细胞核密度（单个）	7	个/毫米	Mucosal epithelial nucleus% (per)	7=H/C	单个即单个输出小管或附睾管
     以95%置信区间和均数±标准差呈现
     血管相对面积	8	%	Vessel area%	8=I/J
     黏膜上皮厚度（单个）	9	微米	Average thickness of mucosal epithelium (per)	 	单个即单个输出小管或附睾管
     以95%置信区间和均数±标准差呈现
     */
    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        Map<String, IndicatorAddIn>  resultsMap = new HashMap<>();

        // 获取各种指标
        BigDecimal organAreaB = areaUtils.getOrganArea(jsonTask, "12F0F5");// B输出小管/附睾管黏膜上皮面积（全片）
        Annotation annotation = commonJsonParser.getOrganArea(jsonTask, "12F0F5");// C输出小管/附睾管黏膜上皮周长（单个）
        BigDecimal perimeterC = annotation.getStructurePerimeterNum();
        BigDecimal organAreaE = areaUtils.getOrganArea(jsonTask, "12F0F4");// E输出小管/附睾管管腔面积（全片）
        BigDecimal organAreaG = areaUtils.getOrganArea(jsonTask, "12F0F7");// G精子面积（全片）
        Integer organAreaH = areaUtils.getOrganAreaCount(jsonTask, "12F0F7");// G精子面积（全片）
        BigDecimal organAreaI = areaUtils.getOrganArea(jsonTask, "12F003");// I血管面积
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());// J组织轮廓面积
        BigDecimal organAreaJ = BigDecimal.valueOf(Double.parseDouble(slideArea));
        // todo H黏膜上皮细胞核数量（单个）

        Annotation annotationBy = new Annotation();
        annotationBy.setAreaName("输出小管/附睾管黏膜上皮面积（单个）");
        annotationBy.setAreaUnit(SQ_UM_THOUSAND);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask,"12F0F5",annotationBy,1);

        Annotation annotation1 = new Annotation();
        annotation1.setPerimeterName("输出小管/附睾管黏膜上皮周长（单个）");
        annotation1.setPerimeterUnit("毫米");
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask,"12F0F5",annotation1,3);

        Annotation annotation2s = new Annotation();
        annotation2s.setCountName("黏膜上皮细胞核数量（单个）");
        commonJsonParser.putAnnotationDynamicData(jsonTask,"12F0F5","12F0F6", annotation2s);

        Annotation annotationBy1 = new Annotation();
        annotationBy1.setAreaName("输出小管/附睾管管腔面积（单个）");
        annotationBy1.setAreaUnit(SQ_UM_THOUSAND);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask,"12F0F4",annotationBy1,1);


        Annotation annotationBy2 = new Annotation();
        annotationBy2.setAreaName("精子面积（单个）");
        annotationBy2.setAreaUnit(SQ_UM_THOUSAND);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask,"12F0F7",annotationBy2,1);

        // 算法输出指标
        resultsMap.put("输出小管/附睾管黏膜上皮面积（单个）", createDefaultIndicator());// A输出小管/附睾管黏膜上皮面积（单个）
        resultsMap.put("输出小管/附睾管黏膜上皮面积（全片）", createIndicator(organAreaB, SQ_MM));
        resultsMap.put("输出小管/附睾管黏膜上皮周长（单个）", createDefaultIndicator());
        resultsMap.put("输出小管/附睾管管腔面积（全片）", createIndicator(organAreaE, SQ_MM));
        resultsMap.put("精子面积（全片）", createIndicator(organAreaG, SQ_MM));
        resultsMap.put("血管面积", createIndicator(organAreaI.setScale(3, RoundingMode.HALF_UP), SQ_MM));
        resultsMap.put("黏膜上皮细胞核数量（单个）",createDefaultIndicator());
        resultsMap.put("输出小管/附睾管管腔面积（单个）", createDefaultIndicator());// D输出小管/附睾管管腔面积（单个）
        resultsMap.put("精子面积（单个）", createDefaultIndicator());// F精子面积（单个）

        // 产品呈现指标
        BigDecimal one = new BigDecimal("1");

        // 输出小管和附睾管面积占比（全片）
        BigDecimal erythrocyteArea = commonJsonParser.getProportionMultiply(organAreaB, organAreaJ);
        // 间质面积占比
        BigDecimal mucosalArea = one.subtract(erythrocyteArea);
        // 黏膜上皮面积占比（单个）
        List<BigDecimal> list1 = new ArrayList<>();
        List<Annotation> annotationList1 = commonJsonParser.getStructureContourList(jsonTask,"12F0F5");
        for(Annotation i : annotationList1){
            Annotation annotation2 = commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "12F0F4", true);
            list1.add(one.subtract(commonJsonParser.bigDecimalDivideCheck(annotation2.getStructureAreaNum(),i.getStructureAreaNum())).multiply(new BigDecimal("100")).setScale(3, RoundingMode.HALF_UP));
        }
        String mucosalAreaPer = MathUtils.getConfidenceInterval(list1);

        // 精子面积占比（单个）
        List<BigDecimal> list2 = new ArrayList<>();
        List<String> list3s = new ArrayList<>();
        List<String> list4s = new ArrayList<>();
        List<Annotation> annotationList2 = commonJsonParser.getStructureContourList(jsonTask,"12F0F4");
        for(Annotation i : annotationList2){
            Annotation annotation2 = commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "12F0F7", true);
            BigDecimal res = commonJsonParser.bigDecimalDivideChecks(BigDecimal.valueOf(Double.parseDouble(areaUtils.micrometerToSquareMicrometer(annotation2.getArea()))),BigDecimal.valueOf(Double.parseDouble(areaUtils.micrometerToSquareMicrometer(i.getArea()))));
            list3s.add(areaUtils.micrometerToSquareMicrometer(annotation2.getArea()));
            list4s.add(areaUtils.micrometerToSquareMicrometer(i.getArea()));
            list2.add(res.multiply(new BigDecimal("100")).setScale(3, RoundingMode.HALF_UP));
        }
//        log.info("====================================================");
//        for(int i = 0; i < list2.size(); i++){
//            //log.info("{},{}",i,list2.get(i));
//            System.out.println(list2.get(i));
//        }
//        log.info("------------------------------------------------->");
//        for(int i = 0; i < list3s.size(); i++){
//            //log.info("{},{}",i,list2.get(i));
//            System.out.println(list3s.get(i));
//        }
//        log.info("------------------------------------------------->");
//        for(int i = 0; i < list4s.size(); i++){
//            //log.info("{},{}",i,list2.get(i));
//            System.out.println(list4s.get(i));
//        }
//        log.info("====================================================");

        String spermAreaPer = MathUtils.getConfidenceInterval(list2);

        // 精子面积占比（全片）
        BigDecimal spermArea = commonJsonParser.getProportion(organAreaG, organAreaE);
        // 黏膜上皮细胞核密度（单个）
        List<BigDecimal> list3 = new ArrayList<>();
        for(Annotation i : annotationList1){
            Annotation annotation2 = commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "12F0F6", true);
            BigDecimal res = commonJsonParser.bigDecimalDivideCheck(BigDecimal.valueOf(annotation2.getCount()), i.getStructurePerimeterNum());
            list3.add(res);
        }
        String mucosalCellDensity = MathUtils.getConfidenceInterval(list3);
        // 血管相对面积
        BigDecimal vesselArea = commonJsonParser.getProportion(organAreaI, organAreaJ);
        //黏膜上皮厚度（单个）
        List<BigDecimal> list4 = new ArrayList<>();
        for(Annotation i : annotationList1){
            Annotation annotation2 = commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "12F0F4", true);
            if(i.getArea() != null && annotation2.getArea() != null){
                BigDecimal sqrtI = commonJsonParser.bigDecimalDivideCheck(BigDecimal.valueOf(Double.parseDouble(i.getArea())), BigDecimal.valueOf(Double.parseDouble(A)));
                BigDecimal sqrt1 = commonJsonParser.sqrt(sqrtI);
                BigDecimal sqrtAnnotation = commonJsonParser.bigDecimalDivideCheck(BigDecimal.valueOf(Double.parseDouble(annotation2.getArea())), BigDecimal.valueOf(Double.parseDouble(A)));
                BigDecimal sqrt2 = commonJsonParser.sqrt(sqrtAnnotation);
                list4.add(sqrt1.subtract(sqrt2));
            }
        }
        String mucosalThickness = MathUtils.getConfidenceInterval(list4);

        resultsMap.put("输出小管和附睾管面积占比（全片）", createNameIndicator("Efferent ducts and epididymal ducts area%（all）",erythrocyteArea, PERCENTAGE));
        resultsMap.put("间质面积占比", createNameIndicator("Mesenchyme area%", mucosalArea, PERCENTAGE));
        resultsMap.put("黏膜上皮面积占比（单个）", createNameIndicator("Mucosal epithelium area% (per)", mucosalAreaPer, PERCENTAGE ));
        resultsMap.put("精子面积占比（单个）", createNameIndicator("Sperm area% (per)", spermAreaPer, PERCENTAGE));
        resultsMap.put("精子面积占比（全片）", createNameIndicator("Sperm area% (all)", spermArea, PERCENTAGE));
        resultsMap.put("黏膜上皮细胞核密度（单个）", createNameIndicator("Mucosal epithelial nucleus% (per)", mucosalCellDensity, MM_PIECE));
        resultsMap.put("血管相对面积", createNameIndicator("Vessel area%", vesselArea, PERCENTAGE));
        resultsMap.put("黏膜上皮厚度（单个）", createNameIndicator("Average thickness of mucosal epithelium (per)", mucosalThickness, UM));

        resultsMap.put("附睾面积", createNameIndicator("Epididymal area", new BigDecimal(slideArea).setScale(3, RoundingMode.HALF_UP), SQ_MM));


        aiForecastService.addAiForecast(jsonTask.getSingleId(),  resultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Epididymide";
    }
}
