package cn.staitech.fr.service.strategy.json;

import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.utils.MathUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * @author mugw
 * @version 1.0
 * @description
 * @date 2024/5/14 11:13:10
 */
@Slf4j
@Data
public abstract class AbstractCustomParserStrategy implements CustomParserStrategy{

    private CommonJsonParser commonJsonParser;
    private CommonJsonCheck commonJsonCheck;

    // 指标单位
    protected static final String PIECE = "个";
//    protected static final String MM = "毫米";
//    protected static final String UM = "微米";
//    protected static final String SQ_MM = "平方毫米";
//    protected static final String SQ_UM = "平方微米";
//    protected static final String SQ_MM_PIECE = "个/平方毫米";
//    protected static final String SQ_UM_THOUSAND = "×10³平方微米";
//    protected static final String MM_PIECE = "个/毫米";
    
    protected static final String MM = "mm";
    protected static final String UM = "μm";
    protected static final String SQ_MM = "mm²";
    protected static final String SQ_UM = "μm²";
    protected static final String SQ_MM_PIECE = "个/mm²";
    protected static final String SQ_UM_THOUSAND = "10³ μm²";
    protected static final String MM_PIECE = "个/mm";
    protected static final String SQ_UM_PICE = "个/10³μm²";
    
    protected static final String PERCENTAGE = "%";
    protected static final String A = "3.141";
    protected static final String NOT = "无";

    

    @Override
    public void parseJson(JsonTask jsonTask, JsonFile jsonFileS) {
        commonJsonParser.parseJson(jsonTask, jsonFileS);
    }

    @Override
    public boolean checkJson(JsonTask jsonTask, List<JsonFile> jsonFileList) {
        return commonJsonCheck.checkJson(jsonTask, jsonFileList);
    }

    /**
     * 获取脏器轮廓面积
     *
     * @param jsonTask    jsonTask
     * @param structureId 结构ID
     * @return 脏器面积-平方毫米
     */
    protected Annotation getOrganArea(JsonTask jsonTask, String structureId) {
        return commonJsonParser.getOrganArea(jsonTask,structureId);
    }

    protected Annotation getInsideOrOutside(JsonTask jsonTask, String structureId, String structureIds, Boolean InsideOrOutside){
        return commonJsonParser.getInsideOrOutside(jsonTask,structureId,structureIds,InsideOrOutside);
    }

    protected List<Annotation> getStructureContourList(JsonTask jsonTask, String structureId){
        return commonJsonParser.getStructureContourList(jsonTask,structureId);
    }

    protected Annotation getContourInsideOrOutside(JsonTask jsonTask, String contour, String structureIds, Boolean InsideOrOutside){
        return commonJsonParser.getContourInsideOrOutside(jsonTask,contour,structureIds,InsideOrOutside);
    }

    protected Annotation getOrganArea(JsonTask jsonTask, String structureId,BigDecimal unit) {
        Annotation annotation = commonJsonParser.getOrganArea(jsonTask,structureId);
        BigDecimal bigDecimal = annotation.getStructureAreaNum();
        if (bigDecimal!=null){
        	bigDecimal = bigDecimal.multiply(unit).setScale(3, RoundingMode.HALF_UP);
            annotation.setStructureAreaNum(bigDecimal);
        }
        return annotation;
    }

    /**
     * 取脏器轮廓数量
     *
     * @param jsonTask    jsonTask
     * @param structureId 结构ID
     * @return 脏器轮廓数量
     */
    protected Integer getOrganAreaCount(JsonTask jsonTask, String structureId) {
        return commonJsonParser.getOrganAreaCount(jsonTask,structureId);
    }

    /**
     * 创建指标对象（单个面积的提示）
     *
     * @return 指标对象
     */
    protected IndicatorAddIn createDefaultIndicator() {
        return new IndicatorAddIn(CommonConstant.SINGLE_RESULT, CommonConstant.NUMBER_1);
    }

    /**
     * 创建指标对象（算法输出指标）
     *
     * @param result 结果
     * @param unit   单位
     * @return 指标对象
     */
    protected IndicatorAddIn createIndicator(Object result, String unit) {
        if (result instanceof BigDecimal) {
            BigDecimal roundedResult = ((BigDecimal) result).setScale(3, RoundingMode.HALF_UP);
            return new IndicatorAddIn("", String.valueOf(roundedResult), unit, CommonConstant.NUMBER_1);
        }
        return new IndicatorAddIn("", String.valueOf(result), unit, CommonConstant.NUMBER_1);
    }

    /**
     * 创建指标对象（产品呈现指标）
     *
     * @param enName 指标英文名称
     * @param result 结果
     * @param unit   单位
     * @return 指标对象
     */
    protected IndicatorAddIn createNameIndicator(String enName, Object result, String unit) {
        if (result instanceof BigDecimal) {
            BigDecimal roundedResult = ((BigDecimal) result).setScale(3, RoundingMode.HALF_UP);
            return new IndicatorAddIn(enName, String.valueOf(roundedResult), unit, CommonConstant.NUMBER_0);
        }
        return new IndicatorAddIn(enName, String.valueOf(result), unit, CommonConstant.NUMBER_0);
    }

    protected IndicatorAddIn createComplexIndicator(List<BigDecimal> dataList,String enName,String unit,String type) {
        String result = MathUtils.getConfidenceInterval(dataList);
        return new IndicatorAddIn(enName, result, unit,type);
    }

}
