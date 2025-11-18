package cn.staitech.fr.service.strategy.json.impl;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.DynamicData;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @author wanglibei
 * @version V1.0
 * @ClassName: SpinalCordParserStrategyImpl
 * @Description-d:脊髓
 * @date 2025年7月22日
 */
@Slf4j
@Component("Spinal_cord")
public class SpinalCordParserStrategyImpl extends AbstractCustomParserStrategy {
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
        log.info("SpinalCordParserStrategyImpl init");
    }


    @Override
    public void alculationIndicators(JsonTask jsonTask) {

        log.info("大鼠脊髓构指标计算开始");

        //G 灰质面积（全片）	G	mm2
        BigDecimal bigDecimalG = commonJsonParser.getOrganArea(jsonTask, "1390B3").getStructureAreaNum();
        //H 白质面积（全片）	H	mm2	已扣除灰质
        BigDecimal bigDecimalH = commonJsonParser.getOrganArea(jsonTask, "1390B2").getStructureAreaNum();

        //I 中央管面积（全片）	I	103 μm2
        BigDecimal bigDecimalI = commonJsonParser.getOrganArea(jsonTask, "1390B4").getStructureAreaNum();

        //J 室管膜细胞核数量（全片）	D	个	单个脊髓内数据相加输出
        //Integer mucosaCountD = commonJsonParser.getOrganAreaCount(jsonTask, "1390B5");
        //K	红细胞面积（全片）	mm2
        BigDecimal bigDecimalE = commonJsonParser.getOrganArea(jsonTask, "139004").getStructureAreaNum();
        //L	组织轮廓面积（全片）	mm2
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());
        BigDecimal bigDSlideArea = new BigDecimal(slideArea);

        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();

        indicatorResultsMap.put("灰质面积（全片 ）", new IndicatorAddIn("", bigDecimalG.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1, "1390B3"));
        indicatorResultsMap.put("白质面积（全片 ）", new IndicatorAddIn("", bigDecimalH.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "1", "1390B2"));
        indicatorResultsMap.put("中央管面积（全片 ）", new IndicatorAddIn("", areaUtils.convertToSquareMicrometer(bigDecimalI.toString()), SQ_UM_THOUSAND, CommonConstant.NUMBER_1, "1390B4"));
//		indicatorResultsMap.put("室管膜细胞核数量（全片 ）", new IndicatorAddIn("", mucosaCountD.toString(), PIECE, "1",areaUtils.getStructureIds("1390B4","1390B5")));
        indicatorResultsMap.put("红细胞面积（全片 ）", new IndicatorAddIn("", bigDecimalE.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, CommonConstant.NUMBER_1, "139004"));

        Annotation annotationC = new Annotation();
        annotationC.setAreaName("灰质面积（单个）");
        annotationC.setAreaUnit(CommonConstant.SQUARE_MILLIMETRE);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "1390B3", annotationC,3);
        annotationC.setAreaName("白质面积（单个）");
        annotationC.setAreaUnit(CommonConstant.SQUARE_MILLIMETRE);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "1390B2", annotationC, 3);
        annotationC.setAreaName("中央管面积（单个）");
        annotationC.setAreaUnit(CommonConstant.SQUARE_MICROMETER);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "1390B4", annotationC, 1);
