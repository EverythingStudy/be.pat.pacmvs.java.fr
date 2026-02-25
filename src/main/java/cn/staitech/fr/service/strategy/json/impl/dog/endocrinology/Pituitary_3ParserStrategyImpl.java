package cn.staitech.fr.service.strategy.json.impl.dog.endocrinology;

import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 
* @ClassName: PituitaryParserStrategyImpl
* @Description:犬-垂体
* @author wanglibei
* @date 2026年2月11日
* @version V1.0
 */
@Slf4j
@Component("Pituitary_3")
public class Pituitary_3ParserStrategyImpl extends AbstractCustomParserStrategy{
    @Resource
    private AreaUtils areaUtils;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private CommonJsonCheck commonJsonCheck;
    @Resource
    private SingleSlideMapper singleSlideMapper;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("PituitaryParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
    	/**
    	 * 
    	结构编码
    	神经部 30607F
    	中间部 306081
    	中间部细胞核 306082
    	远侧部 306083
    	远侧部细胞核 306084
    	红细胞 306004
    	组织轮廓 306111
    	
    	
    	算法输出指标指标代码（仅限本文档）单位（保留小数点后三位）备注相关结构
    	神经部面积 A mm² 若多个数据则相加输出 30607F
    	中间部面积 B mm² 若多个数据则相加输出 306081
    	远侧部面积  C mm² 若多个数据则相加输出 306083
    	红细胞面积D mm² 数据相加输出 306004
    	中间部细胞核数量 E 个 306081、306082
    	远侧部细胞核数量 F 个 306083、306084
    	组织轮廓面积 G mm² 仅辅助指标7计算，数值不显示在页面指标表格里306111
    	*/
        log.info("犬垂体构指标计算开始");
        // A 神经部面积 A 平方毫米 若多个数据则相加输出
        BigDecimal A_30607F_area = getOrganArea(jsonTask, "30607F").getStructureAreaNum();
        A_30607F_area = commonJsonParser.getBigDecimalValue(A_30607F_area.setScale(3, RoundingMode.HALF_UP));
        // B 中间部面积 B 平方毫米 若多个数据则相加输出
        BigDecimal B_306081_area = getOrganArea(jsonTask, "306081").getStructureAreaNum();
        B_306081_area = commonJsonParser.getBigDecimalValue(B_306081_area.setScale(3, RoundingMode.HALF_UP));
        // C 远侧部面积 C 平方毫米 若多个数据则相加输出
        BigDecimal C_306083_area = getOrganArea(jsonTask, "306083").getStructureAreaNum();
        C_306083_area = commonJsonParser.getBigDecimalValue(C_306083_area.setScale(3, RoundingMode.HALF_UP));
        //D 红细胞面积  平方毫米 数据相加输出
        BigDecimal D_306004_area  = getOrganArea(jsonTask, "306004").getStructureAreaNum();
        D_306004_area = commonJsonParser.getBigDecimalValue(D_306004_area.setScale(3, RoundingMode.HALF_UP));
        
        // E 中间部细胞核数量  个 无
        Integer E_306082_Count = getOrganAreaCount(jsonTask, "306082");
        
        // F 远侧部细胞核数量  个 无
        Integer F_306084_Count = getOrganAreaCount(jsonTask, "306084");
        
        //G 组织轮廓面积
        String slideArea = areaUtils.getFineContourArea(jsonTask.getSingleId());
        BigDecimal G_306111_area = new BigDecimal(slideArea);
        G_306111_area = commonJsonParser.getBigDecimalValue(G_306111_area.setScale(3, RoundingMode.HALF_UP));
        
        
        
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        /**
    	 * 
    	结构编码
    	神经部 30607F
    	中间部 306081
    	中间部细胞核 306082
    	远侧部 306083
    	远侧部细胞核 306084
    	红细胞 306004
    	组织轮廓 306111
    	
    	
    	算法输出指标指标代码（仅限本文档）单位（保留小数点后三位）备注相关结构
    	神经部面积 A mm² 若多个数据则相加输出 30607F
    	中间部面积 B mm² 若多个数据则相加输出 306081
    	远侧部面积  C mm² 若多个数据则相加输出 306083
    	红细胞面积D mm² 数据相加输出 306004
    	中间部细胞核数量 E 个 306081、306082
    	远侧部细胞核数量 F 个 306083、306084
    	组织轮廓面积 G mm² 仅辅助指标7计算，数值不显示在页面指标表格里306111
    	*/
        indicatorResultsMap.put("神经部面积", createIndicator(String.valueOf(A_30607F_area.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "30607F"));
        indicatorResultsMap.put("中间部面积", createIndicator(String.valueOf(B_306081_area.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "306081"));
        indicatorResultsMap.put("远侧部面积", createIndicator(String.valueOf(C_306083_area.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "306083"));
        indicatorResultsMap.put("红细胞面积", createIndicator(String.valueOf(D_306004_area.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "306004"));
        indicatorResultsMap.put("中间部细胞核数量", createIndicator(String.valueOf(E_306082_Count), PIECE, "306081,306082"));
        indicatorResultsMap.put("远侧部细胞核数量", createIndicator(String.valueOf(F_306084_Count), PIECE, "306083,306084"));
        indicatorResultsMap.put("组织轮廓面积", createIndicator(String.valueOf(G_306111_area), PIECE, "306111"));

       //产品呈现指标
        /**
    	 * 
	        产品呈现指标指标代码（仅限本文档）单位（保留小数点后三位）English计算方式备注
	        神经部面积占比 1 % Pars nervosa area% 1=A/G
	        中间部面积占比 2 % Pars intermedia area% 2=B/G
	        远侧部面积占比 3 % Pars distalis area% 3=C/G
	        红细胞面积占比 4 % Erythrocyte area% 4=D/G
	        中间部细胞核密度 5 个/mm2 Nucleus density of pars intermedia 5=E/B
	        远侧部细胞核密度 6 个/mm2 Nucleus density ofpars distalis 6=F/C
	        垂体面积 7 mm2 Pituitary gland area 7=G
         */
        
        //1 神经部面积占比	   %	Pars nervosa area%	1=A/G
        BigDecimal pna = getProportion(A_30607F_area, G_306111_area);
        indicatorResultsMap.put("神经部面积占比", createNameIndicator("Pars nervosa area%", String.valueOf(pna.setScale(3, RoundingMode.HALF_UP)), PERCENTAGE, "30607F,306111"));
        //2 中间部面积占比	  %	   Pars intermedia area%	2=B/G
        BigDecimal pia = getProportion(B_306081_area, G_306111_area);
        indicatorResultsMap.put("中间部面积占比", createNameIndicator("Pars intermedia area%", String.valueOf(pia.setScale(3, RoundingMode.HALF_UP)), PERCENTAGE, "306081,306111"));

        //3 远侧部面积占比	 %	  Pars distalis area%	3=C/G
        BigDecimal pda = getProportion(C_306083_area, G_306111_area);
        indicatorResultsMap.put("远侧部面积占比", createNameIndicator("Pars distalis area%", String.valueOf(pda.setScale(3, RoundingMode.HALF_UP)), PERCENTAGE, "306083,306111"));

        //4 红细胞面积占比	 %	 Erythrocyte area%	4=D/G
        BigDecimal ea = getProportion(D_306004_area, G_306111_area);
        indicatorResultsMap.put("红细胞面积占比", createNameIndicator("Erythrocyte area%", String.valueOf(ea.setScale(3, RoundingMode.HALF_UP)), PERCENTAGE, "306004,306111"));

        //5 中间部细胞核密度 个/平方毫米	Nucleus density of pars intermedia	5=E/B
        BigDecimal ndpi = commonJsonParser.getProportionMultiply(new BigDecimal(E_306082_Count), B_306081_area);
        indicatorResultsMap.put("中间部细胞核密度", createNameIndicator("Nucleus density of pars intermedi", String.valueOf(ndpi.setScale(3, RoundingMode.HALF_UP)), SQ_MM_PIECE, "306082,306081"));

        //6 远侧部细胞核密度个/平方毫米	Nucleus density of 7=G/C
        BigDecimal ndo = commonJsonParser.getProportionMultiply(G_306111_area, C_306083_area);
        indicatorResultsMap.put("远侧部细胞核密度", createNameIndicator("Nucleus density of pars distalis", String.valueOf(ndo.setScale(3, RoundingMode.HALF_UP)), SQ_MM_PIECE, "306111,306083"));
        //7 垂体面积	 8=H
        indicatorResultsMap.put("垂体面积", createNameIndicator("Pituitary gland area", String.valueOf(G_306111_area.setScale(3, RoundingMode.HALF_UP)), SQ_MM, "106111"));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Pituitary_3";
    }

}