//        annotationC.setAreaName("室管膜细胞核数量（单个）");
//        annotationC.setAreaUnit(CommonConstant.PIECE);
//        commonJsonParser.putAnnotationDynamicData(jsonTask, "1390B4", "1390B5", annotationC, 1);
        annotationC.setAreaName("红细胞面积（单个）");
        annotationC.setAreaUnit(CommonConstant.SQUARE_MILLIMETRE);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "139004", annotationC, 3);

        BigDecimal bigDecimalG_H = BigDecimal.ZERO;
        bigDecimalG_H = bigDecimalG.add(bigDecimalH);

        //1	灰质面积占比（单个）		%	Gray matter area（per）	1=A/(A+B)
        Annotation annotationBy = new Annotation();
        annotationBy.setAreaName("灰质面积占比（单个）");
        annotationBy.setAreaUnit(PERCENTAGE);
        putAnnotationDynamicData(jsonTask, "1390B3", "1390B2", "1390B3", annotationBy);
        //2 白质面积占比（单个） 		%  2=B/(A+B)
        annotationBy.setAreaName("白质面积占比（单个）");
        annotationBy.setAreaUnit(PERCENTAGE);
        putAnnotationDynamicData(jsonTask, "1390B2", "1390B2", "1390B3", annotationBy);
        //3 中央管面积占比（单个） 		%  3=C/(A+B)
        annotationBy.setAreaName("中央管面积占比（单个）");
        annotationBy.setAreaUnit(PERCENTAGE);
        putAnnotationDynamicData(jsonTask, "1390B4", "1390B2", "1390B3", annotationBy);
        //4 室管膜细胞核数量占比（单个） 		%  4=D/C
        annotationBy.setAreaName("室管膜细胞核密度（单个）");
        annotationBy.setAreaUnit(SQ_UM_PICE);
        putAnnotationDynamicData(jsonTask, "1390B4", "1390B5", annotationBy);
        //5 红细胞面积占比（单个） 	%	5=E/(A+B)
        annotationBy.setAreaName("红细胞面积占比（单个）");
        annotationBy.setAreaUnit(PERCENTAGE);
        putAnnotationDynamicData(jsonTask, "139004", "1390B2", "1390B3", annotationBy);
        //6 脊髓面积（单个） 		mm2  6=A+B
        annotationBy.setAreaName("脊髓面积（单个）");
        annotationBy.setAreaUnit(SQ_MM);
        putAnnotationDynamicData(jsonTask, "1390B2", "1390B3", annotationBy);

        //灰质面积占比（全片）7=G/(G+H)   Gray matter area（all）
        if (bigDecimalG.compareTo(BigDecimal.ZERO) != 0 && bigDecimalG_H.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal mesenchymeAreaRate = bigDecimalG.divide(bigDecimalG_H, 7, BigDecimal.ROUND_HALF_UP);
            mesenchymeAreaRate = getMultiply100(mesenchymeAreaRate);
            indicatorResultsMap.put("灰质面积占比（全片）", new IndicatorAddIn("Gray matter area（all）", String.valueOf(mesenchymeAreaRate), PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("1390B3", "1390B2")));
        }
        //白质面积占比（全片）White matter area（all）  8=H/(G+H)
        if (bigDecimalH.compareTo(BigDecimal.ZERO) != 0 && bigDecimalG_H.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal mesenchymeAreaRate = bigDecimalH.divide(bigDecimalG_H, 7, BigDecimal.ROUND_HALF_UP);
            mesenchymeAreaRate = getMultiply100(mesenchymeAreaRate);
            indicatorResultsMap.put("白质面积占比（全片）", new IndicatorAddIn("White matter area（all）", String.valueOf(mesenchymeAreaRate), PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("1390B3", "1390B2")));
        }
        //中央管面积占比（全片）Central canal area（all）9=I/(G+H)
        if (bigDecimalI.compareTo(BigDecimal.ZERO) != 0 && bigDecimalG_H.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal mesenchymeAreaRate = bigDecimalI.divide(bigDecimalG_H, 7, BigDecimal.ROUND_HALF_UP);
            mesenchymeAreaRate = getMultiply100(mesenchymeAreaRate);
            indicatorResultsMap.put("中央管面积占比（全片）", new IndicatorAddIn("Central canal area（all）", String.valueOf(mesenchymeAreaRate), PERCENTAGE, CommonConstant.NUMBER_0, areaUtils.getStructureIds("1390B3", "1390B2", "1390B4")));
        }

        //	脊髓面积	6	平方毫米	Spinal cord area（all）	12=G+H
        if (bigDecimalG_H.compareTo(BigDecimal.ZERO) != 0) {
            indicatorResultsMap.put("脊髓面积（全片）", new IndicatorAddIn("Spinal cord area（all）", String.valueOf(bigDecimalG_H.setScale(3, RoundingMode.HALF_UP)), SQ_MM, CommonConstant.NUMBER_0, areaUtils.getStructureIds("1390B3", "1390B2")));
        }
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);

    }

    public void putAnnotationDynamicData(JsonTask jsonTask, String structureC, String structureD, Annotation inputAnnotation) {
        Long sequenceNumber = commonJsonParser.getSequenceNumber(jsonTask.getSpecialId());
        List<Annotation> annotationList1 = getStructureContourList(jsonTask, structureC);
        List<Annotation> processedAnnotations = new ArrayList<>();
        for (Annotation item : annotationList1) {
            Annotation annotationD = getContourInsideOrOutside(jsonTask, item.getContour(), structureD, true);
            inputAnnotation.setStructureAreaNum(item.getStructureAreaNum());
            if (structureC.equals("1390B2") && structureD.equals("1390B3")) {
                inputAnnotation.setStructureAreaNum(new BigDecimal(1));
                annotationD.setStructureAreaNum(item.getStructureAreaNum().add(annotationD.getStructureAreaNum()));
            }

            processedAnnotations.add(processSingleAnnotation(annotationD, inputAnnotation, item, sequenceNumber));
        }
        // 批量更新数据库
        commonJsonParser.batchUpdateAnnotations(processedAnnotations);
    }

    public void putAnnotationDynamicData(JsonTask jsonTask, String structureId, String structureB, String structureA, Annotation inputAnnotation) {
        Long sequenceNumber = commonJsonParser.getSequenceNumber(jsonTask.getSpecialId());
        List<Annotation> annotationList1 = getStructureContourList(jsonTask, structureB);
        List<Annotation> processedAnnotations = new ArrayList<>();
        for (Annotation item : annotationList1) {
            Annotation annotationA = getContourInsideOrOutside(jsonTask, item.getContour(), structureA, true);
            Annotation annotationS = new Annotation();
            if (structureId.equals(structureA)) {
                annotationS = annotationA;
            } else if (structureId.equals(structureB)) {
                annotationS = item;
            } else {
                annotationS = getContourInsideOrOutside(jsonTask, item.getContour(), structureId, true);
            }

            inputAnnotation.setStructureAreaNum(item.getStructureAreaNum().add(annotationA.getStructureAreaNum()));
            processedAnnotations.add(processSingleAnnotation(annotationS, inputAnnotation, item, sequenceNumber));

        }
        // 批量更新数据库
        commonJsonParser.batchUpdateAnnotations(processedAnnotations);
    }

    private Annotation processSingleAnnotation(Annotation annotationBy, Annotation inputAnnotation, Annotation item, Long sequenceNumber) {
        JSONArray jsonArray = new JSONArray();
        // 处理动态数据
        List<String> list = covertList(item, jsonArray);
        if (inputAnnotation.getAreaName() != null && annotationBy.getStructureAreaNum() != null) {
            DynamicData dynamicData = buildDynamicData(inputAnnotation.getAreaName(), formatDecimal(annotationBy.getStructureAreaNum()), inputAnnotation.getAreaUnit());
            jsonArray = updateDynamicDataList(list, jsonArray, dynamicData);
            list = addList(list, inputAnnotation.getAreaName());
        }

        if (inputAnnotation.getPerimeterName() != null && annotationBy.getStructurePerimeterNum() != null) {
            DynamicData dynamicData = buildDynamicData(inputAnnotation.getPerimeterName(), formatDecimal(annotationBy.getStructurePerimeterNum()), inputAnnotation.getPerimeterUnit());
            jsonArray = updateDynamicDataList(list, jsonArray, dynamicData);
            list = addList(list, inputAnnotation.getPerimeterName());
        }
        if (inputAnnotation.getStructureAreaNum() != null) {
            DynamicData dynamicData = buildDynamicData(inputAnnotation.getAreaName(), getProportion(annotationBy.getStructureAreaNum(), inputAnnotation.getStructureAreaNum()).toString(), inputAnnotation.getAreaUnit());
            jsonArray = updateDynamicDataList(list, jsonArray, dynamicData);
            list = addList(list, inputAnnotation.getAreaName());
        }
        if (inputAnnotation.getCountName() != null) {
            DynamicData dynamicData = buildDynamicData(inputAnnotation.getCountName(), String.valueOf(annotationBy.getCount()), inputAnnotation.getCountUnit());
            jsonArray = updateDynamicDataList(list, jsonArray, dynamicData);
        }

        if (jsonArray.size() > 0) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("dynamicData", jsonArray);
            item.setSequenceNumber(sequenceNumber);
            item.setDynamicData(jsonObject.toString());
            return item;
        }

        return null;
    }

    private List<String> covertList(Annotation item, JSONArray jsonArray) {
        List<String> list = new ArrayList<>();
        if (item.getDynamicDataList() != null) {
            JSONObject jsonObject = JSONObject.parseObject(item.getDynamicDataList().toString());
            if (jsonObject.getJSONArray("dynamicData") != null) {
                jsonArray = jsonObject.getJSONArray("dynamicData");
                for (int j = 0; j < jsonArray.size(); j++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(j);
                    list.add(jsonObject1.getString("name"));
                }
            }
        }
        return list;
    }

    private DynamicData buildDynamicData(String name, String data, String unit) {
        DynamicData dynamicData = new DynamicData();
        dynamicData.setName(name);
        dynamicData.setData(data);
        dynamicData.setUnit(unit);
        return dynamicData;
    }

    public JSONArray updateDynamicDataList(List<String> nameList, JSONArray jsonArray, DynamicData dynamicData) {
        if (nameList.contains(dynamicData.getName())) {
            for (int j = 0; j < jsonArray.size(); j++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(j);
                if (Objects.equals(jsonObject1.getString("name"), dynamicData.getName())) {
                    jsonObject1.put("data", dynamicData.getData());
                }
            }
        } else {
            jsonArray.add(dynamicData);
        }
        return jsonArray;
    }

    public List<String> addList(List<String> nameList, String name) {
        if (!nameList.contains(name)) {
            nameList.add(name);
        }
        return nameList;
    }

    private String formatDecimal(BigDecimal value) {
        return value.setScale(3, RoundingMode.HALF_UP).toString();
    }

    @Override
    public String getAlgorithmCode() {
        return "Spinal_cord";
    }
}
